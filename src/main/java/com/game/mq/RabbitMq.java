package com.game.mq;

import com.game.controller.KeyControl;
import com.game.entity.GameFrame;
import com.game.keep.GameSaverLoader;
import com.game.tank.Tank;
import com.game.util.Constant;
import com.game.view.GameWindow;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RabbitMq
{
    // 主机IP
    private static final String HOST="127.0.0.1";
    // 主机端口
    private static final int PORT=5672;
    // 主机用户名
    private static final String USERNAME="guest";
    // 主机密码
    private static final String PASSWORD="guest";

    public static final String FILE_PATH_IS_HOST = "res/host/isHost.txt";

    // 显示消息队列
    private static final String messageQueueName="messageQueueName";
    // 控制消息队列
    private static final String controlQueueName="controlQueueName";

    private static Channel channel;
    private static String message="";
    private static String previousMessage;

    // 主从机标识符
    private static int hostMark;

    // 定时接收消息线程
    private static final ScheduledExecutorService executorService= Executors.newSingleThreadScheduledExecutor();

    // 从机此刻的控制信号
    private static int gameControl;
    // 主机获取的从机的控制信号队列
    private static final List<Integer> gameControlList = new ArrayList<>() {{add(0);}};

    /**
     * 配置服务器链接参数
     */
    public static void createConnectionFactory(String queueName) throws IOException, TimeoutException
    {
        // 配置RabbitMq连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);                      // 设置RabbitMq服务器地址
        factory.setPort(PORT);                      // 默认端口
        factory.setUsername(USERNAME);              // 默认用户名
        factory.setPassword(PASSWORD);              // 默认密码
        factory.setVirtualHost("/");                // 默认虚拟主机

        // 创建连接和通道
        Connection connection=factory.newConnection();
        channel=connection.createChannel();

        // 声明队列和交换机
        channel.queueDeclare(queueName,true,false,false,null);
    }

    /**
     * 消息生产者
     * 创建一个方法来发送消息
     */
    public static void sendMessage(String message) throws IOException
    {
        channel.basicPublish("", messageQueueName,null,message.getBytes("UTF-8"));
    }

    /**
     *消息生产者
     * 创建一个方法来发送控制信号
     */
    public static void sendControl(String control) throws IOException
    {
        channel.basicPublish("", controlQueueName,null,control.getBytes("UTF-8"));
    }

    /**
     * 消息消费者
     * 创建一个消费者来监听队列中的消息
     */
    public static void createConsumer(String queueName) throws IOException
    {
        DeliverCallback deliverCallback=(consumerTag,delivery)->
        {
            // 获取消息队列
            message=new String(delivery.getBody(),"UTF-8");

            if(hostMark==0)
            {
                System.out.println(message);

                if(message==previousMessage)
                {
                    GameWindow.setPauseRun(true);
                }
                else
                {
                    GameWindow.setPauseRun(false);

                    // 写入对应文件
                    Message.splitJsonFiles(message);

                    // 加载同步资源
                    if(GameFrame.getMyTank1()!=null)
                    {
                        // 清空我方坦克的子弹
                        GameFrame.getMyTank2().getBullets().clear();

                        GameSaverLoader.loadGameData();
                    }

                    previousMessage=message;
                }
            }
            else
            {
                gameControlList.add(Integer.parseInt(message));
                try
                {
                    hostRunFromClient();
                }
                catch (TimeoutException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };

        // 启动一个消费者从指定队列自动接收消息
        channel.basicConsume(queueName,true,deliverCallback, consumerTag->{});
        Runnable consumeTask=()->
        {
            try
            {
                channel.basicGet(queueName,true);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        };

        executorService.scheduleAtFixedRate(consumeTask,0, 30, TimeUnit.MILLISECONDS);
    }

    /**
     * 关闭通道和连接
     */
    public static void shutDown() throws IOException, TimeoutException
    {
        channel.close();
        channel.getConnection().close();
    }

    /**
     * 主从机判断
     */
    public static boolean isHost() throws FileNotFoundException
    {
        Scanner scanner= new Scanner(new File(FILE_PATH_IS_HOST));
        String isHostString=scanner.nextLine();
        scanner.close();
        return isHostString.equals("false") ? true : false;
    }

    /**
     * 设置主机标识
     */
    public static void setHost() throws IOException
    {
        FileWriter writer=new FileWriter(FILE_PATH_IS_HOST);
        writer.write("true");
        // 刷新缓存区
        writer.flush();
        writer.close();
    }

    /**
     * 修改主机标识
     */
    public static void clearHost() throws IOException
    {
        FileWriter writer=new FileWriter(FILE_PATH_IS_HOST);
        writer.write("false");
        // 刷新缓存区
        writer.flush();
        writer.close();
    }

    /**
     * 主从机启动
     */
    public static void startHostAndClient() throws IOException
    {
        // 第一个程序打开，成为主机
        if(isHost())
        {
            System.out.println("host");

            // 主机标识符设置为1
            hostMark=1;

            setHost();
            runHostLogic();
        }
        // 后续程序打开，成为从机
        else
        {
            System.out.println("client");

            // 主机标识符设置为0
            hostMark=0;

            // 从机逻辑
            runClientLogic();
        }
    }

    /**
     * 主机逻辑
     */
    private static void runHostLogic() throws IOException
    {
        // 新建显示消息队列
        startGameServer(messageQueueName);

        // 获取从机控制信息
        createConsumer(controlQueueName);
    }

    /**
     * 从机逻辑
     */
    private static void runClientLogic() throws IOException
    {
        // 新建控制消息队列
        startGameServer(controlQueueName);

        // 连接主机
        connectToHostMessage(messageQueueName);
    }

    /**
     * 启动游戏服务器
     */
    private static void startGameServer(String queueName)
    {
        try
        {
            // 建立工厂
            if(channel==null)
            {
                createConnectionFactory(queueName);
            }
        }
        catch (IOException | TimeoutException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 连接到主机——显示信息
     */
    private static void connectToHostMessage(String message) throws IOException
    {
        // 创建消费者
        createConsumer(message);
    }

    /**
     * 主机响应从机的控制的方法
     */
    private static void hostRunFromClient() throws IOException, TimeoutException
    {
        // 倒数第一个数字
        int firstElement=gameControlList.get(gameControlList.size()-1);
        // 倒数第二个数字
        int secondElement=gameControlList.get(gameControlList.size()-2);

        // 暂停
        if(firstElement== KeyEvent.VK_ESCAPE)
        {
            GameFrame.setGameState(Constant.STATE_MENU);
            GameFrame.setMenuIndex(1);
            GameFrame.setPauseTime(0);
            GameFrame.setStartPauseTime(System.currentTimeMillis());
        }
        // 松开按键坦克停止移动
        else if(firstElement==0 && secondElement!=0)
        {
            GameFrame.getMyTank2().setState(Tank.STATE_STAND);
        }
        // 坦克移动
        else if(firstElement!=secondElement)
        {
            KeyControl.keyPressedEventRun(firstElement,GameFrame.getMyTank2());
        }
    }

    public static int getHostMark() {
        return hostMark;
    }

    public static int getGameControl() {
        return gameControl;
    }

    public static void setGameControl(int gameControl) {
        RabbitMq.gameControl = gameControl;
    }
}
