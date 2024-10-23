package com.game.tank;

import com.game.entity.GameFrame;
import com.game.map.MapTile;
import com.game.mq.RabbitMq;
import com.game.pool.EnemyTanksPool;
import com.game.util.Constant;
import com.game.util.Tools;

import java.awt.*;
import java.util.List;

import static com.game.util.Constant.FRAME_HEIGHT;

/**
 * 敌方坦克类
 */
public class EnemyTank extends Tank
{
    // 记录5秒开始的时间
    private long aiTime;

    // 绘制坦克图片
    private static final Image[] enemyImg;

    public EnemyTank()
    {
        aiTime=System.currentTimeMillis();
    }

    // 用于创建一个敌人的坦克
    public static Tank createEnemy(List<MapTile> tiles)
    {
        // 全地图随机生成一辆坦克
        int x;
        int y;
        // 坦克不能与地图重叠也不能与我方坦克重叠
        Tank enemy= EnemyTanksPool.getTank();
        do
        {
            x=Tools.getRandomNumber(Constant.MIN_X,Constant.MAX_X);
            y=Tools.getRandomNumber(Constant.MIN_Y,Constant.MAX_Y);
            enemy.setX(x);
            enemy.setY(y);
        }while(Tools.isOverlapTankAndTile(tiles,x,y) ||
                Tools.isOverlapEnemyAndMyTank(enemy, GameFrame.getMyTank1(),MapTile.tileWidth*2) ||
                Tools.isOverlapEnemyAndMyTank(enemy, GameFrame.getMyTank2(),MapTile.tileWidth*2) ||
                y>FRAME_HEIGHT-Tank.RADIUS*7);
        int dir=Tools.getRandomNumber(DIR_W, DIR_D+1);

        enemy.setEnemy(true);
        enemy.setX(x);
        enemy.setY(y);
        enemy.setDir(dir);
        enemy.setState(STATE_MOVE);
        enemy.setColor(Tools.getEnemyColor());
        enemy.setHp(Tank.DEFAULT_HP);

        return enemy;
    }

    @Override
    public void drawImgTank(Graphics g)
    {
        enemyAI();
        g.drawImage(enemyImg[getDir()],getX()-RADIUS,getY()-RADIUS,null);
    }

    /**
     * 敌人的AI
     */
    private void enemyAI()
    {
        // 从机停止敌人的AI行为
        if(RabbitMq.getHostMark()==0)
        {
            return;
        }

        // 间隔5秒随机一个状态
        if (System.currentTimeMillis() - this.aiTime > Constant.ENEMY_AI_INTERVAL)
        {
            this.setDir(Tools.getRandomNumber(DIR_W, DIR_D+1));
            this.setState(Tools.getRandomNumber(0, 2) == 0 ? STATE_STAND : STATE_MOVE);
            this.aiTime = System.currentTimeMillis();
        }

        // 以很小的概率开火
        if (Math.random() < Constant.ENEMY_FIRE_PERCENT)
        {
            this.fire();
        }
    }

    // 在静态代码块中进行初始化
    static
    {
        enemyImg=new Image[4];
        enemyImg[0]=Tools.createImage("res/images/ul.png");
        enemyImg[1]=Tools.createImage("res/images/dl.png");
        enemyImg[2]=Tools.createImage("res/images/ll.png");
        enemyImg[3]=Tools.createImage("res/images/rl.png");
    }
}
