package com.game.controller;

import com.game.entity.GameFrame;
import com.game.mq.RabbitMq;
import com.game.tank.Tank;
import com.game.view.GameWindow;

import java.awt.event.*;

/**
 * 所有的鼠标控制
 */
public class MouseControl
{
    /**
     * 初始化窗口监听事件
     */
    public static void initMouseEventListener(GameWindow gameWindow)
    {
        if(RabbitMq.getHostMark()==0)
        {
            return;
        }

        // 注册监听事件
        gameWindow.addWindowListener(new WindowAdapter() {});

        // 添加鼠标监听事件
        gameWindow.addMouseListener(new MouseAdapter()
        {
            // 鼠标按下事件
            @Override
            public void mousePressed(MouseEvent e)
            {
                int button=e.getButton();
                Tank myTank=GameFrame.getMyTank1();

                if(button==MouseEvent.BUTTON1)
                {
                    // 根据鼠标的相对位置获得坦克的移动方向
                    int dir=getDirMouseToMyTank(e.getX(),e.getY());
                    myTank.setDir(dir);
                    myTank.setState(Tank.STATE_MOVE);
                }
            }

            // 鼠标松开事件
            @Override
            public void mouseReleased(MouseEvent e)
            {
                int button=e.getButton();
                Tank myTank=GameFrame.getMyTank1();

                if(button==MouseEvent.BUTTON1)
                {
                    myTank.setState(Tank.STATE_STAND);
                }
            }

            // 鼠标点击事件
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int button=e.getButton();
                Tank myTank=GameFrame.getMyTank1();

                if(button==MouseEvent.BUTTON3 && myTank.isMyTankFire())
                {
                    myTank.fire();
                }
            }
        });
    }

    /**
     * 求鼠标对于己方坦克的相对位置
     */
    private static int getDirMouseToMyTank(int x,int y)
    {
        Tank myTank=GameFrame.getMyTank1();
        int myTankX=myTank.getX();
        int myTankY=myTank.getY();

        int lengthX=x-myTankX;
        int lengthY=y-myTankY;

        if(Math.abs(lengthX)>=Math.abs(lengthY))
        {
            if(lengthX>0)
            {
                return Tank.DIR_D;
            }
            else
            {
                return Tank.DIR_A;
            }
        }
        else
        {
            if(lengthY>0)
            {
                return Tank.DIR_S;
            }
            else
            {
                return Tank.DIR_W;
            }
        }
    }
}
