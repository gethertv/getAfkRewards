package me.gethertv.afkrewards.listeners;

import me.gethertv.afkrewards.Main;
import me.gethertv.afkrewards.utils.ColorFixer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class InteractionClick implements Listener {


    @EventHandler
    public void onClickEvent(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        if(!player.hasPermission("admin.selector"))
            return;

        if(!player.getInventory().getItemInHand().isSimilar(Main.getSelector()))
            return;

        if(event.getAction()== Action.LEFT_CLICK_BLOCK)
        {
            Main.setFirst(event.getClickedBlock().getLocation());
            event.setCancelled(true);
            player.sendMessage(ColorFixer.addColors("&aPomyslnie ustawionio pierwsza lokalizacje"));
            return;
        }
        if(event.getAction()== Action.RIGHT_CLICK_BLOCK)
        {
            if(event.getHand()== EquipmentSlot.OFF_HAND)
                return;

            Main.setSecond(event.getClickedBlock().getLocation());
            event.setCancelled(true);
            player.sendMessage(ColorFixer.addColors("&aPomyslnie ustawionio druga lokalizacje"));
            return;
        }
    }
}
