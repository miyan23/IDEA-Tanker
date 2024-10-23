package com.game.view;

import com.game.entity.GameFrame;
import com.game.entity.Score;
import com.game.entity.Timing;
import com.game.mq.RabbitMq;
import com.game.tank.Tank;
import com.game.thread.ThreadTasks;
import com.game.util.Tools;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.List;

import static com.game.util.Constant.*;

/**
 * 游戏窗口
 */
public class GameWindow extends Frame implements Runnable
{
    // 游戏窗口上边栏高度
    public static int titleBarHeight;

    // 上一张绘制的图像
    private BufferedImage previousImage;

    // 暂停标志
    private static boolean pauseRun=false;

    /**
     * 窗口初始化
     */
    public GameWindow()
    {
        initWindow();

        // 启动刷新窗口的线程
        new Thread(this).start();

        if(RabbitMq.getHostMark()==0)
        {
            return;
        }

        // 开启监控线程池任务
        ThreadTasks.monitorThreadSubmit();
    }

    /**
     * 属性初始化
     */
    private void initWindow()
    {
        // 设置标题
        setTitle(GAME_TITLE);
        // 设置窗口大小
        setSize(FRAME_WIDTH,FRAME_HEIGHT);
        // 设置窗口左上角的坐标
        setLocation(FRAME_X,FRAME_Y);
        // 设置窗口大小不可改变
        setResizable(false);
        // 设置窗口可见
        setVisible(true);
        // 得到窗口上边栏高度
        titleBarHeight=this.getInsets().top;
    }

    /**
     * Frame类继承下来的方法
     * 负责所有绘制的内容，所有在屏幕显示的内容
     * 该方法不能主动调用，必须通过调用repaint()方法去回调该方法
     * @param g the specified Graphics window
     */
    public void update(Graphics g)
    {
        // 得到图片的画笔
        Graphics gImg= GameFrame.getBufferedImage().getGraphics();
        // 设置属性
        gImg.setFont(GAME_FONT);

        switch(GameFrame.getGameState())
        {
            case STATE_MENU -> drawMenu(gImg);
            case STATE_HELP -> drawHelp(gImg);
            case STATE_ABOUT -> drawAbout(gImg);
            case STATE_RUN ->
            {
                try
                {
                    if(pauseRun)
                    {
                        g.drawImage(previousImage,0,0,null);
                    }
                    else
                    {
                        drawRun(gImg);
                        previousImage=GameFrame.getBufferedImage();
                    }
                }
                catch (FileNotFoundException e)
                {
                    throw new RuntimeException(e);
                }
            }
            case STATE_OVER -> drawOver(gImg);
        }

        // 使用系统画笔将图片绘制到frame上
        g.drawImage(GameFrame.getBufferedImage(),0,0,null);
    }

    /**
     * 菜单绘制
     */
    private void drawMenu(Graphics g)
    {
        // 绘制黑色的背景
        g.setColor(Color.BLACK);
        g.fillRect(0,0,FRAME_WIDTH,FRAME_HEIGHT);

        final int STR_WIDTH=76;
        final int DIS=50;
        int x=FRAME_WIDTH-STR_WIDTH>>1;
        int y=FRAME_HEIGHT/3;
        g.setColor(Color.WHITE);
        for (int i = 0; i < MENUS.length; i++)
        {
            // 选中的菜单项的颜色设置为黄色
            if(i==GameFrame.getMenuIndex())
            {
                g.setColor(Color.YELLOW);
            }
            // 其它的为白色
            else
            {
                g.setColor(Color.WHITE);
            }

            g.drawString(MENUS[i],x,y+DIS*i);
        }
    }

