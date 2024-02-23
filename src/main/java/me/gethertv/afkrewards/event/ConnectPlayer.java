package me.gethertv.afkrewards.event;

import me.gethertv.afkrewards.Main;
import me.gethertv.afkrewards.data.User;
import me.gethertv.afkrewards.runtask.CheckRegion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class ConnectPlayer implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        Collection<List<User>> values = Main.getInstance().getUserData().values();
        for (List<User> zone : values) {
            zone.removeIf(user-> {
                if(user.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                    user.destroy();
                    return true;
                }
                return false;
            });
        }
        for (CheckRegion checkRegion : Main.getInstance().getAfkZoneList())
        {
            checkRegion.getUserdata().remove(player.getUniqueId());
        }

    }
}
