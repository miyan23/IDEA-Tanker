package com.game.util;

import com.game.entity.GameFrame;
import com.game.map.MapTile;
import com.game.mq.RabbitMq;
import com.game.tank.Tank;
import com.game.thread.ThreadTasks;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

/**
 * 工具类
 */
public class Tools
{
    private Tools(){}

    /**
     * 得到指定区间的随机数
     * @param min   区间最小值，包含
     * @param max   区间最大值。不包含
     * @return      随机数
     */
    public static int getRandomNumber(int min,int max)
    {
        return (int)(Math.random()*(max-min)+min);
    }

    /**
     * 己方坦克炮弹的颜色
     */
    public static Color getMyColor()
    {
        return Constant.OWN_COLOR;
    }

    /**
     * 敌方坦克炮弹的颜色
     */
    public static Color getEnemyColor()
    {
        return Constant.ENEMY_COLOR;
    }

    /**
     * 判断一个点是否在某一个正方形内部
     * @param recX          正方形中心点的x坐标
     * @param recY          正方形中心点的y坐标
     * @param radius        正方形边长的一半
     * @param pointX        点的x坐标
     * @param pointY        点的y坐标
     * @return      若点在正方形内部，返回true，否则返回false
     */
    public static boolean isCollide(int recX,int recY,int radius,int pointX,int pointY)
    {
        // 正方形中心点和点的x y轴距离
        int disX=Math.abs(recX-pointX);
        int disY=Math.abs(recY-pointY);

        return disX < radius && disY < radius;
    }

    /**
     * 根据图片的资源路径创建加载图片对象
     */
    public static Image createImage(String path)
    {
        return Toolkit.getDefaultToolkit().createImage(path);
    }

    /**
     * 判断砖块和砖块重叠或砖块和坦克重叠
     */
    public static boolean isOverlapTankAndTile(List<MapTile> tiles, int x, int y)
    {
        for (MapTile tile : tiles)
        {
            int tileX=tile.getX();
            int tileY=tile.getY();

            if(Math.abs(tileX-x)<MapTile.tileWidth && Math.abs(tileY-y)<MapTile.tileWidth)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断一个砖块和所有坦克重叠
     */
    public static boolean isOverlapTileAndTank(List<Tank> enemyTanks, Tank myTank, int x, int y)
    {
        for (Tank enemyTank : enemyTanks)
        {
            int enemyX=enemyTank.getX();
            int enemyY=enemyTank.getY();

            if(Math.abs(enemyX-x)<MapTile.tileWidth*2 && Math.abs(enemyY-y)<MapTile.tileWidth*2)
            {
                return true;
            }
        }

        return Math.abs(myTank.getX()-x)<MapTile.tileWidth*2 && Math.abs(myTank.getY()-y)<MapTile.tileWidth*2;
    }

    /**
     * 判断己方坦克和敌方坦克碰撞——游戏中
     */
    public static boolean isOverlapMyTankAndEnemy(Tank enemy, Tank myTank)
    {
        return Math.abs(enemy.getX()-myTank.getX())<MapTile.tileWidth && Math.abs(enemy.getY()-myTank.getY())<MapTile.tileWidth;
    }

    /**
     * 生成敌方坦克时判断其不能靠近我方坦克一定范围内
     */
    public static boolean isOverlapEnemyAndMyTank(Tank enemy, Tank myTank,int width)
    {
        return Math.abs(enemy.getX()-myTank.getX())<width && Math.abs(enemy.getY()-myTank.getY())<width;
    }

    /**
     * 游戏结束
     */
    public static void gameOver()
    {
        // 两个己方坦克都被毁则游戏结束
        if(GameFrame.getMyTank1().getHp()==0 && GameFrame.getMyTank2().getHp()==0)
        {
            // 游戏结束
            GameFrame.setGameState(Constant.STATE_OVER);
            // 关闭线程池
            ThreadTasks.shutThreadsPool();
        }
    }

    /**
     * 主机关闭时从机也关闭
     */
    public static void closeHostAndClient() throws FileNotFoundException
    {
        Scanner scanner= new Scanner(new File(RabbitMq.FILE_PATH_IS_HOST));
        String isHostString=scanner.nextLine();
        scanner.close();

        if(GameFrame.getGameState()==Constant.STATE_RUN && isHostString.equals("false"))
        {
            System.exit(0);
        }
    }
}