    /**
     * 帮助绘制
     */
    private void drawHelp(Graphics g)
    {
        String help_1="游戏帮助";
        String help_2="通过方向键和W,A,S,D键控制坦克的四个行走方向,空格键开火.或者通过鼠标,长按左键则坦";
        String help_3="克移动,松开则停止,点击鼠标右键开火.普通的砖块可以击毁,钢铁块不能击毁.此无尽模式下";
        String help_4="己方坦克被击毁则游戏结束.";

        g.setColor(Color.BLACK);
        g.fillRect(0,0,FRAME_WIDTH,FRAME_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(GAME_FONT);
        g.drawString(help_1,350,180);

        g.setFont(OVER_FONT);
        g.drawString(help_2,60,260);
        g.drawString(help_3,60,300);
        g.drawString(help_4,60,340);
        drawReturnMenu(g);
    }

    /**
     * 关于绘制
     */
    private void drawAbout(Graphics g)
    {
        String about_1="游戏关于";
        String about_2="姓名：陈鑫宇";
        String about_3="学号：220211090903";
        String about_4="班级：软件工程2101";

        g.setColor(Color.BLACK);
        g.fillRect(0,0,FRAME_WIDTH,FRAME_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(GAME_FONT);
        g.drawString(about_1,350,180);

        g.setFont(OVER_FONT);
        g.drawString(about_2,340,260);
        g.drawString(about_3,340,300);
        g.drawString(about_4,340,340);
        drawReturnMenu(g);
    }

    /**
     * 返回主菜单绘制
     */
    private void drawReturnMenu(Graphics g)
    {
        String returnMenu="按下任意键返回主菜单";

        g.drawString(returnMenu,60,500);
   }

    /**
     * 运行绘制
     */
    public void drawRun(Graphics g) throws FileNotFoundException
    {
        // 绘制黑色的背景
        g.setColor(Color.BLACK);
        g.fillRect(0,0,FRAME_WIDTH,FRAME_HEIGHT);

        // 绘制子弹
        drawBullets(g);

        // 绘制地图的碰撞层
        GameFrame.getGameMap().drawBackground(g);

        // 绘制坦克
        drawEnemies(g);
        GameFrame.getMyTank1().draw(g);
        GameFrame.getMyTank2().draw(g);

        // 绘制地图的遮挡层
        GameFrame.getGameMap().drawCover(g);

        // 计时
        Timing.countRunTime();

        // 得分绘制
        drawScore(g);

        // 游戏结束
        Tools.gameOver();

        // 退出程序
        Tools.closeHostAndClient();
    }

    /**
     * 子弹绘制
     */
    private void drawBullets(Graphics g)
    {
        synchronized (GameFrame.getEnemies())
        {
            for (Tank enemy : GameFrame.getEnemies())
            {
                enemy.drawBullets(g);
            }
        }

        GameFrame.getMyTank1().drawBullets(g);
        GameFrame.getMyTank2().drawBullets(g);
    }

    /**
     * 结束绘制
     */
    private void drawOver(Graphics g)
    {
        if(GameFrame.getOverImg()==null)
        {
            GameFrame.setOverImg(Tools.createImage("res/images/over.jpg"));
        }
        
        int imgW=GameFrame.getOverImg().getWidth(null);
        int imgH=GameFrame.getOverImg().getHeight(null);

        g.drawImage(GameFrame.getOverImg(),FRAME_WIDTH-imgW>>1,FRAME_HEIGHT-imgH>>1,null);

        // 添加按键提示信息
        g.setColor(Color.WHITE);
        g.setFont(OVER_FONT);
        g.drawString(OVER_RETURN,20,FRAME_HEIGHT-40);
        g.drawString(OVER_QUIT,FRAME_WIDTH-210,FRAME_HEIGHT-40);
    }

    /**
     * 绘制敌方坦克
     */
    private void drawEnemies(Graphics g)
    {
        List<Tank> enemies=GameFrame.getEnemies();

        for (int i = 0; i < enemies.size(); i++)
        {
            Tank enemy=enemies.get(i);
            if(enemy.isDie())
            {
                enemies.remove(i);
                i--;
                continue;
            }
            enemy.draw(g);
        }
    }

    /**
     * 绘制显示运行时长和击毁坦克
     */
    private void drawScore(Graphics g)
    {
        long runTime=GameFrame.getRunTime();
        long minutes=runTime/60;
        long seconds=runTime-minutes*60;

        String runTimeString="运行时长："+minutes+"分"+seconds+"秒";
        String scoreString="击毁坦克："+ Score.getScore()+"辆";

        g.setColor(Color.RED);
        g.setFont(OVER_FONT);
        g.drawString(scoreString,20,560);
        g.drawString(runTimeString,20,580);
    }

    /**
     * 控制刷新率
     */
    @Override
    public void run()
    {
        GameFrame.getThreadsPool().submitTask(()->
        {
            while(true)
            {
                // 调用repaint()，回调update
                repaint();
                try
                {
                    Thread.sleep(REPAINT_INTERVAL);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static void setPauseRun(boolean pauseRun)
    {
        GameWindow.pauseRun = pauseRun;
    }
}
