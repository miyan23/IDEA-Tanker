package com.game.util;

import java.awt.*;

/**
 * 游戏中的常量都在该类中维护，方便后期管理
 */
public class Constant
{
    /**
     * 游戏窗口相关
     */
    public static final String GAME_TITLE="坦克大战";          // 标题

    public static final int FRAME_WIDTH=800;                // 宽
    public static final int FRAME_HEIGHT=600;               // 高

    /**
     * 通过输出测试得出的坦克行走边界
     */
    public static final int MAX_X=772;
    public static final int MIN_X=28;
    public static final int MAX_Y=572;
    public static final int MIN_Y=52;

    // 动态获得系统的宽高
    public static final int SCREEN_W=Toolkit.getDefaultToolkit().getScreenSize().width;
    public static final int SCREEN_H=Toolkit.getDefaultToolkit().getScreenSize().height;

    // 窗口在屏幕中的坐标
    public static final int FRAME_X=SCREEN_W-FRAME_WIDTH>>1;
    public static final int FRAME_Y=SCREEN_H-FRAME_HEIGHT>>1;

    /**
     * 游戏状态
     */
    public static final int STATE_MENU=0;
    public static final int STATE_HELP=1;
    public static final int STATE_ABOUT=2;
    public static final int STATE_RUN=3;
    public static final int STATE_OVER=4;

    public static final String[] MENUS= {
            "开始游戏",
            "继续游戏",
            "游戏帮助",
            "游戏关于",
            "退出游戏",
    };

    public static final String OVER_QUIT="算了不玩了————Esc键";
    public static final String OVER_RETURN="返回主菜单————Enter键";

    // 字体
    public static final Font GAME_FONT=new Font("宋体",Font.BOLD,24);
    public static final Font OVER_FONT=new Font("宋体",Font.BOLD,16);

    // 刷新率
    public static final int REPAINT_INTERVAL=30;

    // 坦克炮弹的颜色
    public static final Color OWN_COLOR = new Color(255, 204, 0);
    public static final Color ENEMY_COLOR = new Color(42, 229, 42);

    // 最大敌人数量
    public static final int ENEMY_MAX_COUNT=5;
    // 产生敌人坦克的时间间隔
    public static final int ENEMY_BORN_INTERVAL=5000;
    // 敌人状态变换的时间间隔
    public static final int ENEMY_AI_INTERVAL=5000;
    // 坦克的开火频率
    public static final double ENEMY_FIRE_PERCENT=0.03;
    // 己方坦克同时发射的最大炮弹数量
    public static final int OWN_MAX_BULLET=3;

    // 砖块总量
    public static final int TILE_MAX_COUNT=40;
    // 普通砖块刷新时间间隔
    public static final int BLOCK_ADD_INTERVAL=10;
}
