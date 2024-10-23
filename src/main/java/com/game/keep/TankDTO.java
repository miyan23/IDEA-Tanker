package com.game.keep;

import java.util.ArrayList;
import java.util.List;

public class TankDTO
{
    private int x;                      // 坐标
    private int y;                      // 坐标
    private int hp;                     // 血量
    private int dir;                    // 方向
    private int state;                   // 状态

    private List<BulletDTO> bullets=new ArrayList<>();             // 子弹

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getDir() {
        return dir;
    }

    public void setDir(int dir) {
        this.dir = dir;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public List<BulletDTO> getBullets() {
        return bullets;
    }

    public void setBullets(List<BulletDTO> bullets) {
        this.bullets = bullets;
    }
}
