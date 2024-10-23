package com.game.pool;

import com.game.tank.EnemyTank;
import com.game.tank.Tank;

import java.util.ArrayList;
import java.util.List;

/**
 * 敌人的坦克池
 */
public class EnemyTanksPool
{
    public static final int DEFAULT_POOL_SIZE=5;
    public static final int DEFAULT_POOL_MAX_SIZE=5;

    private static final List<Tank> pool=new ArrayList<>();

    static
    {
        for(int i=0;i<DEFAULT_POOL_SIZE;i++)
        {
            pool.add(new EnemyTank());
        }
    }

    public static Tank getTank()
    {
        Tank tank;
        // 池塘已空
        if(pool.size()==0)
        {
            tank=new EnemyTank();
        }
        else
        {
            tank=pool.remove(0);
        }
        return tank;
    }

    public static void returnTank(Tank tank)
    {
        // 池塘中子弹的个数达到最大值,不再归还
        if(pool.size()==DEFAULT_POOL_MAX_SIZE)
        {
            return;
        }
        pool.add(tank);
    }
}
