package me.gethertv.afkrewards;

import dev.gether.getboxsettings.api.IBoxSettingsApi;
import me.gethertv.afkrewards.cmd.AfkZoneCmd;
import me.gethertv.afkrewards.data.AfkZone;
import me.gethertv.afkrewards.data.CmdRewards;
import me.gethertv.afkrewards.data.Cuboid;
import me.gethertv.afkrewards.data.User;
import me.gethertv.afkrewards.event.ConnectPlayer;
import me.gethertv.afkrewards.listeners.InteractionClick;
import me.gethertv.afkrewards.runtask.CheckRegion;
import me.gethertv.afkrewards.utils.ColorFixer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Main extends JavaPlugin {

    private static Main instance;
    private List<CheckRegion> afkZoneList;

    private static ItemStack selector;

    private static Location first;
    private static Location second;


    private HashMap<String, List<User>> userData = new HashMap<>();
    private IBoxSettingsApi iBoxSettingsApi;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();

        if(Bukkit.getPluginManager().getPlugin("getBoxSettings")!=null)
        {
            iBoxSettingsApi = (IBoxSettingsApi) Bukkit.getPluginManager().getPlugin("getBoxSettings");
        }

        afkZoneList = new ArrayList<>();

        selector = new ItemStack(Material.STICK);
        ItemMeta itemMeta = selector.getItemMeta();
        itemMeta.setDisplayName(ColorFixer.addColors("&7&k# &fSelector"));
        selector.setItemMeta(itemMeta);

        loadAfkZone();

        getServer().getPluginManager().registerEvents(new InteractionClick(), this);
        getCommand("afkzone").setExecutor(new AfkZoneCmd());

        getServer().getPluginManager().registerEvents(new ConnectPlayer(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for(CheckRegion checkRegion : afkZoneList)
        {
            checkRegion.cancel();
        }
        Collection<List<User>> values = userData.values();
        for (List<User> zone : values) {
            for (User user : zone)
            {
                user.destroy();
            }
        }
        userData.clear();
        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
        instance = null;
    }

    public void reloadMainPlugin()
    {
        reloadConfig();

        for(CheckRegion checkRegion : afkZoneList)
        {
            checkRegion.cancel();
        }
        Collection<List<User>> values = userData.values();
        for (List<User> zone : values) {
            for (User user : zone)
            {
                user.destroy();
            }
        }
        userData.clear();
        getUserData().clear();
        afkZoneList.clear();
        loadAfkZone();
    }

    private void loadAfkZone()
    {
        if(!getConfig().isSet("afk"))
            return;

        for(String name : getConfig().getConfigurationSection("afk").getKeys(false))
        {
            getUserData().put(name, new ArrayList<>());
            Location first = getConfig().getLocation("afk."+name+".first");
            Location second = getConfig().getLocation("afk."+name+".second");

            HashMap<String, Double> chance = new HashMap<>();
            ConfigurationSection rewardSection =  getConfig().getConfigurationSection("afk."+name+".reward");
            List<String> cmds = new ArrayList<>(rewardSection.getStringList(".commands"));

            ConfigurationSection permisionsSection = rewardSection.getConfigurationSection(".permissions");
            for(String perKey : permisionsSection.getKeys(false))
            {
                ConfigurationSection permSection = permisionsSection.getConfigurationSection("." + perKey);

                String permission = permSection.getString(".permission");
                double chanceReward = permSection.getDouble(".chance");

                chance.put(permission, chanceReward);
            }



            int time = getConfig().getInt("afk."+name+".time");
            Cuboid cuboid = new Cuboid(first, second);
            CheckRegion checkRegion = new CheckRegion(
                    new AfkZone(
                        name,
                        cuboid,
                        new CmdRewards(chance, cmds),
                        time,
                        BarColor.valueOf(getConfig().getString("afk."+name+".p-color").toUpperCase()),
                        BarStyle.valueOf(getConfig().getString("afk."+name+".p-style").toUpperCase()),
                        getConfig().getString("afk."+name+".bar-name"),
                        getConfig().getBoolean("afk."+name+".boss-bar"),
                        getConfig().getBoolean("afk."+name+".action-bar"),
                        getConfig().getBoolean("afk."+name+".title")
                    )
            );
            checkRegion.runTaskTimer(this, 20L*getConfig().getInt("task-time"), 20L*getConfig().getInt("task-time"));
            afkZoneList.add(checkRegion);

        }
    }

    public IBoxSettingsApi getiBoxSettingsApi() {
        return iBoxSettingsApi;
    }

    public HashMap<String, List<User>> getUserData() {
        return userData;
    }

    public List<CheckRegion> getAfkZoneList() {
        return afkZoneList;
    }

    public static Main getInstance() {
        return instance;
    }

    public static ItemStack getSelector() {
        return selector;
    }

    public static void setFirst(Location first) {
        Main.first = first;
    }

    public static void setSecond(Location second) {
        Main.second = second;
    }

    public static Location getFirst() {
        return first;
    }

    public static Location getSecond() {
        return second;
    }
}
