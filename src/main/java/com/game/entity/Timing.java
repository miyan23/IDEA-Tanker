package com.game.entity;

public class Timing
{
    /**
     * 计时——运行时长
     */
    public static void countRunTime()
    {
        GameFrame.setRunTime(System.currentTimeMillis()/1000-GameFrame.getStartRunTime()/1000);
    }

    /**
     * 计时——暂停时长
     */
    public static void countPauseTime()
    {
        GameFrame.setPauseTime(System.currentTimeMillis()/1000-GameFrame.getStartPauseTime()/1000);
    }
}
