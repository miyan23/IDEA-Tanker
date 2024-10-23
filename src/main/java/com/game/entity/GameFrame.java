package com.game.entity;

import com.game.controller.KeyControl;
import com.game.controller.MouseControl;
import com.game.keep.GameSaverLoader;
import com.game.map.GameMap;
import com.game.mq.RabbitMq;
import com.game.tank.MyTank;
import com.game.tank.Tank;
import com.game.thread.ThreadTasks;
import com.game.thread.ThreadsPool;
import com.game.view.GameWindow;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static com.game.util.Constant.*;

/**
 * 游戏显示框架
 */
public class GameFrame implements Serializable
{
    // 第一次使用的时候加载
    private static Image overImg=null;

    // 定义一张和屏幕大小一致的图片
    private static final BufferedImage bufferedImage=new BufferedImage(FRAME_WIDTH,FRAME_HEIGHT,BufferedImage.TYPE_4BYTE_ABGR);

    // 游戏窗口上边栏高度
    public static int titleBarHeight;

    // 游戏状态
    private static int gameState;

    // 菜单选项
    private static int menuIndex;

    // 定义坦克对象
    private static Tank myTank1;
    private static Tank myTank2;

    // 敌人的坦克容器
    private static final List<Tank> enemies= Collections.synchronizedList(new ArrayList<>());

    // 定义地图相关的内容
    private static GameMap gameMap;

    // 新建一个窗口
    private static GameWindow gameWindow;

    // 游戏运行的开始时间
    private static long startRunTime;

    // 游戏运行时间
    private static long runTime;

    // 游戏开始暂停时长
    private static long startPauseTime;

    // 游戏暂停时长
    private static long pauseTime;

    // 线程池
    private static ThreadsPool threadsPool;

    /**
     * 窗口初始化
     */
    public GameFrame() throws IOException, TimeoutException
    {
        // 线程池初始化
        threadsPool=new ThreadsPool();

        // 窗口初始化
        gameWindow=new GameWindow();

        // 按键事件初始化
        KeyControl.initKeyEventListener(gameWindow);

        // 启动主从机
        RabbitMq.startHostAndClient();
    }

    /**
     * 开始新游戏的方法
     */
    public static void newGame()
    {
        if(myTank1 !=null)
        {
            resetGame();
            ThreadTasks.shutThreadsPool();
        }

        // 鼠标事件初始化
        MouseControl.initMouseEventListener(gameWindow);

        // 初始化某些属性
        gameState=STATE_RUN;
        startRunTime=System.currentTimeMillis();
        runTime=0;
        pauseTime=0;

        // 创建坦克对象
        myTank1 =new MyTank(FRAME_WIDTH/3,FRAME_HEIGHT-Tank.RADIUS*2, Tank.DIR_W);
        myTank2 =new MyTank(FRAME_WIDTH/3*2,FRAME_HEIGHT-Tank.RADIUS*2, Tank.DIR_W);
        // 创建地图对象
        gameMap=new GameMap();

        // 从机不执行线程任务
        if(RabbitMq.getHostMark()==0)
        {
            return;
        }

        // 向线程池提交任务
        ThreadTasks.submitAllTasks();
    }

    /**
     * 继续游戏的方法
     */
    public static void continueGame()
    {
        gameState=STATE_RUN;
        threadsPool=new ThreadsPool();

        // 游戏暂停处理
        if(myTank1 !=null)
        {
            Timing.countPauseTime();
            startRunTime=startRunTime+pauseTime*1000;
        }
        // 游戏存档处理
        else
        {
            MouseControl.initMouseEventListener(gameWindow);

            myTank1 =new MyTank(FRAME_WIDTH/3,FRAME_HEIGHT-Tank.RADIUS*2, Tank.DIR_W);
            gameMap=new GameMap();

            // 加载之前的数据
            GameSaverLoader.loadGameData();

            startRunTime=System.currentTimeMillis()-runTime*1000;
        }

        // 重新提交任务
        ThreadTasks.submitAllTasks();
    }

    /**
     * 游戏结束
     * 重置属性
     */
    public static void resetGame()
    {
        menuIndex= 0;

        // 己方坦克子弹归还对象池
        myTank1.bulletsReturn();
        // 销毁己方坦克
        myTank1 =null;

        // 敌方坦克子弹归还对象池
        for (Tank enemy : enemies)
        {
            enemy.bulletsReturn();
        }
        // 销毁敌方坦克
        enemies.clear();

        // 清空地图资源
        gameMap.getTiles().clear();

        // 分数清零
        Score.setScore(0);
    }

    public static void setGameState(int gameState)
    {
        GameFrame.gameState = gameState;
    }

    public static int getGameState()
    {
        return gameState;
    }

    public static int getMenuIndex() {
        return menuIndex;
    }

    public static void setMenuIndex(int menuIndex) {
        GameFrame.menuIndex = menuIndex;
    }

    public static Tank getMyTank1() {
        return myTank1;
    }

    public static List<Tank> getEnemies() {
        return enemies;
    }

    public static BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public static GameMap getGameMap() {
        return gameMap;
    }

    public static Image getOverImg() {
        return overImg;
    }

    public static void setOverImg(Image overImg) {
        GameFrame.overImg = overImg;
    }

    public static long getRunTime() {
        return runTime;
    }

    public static void setRunTime(long runTime) {
        GameFrame.runTime = runTime;
    }

    public static void setStartPauseTime(long startPauseTime) {
        GameFrame.startPauseTime = startPauseTime;
    }

    public static void setPauseTime(long pauseTime) {
        GameFrame.pauseTime = pauseTime;
    }

    public static ThreadsPool getThreadsPool() {
        return threadsPool;
    }

    public static void setThreadsPool(ThreadsPool threadsPool) {GameFrame.threadsPool = threadsPool;}

    public static long getStartRunTime() {return startRunTime;}

    public static long getStartPauseTime() {return startPauseTime;}

    public static Tank getMyTank2() {return myTank2;}
}
