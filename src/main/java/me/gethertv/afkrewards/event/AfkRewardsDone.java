package me.gethertv.afkrewards.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AfkRewardsDone extends Event implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private Player player;
    private boolean cancelled;


    public AfkRewardsDone(Player player) {
        this.player = player;
        this.cancelled = false;
    }


    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public HandlerList getHandlers()
    {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public Player getPlayer() {
        return player;
    }
}
