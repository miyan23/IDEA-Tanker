package com.game.mq;

import com.game.keep.GameSaverLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Message implements Serializable
{
    // 文件路径集
    private static final List<String> filePaths=new ArrayList<>();
    static
    {
        filePaths.add(GameSaverLoader.FILE_PATH_GAME_MAP);
        filePaths.add(GameSaverLoader.FILE_PATH_ENEMIES);
        filePaths.add(GameSaverLoader.FILE_PATH_MY_TANK1);
        filePaths.add(GameSaverLoader.FILE_PATH_MY_TANK2);
        filePaths.add(GameSaverLoader.FILE_PATH_RUNTIME);
        filePaths.add(GameSaverLoader.FILE_PATH_SCORE);
    }

    /**
     * 封装主机游戏信息
     */
    public static String getGameMessage()
    {
        // 保存当前游戏状态
        GameSaverLoader.saveGameData();

        // 拼接文件
        return mergeJsonFiles();
    }

    /**
     * 拼接文件
     */
    private static String mergeJsonFiles()
    {
        StringBuilder combinedJsonBuilder=new StringBuilder();

        for (String filePath : filePaths)
        {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath)))
            {
                // 读取一行并将其添加到组合的JSON字符串中
                combinedJsonBuilder.append(reader.readLine());
                // 在每个文件之间添加一个额外的换行符分隔
                combinedJsonBuilder.append("\n");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return combinedJsonBuilder.toString();
    }

    /**
     * 将消费者获取的信息拆分放回不同的文件
     */
    public static void splitJsonFiles(String message)
    {
        // 将字符串按换行符分割
        String[] lines=message.split("\n");

        int index=0;
        for (String filePath : filePaths)
        {
            try(BufferedWriter writer=new BufferedWriter(new FileWriter(filePath)))
            {
                writer.write(lines[index]);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            index++;
        }
    }
}
