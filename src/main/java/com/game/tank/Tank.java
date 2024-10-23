package com.game.tank;

import com.game.entity.Bullet;
import com.game.entity.GameFrame;
import com.game.entity.Score;
import com.game.map.MapTile;
import com.game.pool.BulletsPool;
import com.game.pool.EnemyTanksPool;
import com.game.pool.MapTilesPool;
import com.game.util.Constant;
import com.game.util.Tools;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 坦克类
 */
public abstract class Tank
{
    public static final int RADIUS=20;              // 半径
    public static final int DEFAULT_SPEED=4;        // 默认速度-每帧30ms
    public static final int DEFAULT_HP=100;        // 初始生命
    public static final int ATK_DEFAULT=30;         // 默认攻击力

    // 四个方向
    public static final int DIR_W=0;
    public static final int DIR_S=1;
    public static final int DIR_A=2;
    public static final int DIR_D=3;

    // 坦克的状态
    public static final int STATE_STAND=0;      // 站立
    public static final int STATE_MOVE=1;       // 行走
    public static final int STATE_DEAD=2;       // 死亡

    private int x;                      // 坐标
    private int y;                      // 坐标
    private int hp=DEFAULT_HP;          // 血量
    private final int atk;                    // 攻击力
    private final int speed;                  //速度
    private int dir;                    // 方向
    private int state=STATE_STAND;      // 状态
    private Color color;                // 颜色
    private boolean isEnemy=false;      // 敌友标识
    private final BloodBar bloodBar=new BloodBar();       // 血条

    // 上次移动后的坐标
    private int oldX=-1;
    private int oldY=-1;

    // 炮弹
    private final List<Bullet> bullets= Collections.synchronizedList(new ArrayList<>());

    /**
     * 绘制坦克
     */
    public void draw(Graphics g)
    {
        // 坦克状态处理
        logic();

        // 坦克绘制
        drawImgTank(g);

        // 血条绘制
        bloodBar.draw(g);
    }

    /**
     * 使用图片的方式绘制坦克
     */
    public abstract void drawImgTank(Graphics g);

    /**
     * 坦克的逻辑处理
     */
    private void logic()
    {
        switch(state)
        {
            case STATE_MOVE:
                move();
            case STATE_STAND:
            case STATE_DEAD:
            default:
        }
    }

    /**
     * 坦克移动的功能
     */
    private void move()
    {
        oldX=x;
        oldY=y;
        switch(dir)
        {
            case DIR_W ->
            {
                y-=speed;
                if(y<Constant.MIN_Y)
                {
                    y=Constant.MIN_Y;
                }
            }
            case DIR_S ->
            {
                y+=speed;
                if(y>Constant.MAX_Y)
                {
                    y=Constant.MAX_Y;
                }
            }
            case DIR_A ->
            {
                x-=speed;
                if(x<Constant.MIN_X)
                {
                    x=Constant.MIN_X;
                }
            }
            case DIR_D ->
            {
                x+=speed;
                if(x>Constant.MAX_X)
                {
                    x=Constant.MAX_X;
                }
            }
        }
    }

    /**
     * 坦克功能：开火
     * 创建一个子弹对象，其属性信息通过坦克的信息获得
     * 然后将创建的子弹添加到坦克管理的容器中
     */
    public void fire()
    {
        // 根据坦克坐标求出炮弹的初始坐标
        int bulletX=x;
        int bulletY=y;
        switch(dir)
        {
            case DIR_W -> bulletY-=RADIUS;
            case DIR_S -> bulletY+=RADIUS;
            case DIR_A -> bulletX-=RADIUS;
            case DIR_D -> bulletX+=RADIUS;
        }

        // 从对象池获得子弹
        Bullet bullet=BulletsPool.getBullet();
        bullet.setX(bulletX);
        bullet.setY(bulletY);
        bullet.setDir(dir);
        bullet.setAtk(atk);
        bullet.setColor(color);
        bullet.setVisible(true);

        bullets.add(bullet);
    }

    /**
     * 将当前坦克发射的所有子弹绘制出来
     */
    public void drawBullets(Graphics g)
    {
        synchronized (bullets)
        {
            for(Bullet bullet : bullets)
            {
                bullet.draw(g);
            }
        }

        // 遍历所有的子弹,将不可见的子弹移除,并归还对象池
        for (int i = 0; i < bullets.size(); i++)
        {
            Bullet bullet=bullets.get(i);
            if(!bullet.isVisible())
            {
                Bullet remove = bullets.remove(i);
                BulletsPool.returnBullet(remove);
                i--;
            }
        }
    }

    /**
     * 坦克销毁的时候处理
     * 归还所有的子弹
     */
    public void bulletsReturn()
    {
        for (Bullet bullet : bullets)
        {
            BulletsPool.returnBullet(bullet);
        }
        bullets.clear();
    }

    /**
     * 子弹和坦克碰撞的方法
     */
    public void collideBullets(List<Bullet> bullets)
    {
        // 遍历所有的子弹，依次和当前的坦克进行碰撞检测
        for (Bullet bullet : bullets)
        {
            // 子弹和坦克碰撞
            if(Tools.isCollide(x,y,RADIUS,bullet.getX(),bullet.getY()))
            {
                // 子弹消失
                bullet.setVisible(false);

                // 坦克受到伤害
                hurt(bullet);
            }
        }
    }

    /**
     * 子弹受伤害的方法
     */
    private void hurt(Bullet bullet)
    {
        hp-=bullet.getAtk();

        if(hp<0)
        {
            hp=0;
            die();
        }
    }

