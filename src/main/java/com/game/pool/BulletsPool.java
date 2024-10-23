package com.game.pool;

import com.game.entity.Bullet;

import java.util.ArrayList;
import java.util.List;

/**
 * 子弹池类
 */
public class BulletsPool
{
    public static final int DEFAULT_POOL_SIZE=200;
    public static final int DEFAULT_POOL_MAX_SIZE=300;

    // 用于保存所有的子弹的容器
    private static final List<Bullet> pool=new ArrayList<>();

    // 在类加载的时候创建200个子弹对象添加到容器中
    static
    {
        for(int i=0;i<DEFAULT_POOL_SIZE;i++)
        {
            pool.add(new Bullet());
        }
    }

    /**
     * 从子弹池获得子弹
     */
    public static Bullet getBullet()
    {
        Bullet bullet;
        // 池塘已空
        if(pool.size()==0)
        {
            bullet=new Bullet();
        }
        // 池塘中还有对象，拿走第一个位置的子弹对象
        else
        {
            bullet=pool.remove(0);
        }
        return bullet;
    }

    /**
     * 子弹被销毁的时候，归还到池塘中来
     */
    public static void returnBullet(Bullet bullet)
    {
        // 池塘中子弹的个数达到最大值,不再归还
        if(pool.size()==DEFAULT_POOL_MAX_SIZE)
        {
            return;
        }
        pool.add(bullet);
    }
}
