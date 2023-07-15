package me.gethertv.afkrewards.data;

import me.gethertv.afkrewards.Main;
import me.gethertv.afkrewards.utils.ColorFixer;
import me.gethertv.afkrewards.utils.Timer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.Map;

public class User {

    private Player player;

    private long second;
    private BossBar bossBar;
    private long finishSecond;



    public User(Player player, AfkZone afkZone)
    {
        this.player = player;
        this.second = System.currentTimeMillis()+(afkZone.getSecond()*1000);
        this.finishSecond = System.currentTimeMillis()+(afkZone.getSecond()*1000);
        checkBoostDrop(afkZone);
    }

    private void checkBoostDrop(AfkZone afkZone) {
        double chance = 0;
        for (Map.Entry<String, Double> entry : afkZone.getReward().getChance().entrySet()) {
            String permission = entry.getKey();
            Double chanceTemp = entry.getValue();
            if (player.hasPermission(permission)) {
                if (chanceTemp > chance)
                    chance = chanceTemp;
            }
        }

        String text = afkZone.getBossName();
        int second = (int) (this.finishSecond - System.currentTimeMillis()) / 1000;
        bossBar = Bukkit.createBossBar(ColorFixer.addColors(
                text.replace("{time}", Timer.getTime(second))
                        .replace("{percent}", "0.00")
                        .replace("{chance}", String.format("%.2f", chance))
                ),
                afkZone.getBarColor(),
                afkZone.getBarStyle());

        bossBar.setProgress(0);


        bossBar.addPlayer(player);
    }


    public Player getPlayer() {
        return player;
    }

    public long getSecond() {
        return second;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public long getFinishSecond() {
        return finishSecond;
    }

    public void destroy() {
        bossBar.removeAll();
    }
}
