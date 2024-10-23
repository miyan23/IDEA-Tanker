package com.game.map;

import com.game.entity.GameFrame;
import com.game.mq.RabbitMq;
import com.game.pool.MapTilesPool;
import com.game.tank.Tank;
import com.game.util.Constant;
import com.game.util.Tools;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 地图类
 */
public class GameMap
{
    // 砖块与上边界的间隔
    public static final int MAP_Y=Tank.RADIUS*3+GameFrame.titleBarHeight;

    // 地图元素块的容器
    private static List<MapTile> tiles=new ArrayList<>();

    public GameMap()
    {
        initMap();
    }

    /**
     * 初始化地图元素块
     */
    private void initMap()
    {
        if(RabbitMq.getHostMark()==0)
        {
            return;
        }

        // 随机得到一个地图元素块，添加到容器中
        // 初始地图有20个block，10个cover，10个hard
        createTiles(Constant.TILE_MAX_COUNT/4,MapTile.TYPE_COVER);
        createTiles(Constant.TILE_MAX_COUNT/4,MapTile.TYPE_HARD);
        createTiles(Constant.TILE_MAX_COUNT/2,MapTile.TYPE_BLOCK);
    }

    /**
     * 创建一个普通砖块
     */
    public static void createBlock()
    {
        if(tiles.size()>=Constant.TILE_MAX_COUNT)
        {
            return;
        }

        List<MapTile> tiles=GameFrame.getGameMap().getTiles();
        List<Tank> enemyTanks=GameFrame.getEnemies();
        Tank myTank1=GameFrame.getMyTank1();
        Tank myTank2=GameFrame.getMyTank2();

        int x;
        int y;

        // 该砖块不能与已有的砖块和坦克重叠
        do
        {
            x=Tools.getRandomNumber(Constant.MIN_X,Constant.MAX_X);
            y=Tools.getRandomNumber(Constant.MIN_Y,Constant.MAX_Y);
        }while(Tools.isOverlapTankAndTile(tiles,x,y) || Tools.isOverlapTileAndTank(enemyTanks,myTank1,x,y) || Tools.isOverlapTileAndTank(enemyTanks,myTank2,x,y));

        MapTile tile=MapTilesPool.getMapTile();
        tile.setType(MapTile.TYPE_BLOCK);
        tile.setX(x);
        tile.setY(y);
        tiles.add(tile);
    }

    /**
     * 随机生成指定数量和类型的砖块
     */
    public static void createTiles(int count,int type)
    {
        for(int i=0;i<count;i++)
        {
            MapTile tile= MapTilesPool.getMapTile();
            tile.setType(type);
            int x= Tools.getRandomNumber(MapTile.tileWidth/2,Constant.FRAME_WIDTH-MapTile.tileWidth/2);
            int y=Tools.getRandomNumber(MAP_Y+MapTile.tileWidth/2,Constant.FRAME_HEIGHT-Tank.RADIUS*6);
            // 生成的块和已有的重叠，重新生成
            if(Tools.isOverlapTankAndTile(tiles,x,y))
            {
                i--;
                continue;
            }
            tile.setX(x);
            tile.setY(y);
            tiles.add(tile);
        }
    }

    /**
     * 只绘制有遮挡效果的块
     */
    public void drawCover(Graphics g)
    {
        if(tiles==null)
        {
            return;
        }

        synchronized (tiles)
        {
            for (MapTile tile : tiles)
            {
                if(tile.getType()==MapTile.TYPE_COVER)
                {
                    tile.draw(g);
                }
            }
        }
    }

    /**
     * 只对没有遮挡效果的块进行绘制
     */
    public void drawBackground(Graphics g)
    {
        // 处理tiles为空的情况
        if(tiles==null)
        {
            return;
        }

        synchronized (tiles)
        {
            for (MapTile tile : tiles)
            {
                if(tile.getType()!=MapTile.TYPE_COVER)
                {
                    tile.draw(g);
                }
            }
        }
    }

    public List<MapTile> getTiles()
    {
        return tiles;
    }

    public static void setTiles(List<MapTile> tiles) {
        GameMap.tiles = tiles;
    }

    /**
     * 把所有不可见的块从容器中移除
     */
    public void clearDestroyTiles()
    {
        for (int i = 0; i < tiles.size(); i++)
        {
            MapTile tile = tiles.get(i);
            if(!tile.isVisible())
            {
                tiles.remove(i);
            }
        }
    }
}
