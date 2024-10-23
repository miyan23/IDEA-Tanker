package com.game.map;

import com.game.entity.Bullet;
import com.game.pool.BulletsPool;
import com.game.util.Tools;

import java.awt.*;
import java.util.List;

/**
 * 地图元素块
 */
public class MapTile
{
    // 砖块的3中类型
    public static final int TYPE_BLOCK=0;       // 普通
    public static final int TYPE_COVER=1;       // 隐藏
    public static final int TYPE_HARD=2;        // 坚固
    public static int tileWidth=40;             // 砖块宽高
    public static int tileRadius=tileWidth/2;   // 砖块半径
    private int type;                           // 砖块类型
    private boolean isVisible=true;             // 可见性
    // 图片资源的中心
    private int x;
    private int y;

    // 加载图片资源
    private static final Image[] tileImg;
    static
    {
        tileImg=new Image[3];
        tileImg[TYPE_BLOCK]= Tools.createImage("res/images/tile.png");
        tileImg[TYPE_COVER]= Tools.createImage("res/images/cover.png");
        tileImg[TYPE_HARD]= Tools.createImage("res/images/hard.png");
        if(tileWidth<=0)
        {
            tileWidth=tileImg[TYPE_BLOCK].getWidth(null);
        }
    }

    /**
     * 绘制地图块
     */
    public void draw(Graphics g)
    {
        if(!isVisible)
        {
            return;
        }

        if(tileWidth<=0)
        {
            tileWidth=tileImg[TYPE_BLOCK].getWidth(null);
        }

        g.drawImage(tileImg[type],x-tileWidth/2,y-tileWidth/2,null);
    }

    /**
     * 砖块和子弹的碰撞
     */
    public boolean isCollideBullet(List<Bullet> bullets)
    {
        if(!isVisible || type==TYPE_COVER)
        {
            return false;
        }

        for (Bullet bullet : bullets)
        {
            int bulletX=bullet.getX();
            int bulletY=bullet.getY();
            boolean collide=Tools.isCollide(x,y,tileRadius,bulletX,bulletY);
            if(collide)
            {
                // 子弹销毁
                bullet.setVisible(false);
                BulletsPool.returnBullet(bullet);
                return true;
            }
            return false;
        }
        return false;
    }

    public MapTile() {}

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

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
}
