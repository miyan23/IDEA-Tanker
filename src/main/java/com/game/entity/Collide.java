package com.game.entity;

import com.game.map.GameMap;
import com.game.tank.Tank;
import com.game.util.Tools;

import java.util.List;

/**
 * 所有的碰撞处理
 */
public class Collide
{
    /**
     * 己方坦克和所有敌人坦克子弹的碰撞
     * 敌方坦克和所有己方坦克子弹的碰撞
     */
    public static void bulletCollideTank()
    {
        List<Tank> enemies=GameFrame.getEnemies();
        Tank myTank1=GameFrame.getMyTank1();
        Tank myTank2=GameFrame.getMyTank2();

        // 敌方坦克和所有己方坦克子弹的碰撞
        for (Tank enemy : enemies)
        {
            enemy.collideBullets(myTank1.getBullets());
            enemy.collideBullets(myTank2.getBullets());
        }

        // 己方坦克和所有敌人坦克子弹的碰撞
        for (Tank enemy : enemies)
        {
            myTank1.collideBullets(enemy.getBullets());
            myTank2.collideBullets(enemy.getBullets());
        }
    }

    /**
     * 所有子弹和砖块的碰撞
     * 所有坦克和地图的碰撞
     */
    public static void bulletCollideMapTile()
    {
        List<Tank> enemies=GameFrame.getEnemies();
        Tank myTank1=GameFrame.getMyTank1();
        Tank myTank2=GameFrame.getMyTank2();

        GameMap gameMap=GameFrame.getGameMap();

        // 己方坦克子弹和砖块的碰撞
        myTank1.bulletsCollideMapTiles(gameMap.getTiles());
        myTank2.bulletsCollideMapTiles(gameMap.getTiles());

        // 敌方坦克子弹和砖块的碰撞
        for (Tank enemy : enemies)
        {
            enemy.bulletsCollideMapTiles(gameMap.getTiles());
        }

        // 己方坦克和地图的碰撞
        if(myTank1.isCollideTile(gameMap.getTiles()))
        {
            myTank1.back();
            myTank1.setState(Tank.STATE_STAND);
        }
        if(myTank2.isCollideTile(gameMap.getTiles()))
        {
            myTank2.back();
            myTank2.setState(Tank.STATE_STAND);
        }

        // 敌方坦克和地图的碰撞
        for (Tank enemy : enemies)
        {
            if(enemy.isCollideTile(gameMap.getTiles()))
            {
                enemy.back();
                enemy.setState(Tank.STATE_STAND);
            }
        }

        // 清理所有被销毁的地图块
        gameMap.clearDestroyTiles();
    }

    /**
     * 己方坦克和敌方坦克的碰撞
     */
    public static void myTankCollideEnemy()
    {
        List<Tank> enemies=GameFrame.getEnemies();
        Tank myTank1=GameFrame.getMyTank1();
        Tank myTank2=GameFrame.getMyTank2();

        for (Tank enemy : enemies)
        {
            if(Tools.isOverlapMyTankAndEnemy(enemy,myTank1))
            {
                enemy.back();
                myTank1.back();
            }
            if(Tools.isOverlapMyTankAndEnemy(enemy,myTank2))
            {
                enemy.back();
                myTank2.back();
            }
        }
    }
}
