package com.game.tank;

import com.game.util.Tools;

import java.awt.*;

/**
 * 己方坦克类
 */
public class MyTank extends Tank
{
    // 绘制坦克图片
    private static final Image[] tankImg;
    // 在静态代码块中进行初始化
    static
    {
        tankImg=new Image[4];
        tankImg[0]=Tools.createImage("res/images/u.png");
        tankImg[1]=Tools.createImage("res/images/d.png");
        tankImg[2]=Tools.createImage("res/images/l.png");
        tankImg[3]=Tools.createImage("res/images/r.png");
    }

    public MyTank(int x, int y, int dir)
    {
        super(x, y, dir);
    }

    @Override
    public void drawImgTank(Graphics g)
    {
        g.drawImage(tankImg[getDir()],getX()-RADIUS,getY()-RADIUS,null);
    }
}
