package com.game.entity;

/**
 * 得分系统
 */
public class Score
{
    public static final int SCORE_ENEMY=1;

    private static int score=0;

    /**
     * 得分计算
     */
    public static void scoreCountEnemy()
    {
        score+=SCORE_ENEMY;
    }

    public static int getScore() {
        return score;
    }

    public static void setScore(int score) {
        Score.score = score;
    }
}
