package com.game.app;

import com.game.entity.GameFrame;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 程序入口
 */
public class GameMain
{
    public static void main(String[] args) throws IOException, TimeoutException
    {
        new GameFrame();
    }
}