    /**
     * 坦克死亡
     */
    private void die()
    {
        // 敌人坦克被消灭，归还对象池
        if(isEnemy)
        {
            EnemyTanksPool.returnTank(this);
            Score.scoreCountEnemy();
        }
        // 己方坦克满血复活
        else
        {
            this.hp=Tank.DEFAULT_HP;
        }
    }

    /**
     * 判断当前坦克是否死亡
     */
    public boolean isDie()
    {
        return hp<=0;
    }

    /**
     * 给血条写一个内部类
     */
    class BloodBar
    {
        public static final int BAR_LENGTH=RADIUS*2;
        public static final int BAR_HEIGHT=5;

        public void draw(Graphics g)
        {
            // 填充底色
            g.setColor(Color.BLACK);
            g.fillRect(x-RADIUS,y-RADIUS-BAR_HEIGHT*2,BAR_LENGTH,BAR_HEIGHT);

            // 当前血量
            g.setColor(Color.RED);
            g.fillRect(x-RADIUS,y-RADIUS-BAR_HEIGHT*2,hp*BAR_LENGTH/DEFAULT_HP,BAR_HEIGHT);

            // 边框
            g.setColor(Color.WHITE);
            g.drawRect(x-RADIUS,y-RADIUS-BAR_HEIGHT*2,BAR_LENGTH,BAR_HEIGHT);
        }
    }

    /**
     * 坦克的子弹和所有砖块的碰撞方法
     */
    public void bulletsCollideMapTiles(List<MapTile> tiles)
    {
        for (MapTile tile : tiles)
        {
            if(tile.isCollideBullet(bullets))
            {
                // 地图hard块不能被击毁
                if(tile.getType()==MapTile.TYPE_HARD)
                {
                    continue;
                }
                // 设置地图块销毁
                tile.setVisible(false);
                // 归还对象池
                MapTilesPool.returnMapTile(tile);
            }
        }
    }

    /**
     * 所有砖块和当前坦克碰撞的方法
     * 从砖块中提取8个点，判断8个点和当前坦克位置
     * 从左上角开始顺时针旋转
     */
    public boolean isCollideTile(List<MapTile> tiles)
    {
        for (MapTile tile : tiles)
        {
            // 不可见或COVER块不做碰撞
            if(!tile.isVisible() || tile.getType()==MapTile.TYPE_COVER)
            {
                continue;
            }

            int tileX=tile.getX()-MapTile.tileRadius;
            int tileY=tile.getY()-MapTile.tileRadius;
            boolean collide=Tools.isCollide(x,y,RADIUS,tileX,tileY);
            if(collide)
            {
                return true;
            }
            tileX+=MapTile.tileRadius;
            collide=Tools.isCollide(x,y,RADIUS,tileX,tileY);
            if(collide)
            {
                return true;
            }
            tileX+=MapTile.tileRadius;
            collide=Tools.isCollide(x,y,RADIUS,tileX,tileY);
            if(collide)
            {
                return true;
            }
            tileY+=MapTile.tileRadius;
            collide=Tools.isCollide(x,y,RADIUS,tileX,tileY);
            if(collide)
            {
                return true;
            }
            tileY+=MapTile.tileRadius;
            collide=Tools.isCollide(x,y,RADIUS,tileX,tileY);
            if(collide)
            {
                return true;
            }
            tileX-=MapTile.tileRadius;
            collide=Tools.isCollide(x,y,RADIUS,tileX,tileY);
            if(collide)
            {
                return true;
            }
            tileX-=MapTile.tileRadius;
            collide=Tools.isCollide(x,y,RADIUS,tileX,tileY);
            if(collide)
            {
                return true;
            }
            tileY-=MapTile.tileRadius;
            collide=Tools.isCollide(x,y,RADIUS,tileX,tileY);
            if(collide)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 坦克撞上砖块回退的方法
     */
    public void back()
    {
        x=oldX;
        y=oldY;
    }

    /**
     * 己方坦克的开火控制
     */
    public boolean isMyTankFire()
    {
        int count=0;

        for (Bullet bullet : bullets)
        {
            if(bullet.getColor()==Constant.OWN_COLOR && bullet.isVisible())
            {
                if(GameFrame.getMyTank1().getDir()==DIR_W && GameFrame.getMyTank1().getY()-bullet.getY()<RADIUS*2)
                {
                    return false;
                }
                if(GameFrame.getMyTank1().getDir()==DIR_S && bullet.getY()-GameFrame.getMyTank1().getY()<RADIUS*2)
                {
                    return false;
                }
                if(GameFrame.getMyTank1().getDir()==DIR_A && GameFrame.getMyTank1().getX()-bullet.getX()<RADIUS*2)
                {
                    return false;
                }
                if(GameFrame.getMyTank1().getDir()==DIR_D && bullet.getX()-GameFrame.getMyTank1().getX()<RADIUS*2)
                {
                    return false;
                }

                count++;
                if(count>=Constant.OWN_MAX_BULLET)
                {
                    return false;
                }
            }
        }
        return true;
    }

    public Tank()
    {
        color= Tools.getMyColor();
        atk=ATK_DEFAULT;
        speed=DEFAULT_SPEED;
    }

    public Tank(int x, int y, int dir)
    {
        this.x = x;
        this.y = y;
        this.dir = dir;
        color= Tools.getMyColor();
        atk=ATK_DEFAULT;
        speed=DEFAULT_SPEED;
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

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getDir() {
        return dir;
    }

    public void setDir(int dir) {
        this.dir = dir;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public List getBullets() {return bullets;}

    public void setEnemy(boolean enemy) {
        isEnemy = enemy;
    }
}
