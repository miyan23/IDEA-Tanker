package com.game.controller;

import com.game.entity.Bullet;
import com.game.entity.GameFrame;
import com.game.keep.GameSaverLoader;
import com.game.mq.RabbitMq;
import com.game.tank.Tank;
import com.game.util.Constant;
import com.game.view.GameWindow;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static com.game.util.Constant.*;

/**
 * 所有的按键控制
 */
public class KeyControl
{
    /**
     * 初始化窗口的事件监听
     */
    public static void initKeyEventListener(GameWindow gameWindow)
    {
        // 注册监听事件
        gameWindow.addWindowListener(new WindowAdapter() {});

        // 添加按键监听事件
        gameWindow.addKeyListener(new KeyAdapter()
        {

            // 按键按下的时候回调
            @Override
            public void keyPressed(KeyEvent e)
            {
                // 获得被按下的键值
                int keyCode=e.getKeyCode();
                // 获得游戏状态
                int gameState=GameFrame.getGameState();

                // 根据不同的游戏状态，给出不同的处理方法
                switch (gameState)
                {
                    case Constant.STATE_MENU ->
                    {
                        try
                        {
                            keyPressedEventMenu(keyCode);
                        }
                        catch (IOException | TimeoutException ex)
                        {
                            throw new RuntimeException(ex);
                        }

                    }
                    case Constant.STATE_HELP -> keyPressedEventHelp();
                    case Constant.STATE_ABOUT -> keyPressedEventAbout();
                    case Constant.STATE_RUN ->
                    {
                        try
                        {
                            keyPressedEventRun(keyCode,GameFrame.getMyTank1());
                        }
                        catch (IOException ex)
                        {
                            throw new RuntimeException(ex);
                        }
                    }
                    case Constant.STATE_OVER -> keyPressedEventOver(keyCode);
                }
            }

            // 按键松开的时候回调
            @Override
            public void keyReleased(KeyEvent e)
            {
                if(RabbitMq.getHostMark()==1)
                {
                    int keyCode=e.getKeyCode();
                    int gameState=GameFrame.getGameState();

                    // 只处理坦克运行时的按键松开事件
                    if(gameState==STATE_RUN)
                    {
                        keyReleasedEventRun(keyCode);
                    }
                }
                else
                {
                    RabbitMq.setGameControl(0);

                    String gameControl = String.valueOf(RabbitMq.getGameControl());
                    try
                    {
                        RabbitMq.sendControl(gameControl);
                    }
                    catch (IOException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }

    /**
     * 菜单状态按键处理
     */
    private static void keyPressedEventMenu(int keyCode) throws IOException, TimeoutException
    {
        // 获得菜单值
        int menuIndex=GameFrame.getMenuIndex();

        switch (keyCode)
        {
            case KeyEvent.VK_UP, KeyEvent.VK_W ->
            {
                menuIndex = --menuIndex < 0 ? MENUS.length - 1 : menuIndex;
                GameFrame.setMenuIndex(menuIndex);
            }
            case KeyEvent.VK_DOWN, KeyEvent.VK_S ->
            {
                menuIndex = ++menuIndex > MENUS.length - 1 ? 0 : menuIndex;
                GameFrame.setMenuIndex(menuIndex);
            }
            case KeyEvent.VK_ENTER -> keyPressedEnter(menuIndex);
        }
    }

    /**
     * 菜单状态下Enter键的处理
     */
    private static void keyPressedEnter(int menuIndex) throws IOException, TimeoutException
    {
        switch (menuIndex)
        {
            case 0 -> GameFrame.newGame();
            case 1 -> GameFrame.continueGame();
            case 2 -> GameFrame.setGameState(STATE_HELP);
            case 3 -> GameFrame.setGameState(STATE_ABOUT);
            case 4 ->
            {
                // 修改主机标识文件
                RabbitMq.clearHost();

                // 关闭通道和连接
                RabbitMq.shutDown();

                GameSaverLoader.saveGameData();
                System.exit(0);
            }
        }
    }

    /**
     * 帮助状态按键处理
     */
    private static void keyPressedEventHelp()
    {
        GameFrame.setGameState(STATE_MENU);
    }

    /**
     * 关于状态按键处理
     */
    private static void keyPressedEventAbout()
    {
        GameFrame.setGameState(STATE_MENU);
    }

    /**
     * 运行状态按键处理
     * 改变坦克方向
     * 控制坦克移动
     */
    public static void keyPressedEventRun(int keyCode,Tank myTank) throws IOException
    {
        // 如果是从机，则直接发送控制信息
        if(RabbitMq.getHostMark()==0)
        {
            if(keyCode==KeyEvent.VK_ESCAPE)
            {
                GameWindow.setPauseRun(true);
            }

            // 反馈控制信息
            RabbitMq.setGameControl(keyCode);

            String gameControl = String.valueOf(RabbitMq.getGameControl());
            RabbitMq.sendControl(gameControl);

            return;
        }

        switch (keyCode)
        {
            case KeyEvent.VK_ESCAPE ->
            {
                GameFrame.setGameState(STATE_MENU);
                GameFrame.setMenuIndex(1);
                GameFrame.setPauseTime(0);
                GameFrame.setStartPauseTime(System.currentTimeMillis());
            }
            case KeyEvent.VK_UP, KeyEvent.VK_W ->
            {
                myTank.setDir(Tank.DIR_W);
                myTank.setState(Tank.STATE_MOVE);
            }
            case KeyEvent.VK_DOWN, KeyEvent.VK_S ->
            {
                myTank.setDir(Tank.DIR_S);
                myTank.setState(Tank.STATE_MOVE);
            }
            case KeyEvent.VK_LEFT, KeyEvent.VK_A ->
            {
                myTank.setDir(Tank.DIR_A);
                myTank.setState(Tank.STATE_MOVE);
            }
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D ->
            {
                myTank.setDir(Tank.DIR_D);
                myTank.setState(Tank.STATE_MOVE);
            }
            case KeyEvent.VK_SPACE ->
            {
                if (myTank.isMyTankFire())
                {
                    myTank.fire();
                }
            }
        }
    }

    /**
     * 结束状态按键处理
     */
    private static void keyPressedEventOver(int keyCode)
    {
        switch (keyCode)
        {
            // 游戏结束
            case KeyEvent.VK_ESCAPE -> System.exit(0);
            // 返回主菜单
            case KeyEvent.VK_ENTER ->
            {
                GameFrame.setGameState(STATE_MENU);
                GameFrame.resetGame();
            }
        }
    }

    /**
     * 按键松开则坦克站立
     */
    private static void keyReleasedEventRun(int keyCode)
    {
        switch (keyCode)
        {
            case KeyEvent.VK_UP,
                    KeyEvent.VK_W,
                    KeyEvent.VK_DOWN,
                    KeyEvent.VK_S,
                    KeyEvent.VK_LEFT,
                    KeyEvent.VK_A,
                    KeyEvent.VK_RIGHT,
                    KeyEvent.VK_D -> GameFrame.getMyTank1().setState(Tank.STATE_STAND);
        }

    }
}
