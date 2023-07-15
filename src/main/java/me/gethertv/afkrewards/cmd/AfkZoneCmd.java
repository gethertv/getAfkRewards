package me.gethertv.afkrewards.cmd;


import me.gethertv.afkrewards.Main;
import me.gethertv.afkrewards.utils.ColorFixer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class AfkZoneCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        if(!player.hasPermission("admin.selector"))
            return false;

        if(args.length==1)
        {
            if(args[0].equalsIgnoreCase("selector"))
                player.getInventory().addItem(Main.getSelector());

            if(args[0].equalsIgnoreCase("reload"))
            {
                Main.getInstance().reloadMainPlugin();
                player.sendMessage(ColorFixer.addColors("&aPomyslnie przeladowano plugin!"));
                return true;
            }
            return false;
        }
        if(args.length==2) {
            if(args[0].equalsIgnoreCase("create")) {
                String name = args[1].toLowerCase();
                if (Main.getFirst() == null || Main.getSecond() == null) {
                    player.sendMessage(ColorFixer.addColors("&cZaznacz bloki!"));
                    return false;
                }

                Main.getInstance().getConfig().set("afk."+name+".first", Main.getFirst());
                Main.getInstance().getConfig().set("afk."+name+".second", Main.getSecond());
                Main.getInstance().getConfig().set("afk."+name+".reward.commands", Arrays.asList("say {player} test!"));
                Main.getInstance().getConfig().set("afk."+name+".reward.permissions.1.permission", "afk.vip");
                Main.getInstance().getConfig().set("afk."+name+".reward.permissions.1.chance", 50);
                Main.getInstance().getConfig().set("afk."+name+".time", 600);
                Main.getInstance().getConfig().set("afk."+name+".p-color", "GREEN");
                Main.getInstance().getConfig().set("afk."+name+".p-style", "SOLID");
                Main.getInstance().getConfig().set("afk."+name+".bar-name", "&a✦ &7| &fPodstawową nagrodę dostaniesz za: &a{time} &8(&a{percent}%&8) &7| &fszansa: &7{chance}");
                Main.getInstance().saveConfig();
                player.sendMessage(ColorFixer.addColors("&aPomyslnie dodano!"));
            }
        }
        return false;
    }
}
