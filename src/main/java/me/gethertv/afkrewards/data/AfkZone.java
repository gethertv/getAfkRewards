package me.gethertv.afkrewards.data;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.List;

public class AfkZone {


    private String name;
    private Cuboid cuboid;
    private CmdRewards reward;
    private int second;

    private BarColor barColor;
    private BarStyle barStyle;
    private String bossName;
    private boolean bossBar;
    private boolean actionBar;
    private boolean title;
    public AfkZone(String name, Cuboid cuboid, CmdRewards reward, int second, BarColor barColor, BarStyle barStyle, String bossName, boolean bossBar, boolean actionBar, boolean title) {
        this.name = name;
        this.bossName = bossName;
        this.cuboid = cuboid;
        this.reward = reward;
        this.second = second;
        this.barColor = barColor;
        this.barStyle = barStyle;
        this.bossBar = bossBar;
        this.actionBar = actionBar;
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public BarColor getBarColor() {
        return barColor;
    }

    public BarStyle getBarStyle() {
        return barStyle;
    }

    public String getBossName() {
        return bossName;
    }

    public Cuboid getCuboid() {
        return cuboid;
    }

    public CmdRewards getReward() {
        return reward;
    }

    public int getSecond() {
        return second;
    }

    public boolean isActionBar() {
        return actionBar;
    }

    public boolean isBossBar() {
        return bossBar;
    }

    public boolean isTitle() {
        return title;
    }
}
