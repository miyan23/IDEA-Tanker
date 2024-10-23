package com.game.pool;

import com.game.map.MapTile;

import java.util.ArrayList;
import java.util.List;

/**
 * 砖块对象池
 */
public class MapTilesPool
{
    public static final int DEFAULT_POOL_SIZE=50;
    public static final int DEFAULT_POOL_MAX_SIZE=70;

    private static final List<MapTile> pool=new ArrayList<>();

    static
    {
        for(int i=0;i<DEFAULT_POOL_SIZE;i++)
        {
            pool.add(new MapTile());
        }
    }

    public static MapTile getMapTile()
    {
        MapTile mapTile;
        // 池塘已空
        if(pool.size()==0)
        {
            mapTile=new MapTile();
        }
        else
        {
            mapTile=pool.remove(0);
        }
        return mapTile;
    }

    public static void returnMapTile(MapTile mapTile)
    {
        if(pool.size()==DEFAULT_POOL_MAX_SIZE)
        {
            return;
        }
        pool.add(mapTile);
    }
}
