package me.gethertv.afkrewards.event;

import me.gethertv.afkrewards.Main;
import me.gethertv.afkrewards.runtask.CheckRegion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectPlayer implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        for (CheckRegion checkRegion : Main.getInstance().getAfkZoneList())
        {
            checkRegion.getUserdata().remove(event.getPlayer().getUniqueId());
        }

    }
}
