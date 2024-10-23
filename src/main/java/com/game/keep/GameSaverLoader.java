package com.game.keep;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.entity.Bullet;
import com.game.entity.GameFrame;
import com.game.entity.Score;
import com.game.map.GameMap;
import com.game.map.MapTile;
import com.game.pool.EnemyTanksPool;
import com.game.tank.Tank;
import com.game.util.Tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameSaverLoader
{
    // json文件路径
    public static final String FILE_PATH_MY_TANK1 ="res/data/myTank1.json";
    public static final String FILE_PATH_MY_TANK2 ="res/data/myTank2.json";
    public static final String FILE_PATH_ENEMIES="res/data/enemies.json";
    public static final String FILE_PATH_GAME_MAP ="res/data/gameMap.json";
    public static final String FILE_PATH_SCORE="res/data/score.json";
    public static final String FILE_PATH_RUNTIME="res/data/runTime.json";

    /**
     * 保存游戏数据文件
     */
    public static void saveGameData()
    {
        // 游戏正常结束则不保存
        if(GameFrame.getMyTank1()==null)
        {
            return;
        }

        try
        {
            ObjectMapper mapper=new ObjectMapper();

            // 写入数据——己方坦克
            writeMyTankAndBullets(GameFrame.getMyTank1(),mapper,FILE_PATH_MY_TANK1);
            writeMyTankAndBullets(GameFrame.getMyTank2(),mapper,FILE_PATH_MY_TANK2);

            // 写入数据——敌方坦克
            writeEnemiesAndBullets(GameFrame.getEnemies(),mapper,FILE_PATH_ENEMIES);

            // 写入数据——地图
            mapper.writeValue(new File(FILE_PATH_GAME_MAP),GameFrame.getGameMap().getTiles());

            // 写入数据——得分
            mapper.writeValue(new File(FILE_PATH_SCORE), Score.getScore());

            // 写入数据——计时
            mapper.writeValue(new File(FILE_PATH_RUNTIME),GameFrame.getRunTime());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从文件加载数据
     */
    public static void loadGameData()
    {
        // 文件为空则不读取
        if(!new File(FILE_PATH_MY_TANK1).exists() ||
                !new File(FILE_PATH_MY_TANK2).exists() ||
                !new File(FILE_PATH_ENEMIES).exists() ||
                !new File(FILE_PATH_GAME_MAP).exists() ||
                !new File(FILE_PATH_SCORE).exists() ||
                !new File(FILE_PATH_RUNTIME).exists())
        {
            return;
        }

        try
        {
            ObjectMapper mapper=new ObjectMapper();

            // 读取数据——己方坦克
            readMyTankAndBullets(GameFrame.getMyTank1(),mapper,FILE_PATH_MY_TANK1);
            readMyTankAndBullets(GameFrame.getMyTank2(),mapper,FILE_PATH_MY_TANK2);

            // 读取数据——敌方坦克
            readEnemiesAndBullets(GameFrame.getEnemies(),mapper,FILE_PATH_ENEMIES);

            // 读取数据——地图
            List<MapTile> tiles= mapper.readValue(new File(FILE_PATH_GAME_MAP), new TypeReference<>() {});
            GameMap.setTiles(tiles);

            // 读取数据——得分
            Score.setScore(mapper.readValue(new File(FILE_PATH_SCORE),int.class));

            // 读取数据——计时
            GameFrame.setRunTime(mapper.readValue(new File(FILE_PATH_RUNTIME),long.class));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 写入数据——己方坦克和子弹
     */
    private static void writeMyTankAndBullets(Tank myTank,ObjectMapper mapper,String filePath) throws IOException
    {
        // 写入数据——己方坦克
        TankDTO tankDTO =new TankDTO();
        tankDTO.setX(myTank.getX());
        tankDTO.setY(myTank.getY());
        tankDTO.setHp(myTank.getHp());
        tankDTO.setDir(myTank.getDir());
        tankDTO.setState(myTank.getState());

        // 写入己方坦克子弹
        List<BulletDTO> bulletsDTO=new ArrayList<>();
        for (Object bullet : myTank.getBullets())
        {
            BulletDTO bulletDTO=new BulletDTO();
            bulletDTO.setX(((Bullet) bullet).getX());
            bulletDTO.setY(((Bullet) bullet).getY());
            bulletDTO.setDir(((Bullet) bullet).getDir());
            bulletDTO.setVisible(((Bullet) bullet).isVisible());
            bulletsDTO.add(bulletDTO);
        }
        tankDTO.setBullets(bulletsDTO);

        mapper.writeValue(new File(filePath), tankDTO);
    }

    /**
     * 写入数据——敌方坦克和子弹
     */
    private static void writeEnemiesAndBullets(List<Tank> enemies,ObjectMapper mapper,String filePath) throws IOException
    {
        List<TankDTO> enemiesDTO=new ArrayList<>();
        for (Tank enemy : enemies)
        {
            TankDTO enemyDTO=new TankDTO();
            enemyDTO.setX(enemy.getX());
            enemyDTO.setY(enemy.getY());
            enemyDTO.setHp(enemy.getHp());
            enemyDTO.setDir(enemy.getDir());
            enemyDTO.setState(enemy.getState());

            // 写入敌方坦克子弹
            List<BulletDTO> bulletsEnemyDTO=new ArrayList<>();
            for (Object bullet : enemy.getBullets())
            {
                BulletDTO bulletDTO=new BulletDTO();
                bulletDTO.setX(((Bullet) bullet).getX());
                bulletDTO.setY(((Bullet) bullet).getY());
                bulletDTO.setDir(((Bullet) bullet).getDir());
                bulletDTO.setVisible(((Bullet) bullet).isVisible());
                bulletsEnemyDTO.add(bulletDTO);
            }
            enemyDTO.setBullets(bulletsEnemyDTO);

            enemiesDTO.add(enemyDTO);
        }
        mapper.writeValue(new File(filePath),enemiesDTO);
    }

    /**
     * 读取数据——己方坦克和子弹
     */
    private static void readMyTankAndBullets(Tank myTank,ObjectMapper mapper,String filePath) throws IOException
    {
        // 读取数据——己方坦克
        TankDTO tankDTO =mapper.readValue(new File(filePath), TankDTO.class);

        if(tankDTO==null)
        {
            return;
        }

        myTank.setX(tankDTO.getX());
        myTank.setY(tankDTO.getY());
        myTank.setHp(tankDTO.getHp());
        myTank.setDir(tankDTO.getDir());
        myTank.setState(tankDTO.getState());

        // 读取己方坦克子弹
        List<BulletDTO> bulletsDTO=tankDTO.getBullets();
        for (BulletDTO bulletDTO : bulletsDTO)
        {
            Bullet bullet=new Bullet();
            bullet.setAtk(Tank.ATK_DEFAULT);
            bullet.setX(bulletDTO.getX());
            bullet.setY(bulletDTO.getY());
            bullet.setDir(bulletDTO.getDir());
            bullet.setColor(Tools.getMyColor());
            bullet.setVisible(bulletDTO.isVisible());
            myTank.getBullets().add(bullet);
        }
        myTank.setColor(Tools.getMyColor());
    }

    /**
     * 读取数据——敌方坦克和子弹
     */
    private static void readEnemiesAndBullets(List<Tank> enemies,ObjectMapper mapper,String filePath) throws IOException
    {
        // 文件为空则不读取
        if(!new File(filePath).exists())
        {
            return;
        }

        List<TankDTO> enemiesDTO=mapper.readValue(new File(filePath), new TypeReference<>() {});

        if(enemiesDTO==null)
        {
            return;
        }

        enemies.clear();
        for (TankDTO dto : enemiesDTO)
        {
            Tank enemy= EnemyTanksPool.getTank();
            enemy.setEnemy(true);

            enemy.setX(dto.getX());
            enemy.setY(dto.getY());
            enemy.setHp(dto.getHp());
            enemy.setDir(dto.getDir());
            enemy.setState(dto.getState());
            enemy.setColor(Tools.getEnemyColor());

            // 读取敌方坦克子弹
            List<BulletDTO> bulletsEnemyDTO=dto.getBullets();
            for (BulletDTO bulletDTO : bulletsEnemyDTO)
            {
                Bullet bullet=new Bullet();
                bullet.setAtk(Tank.ATK_DEFAULT);
                bullet.setX(bulletDTO.getX());
                bullet.setY(bulletDTO.getY());
                bullet.setDir(bulletDTO.getDir());
                bullet.setColor(Tools.getEnemyColor());
                bullet.setVisible(bulletDTO.isVisible());
                enemy.getBullets().add(bullet);
            }

            GameFrame.getEnemies().add(enemy);
        }
    }
}
