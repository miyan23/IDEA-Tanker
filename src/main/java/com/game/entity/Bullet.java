package com.game.entity;

import com.game.tank.Tank;
import com.game.util.Constant;

import java.awt.*;

/**
 * 子弹类
 */
public class Bullet
{
    // 子弹的默认速度为坦克速度的2倍
    public static final int DEFAULT_SPEED= Tank.DEFAULT_SPEED*2;
    // 炮弹的半径
    public static final int RADIUS=4;

    private int x;                          // 坐标
    private int y;                          // 坐标
    private int speed;                      // 速度
    private int dir;                        // 方向
    private int atk;                        // 攻击力
    private Color color;                    // 颜色
    private boolean visible=true;           // 可见性

    /**
     * 炮弹自身的绘制方法
     */
    public void draw(Graphics g)
    {
        if(!visible)
        {
            return;
        }

        logic();

        g.setColor(color);
        g.fillOval(x-RADIUS,y-RADIUS,RADIUS*2,RADIUS*2);
    }

    /**
     * 子弹的逻辑
     */
    private void logic()
    {
        move();
    }

    /**
     * 子弹的移动
     */
    private void move()
    {
        switch (dir)
        {
            case Tank.DIR_W -> y -= speed;
            case Tank.DIR_S -> y += speed;
            case Tank.DIR_A -> x -= speed;
            case Tank.DIR_D -> x += speed;
        }


        // 子弹飞出屏幕则不可见
        if(x<0 || x> Constant.FRAME_WIDTH || y<0 || y>Constant.FRAME_HEIGHT)
        {
            visible=false;
        }
    }

    /**
     * 给对象池使用
     * 所有的属性均为默认值
     */
    public Bullet() {speed=DEFAULT_SPEED;}

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setDir(int dir) {
        this.dir = dir;
    }

    public int getAtk() {
        return atk;
    }

    public void setAtk(int atk) {
        this.atk = atk;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getDir() {
        return dir;
    }

    public void setSpeed(int speed) {this.speed = speed;}
}
