package com.game.thread;

import com.game.entity.Collide;
import com.game.entity.GameFrame;
import com.game.map.GameMap;
import com.game.mq.Message;
import com.game.mq.RabbitMq;
import com.game.tank.EnemyTank;
import com.game.tank.Tank;

import java.io.IOException;

import static com.game.util.Constant.*;

public class ThreadTasks
{
    /**
     * 向线程池提交任务
     */
    public static void submitAllTasks()
    {
        if(GameFrame.getThreadsPool().isShutdown())
        {
            GameFrame.setThreadsPool(new ThreadsPool());
        }

        // 开启碰撞检测任务
        collideCheckStartThreadSubmit();
        // 开启生成敌方坦克任务
        createEnemyThreadSubmit();
        // 开启新增地图块任务
        addBlockThreadSubmit();
        // 开启坦克检测任务
        checkEnemiesThreadSubmit();
        // 主机发送消息任务
        sendMessageThreadSubmit();
    }

    /**
     * 关闭线程池
     */
    public static void shutThreadsPool()
    {
        GameFrame.getThreadsPool().shutdown();
    }

    /**
     * 提交任务——生成敌方坦克
     */
    private static void createEnemyThreadSubmit()
    {
        GameFrame.getThreadsPool().submitTask(new Thread(()->
        {
            while(GameFrame.getGameState()==STATE_RUN)
            {
                if(GameFrame.getEnemies().size()<ENEMY_MAX_COUNT)
                {
                    Tank enemy= EnemyTank.createEnemy(GameFrame.getGameMap().getTiles());
                    GameFrame.getEnemies().add(enemy);
                }
                try
                {
                    Thread.sleep(ENEMY_BORN_INTERVAL);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }));
    }

    /**
     * 提交任务——碰撞检测
     */
    private static void collideCheckStartThreadSubmit()
    {
        GameFrame.getThreadsPool().submitTask(new Thread(() ->
        {
            while(GameFrame.getGameState()==STATE_RUN)
            {
                // 子弹和坦克的碰撞方法
                Collide.bulletCollideTank();

                // 子弹和所有的砖块碰撞
                Collide.bulletCollideMapTile();

                // 己方坦克和所有敌方坦克的碰撞
                Collide.myTankCollideEnemy();

                try
                {
                    Thread.sleep(REPAINT_INTERVAL);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }));
    }

    /**
     * 提交任务——新增地图块
     */
    private static void addBlockThreadSubmit()
    {
        GameFrame.getThreadsPool().submitTask(new Thread(()->
        {
            while(GameFrame.getGameState()==STATE_RUN)
            {
                GameMap.createBlock();

                try
                {
                    Thread.sleep(BLOCK_ADD_INTERVAL);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }));
    }

    /**
     * 提交任务——检测敌方坦克位置
     */
    private static void checkEnemiesThreadSubmit()
    {
        GameFrame.getThreadsPool().submitTask(new Thread(()->
        {
            while(GameFrame.getGameState()==STATE_RUN)
            {
                for (Tank enemy : GameFrame.getEnemies())
                {
                    if(enemy.getX()<MIN_X || enemy.getX()>MAX_X || enemy.getY()<MIN_Y || enemy.getY()>MAX_Y)
                    {
                        enemy.setHp(-1);
                    }
                }

                try
                {
                    Thread.sleep(REPAINT_INTERVAL);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }));
    }

    /**
     * 单线程任务——监控线程池
     */
    public static void monitorThreadSubmit()
    {
        (new Thread(() ->
        {
            while(true)
            {
                // 运行期间线程池意外关闭则重新打开
                if(GameFrame.getGameState()==STATE_RUN && GameFrame.getThreadsPool().isShutdown())
                {
                    // 先确保关闭了线程池
                    shutThreadsPool();
                    // 重新初始化线程池
                    GameFrame.setThreadsPool(new ThreadsPool());
                    // 重新提交任务
                    submitAllTasks();
                }

                try
                {
                    Thread.sleep(REPAINT_INTERVAL);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        })).start();
    }

    /**
     * 提交任务——主机发送显示消息
     */
    public static void sendMessageThreadSubmit()
    {
        GameFrame.getThreadsPool().submitTask(new Thread(()->
        {
            while(GameFrame.getGameState()==STATE_RUN)
            {
                try
                {
                    // 发送总体游戏信息
                    String gameMessage = Message.getGameMessage();
                    RabbitMq.sendMessage(gameMessage);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }

                try
                {
                    Thread.sleep(REPAINT_INTERVAL);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }));
    }
}
