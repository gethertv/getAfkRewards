package me.gethertv.afkrewards.runtask;

import dev.gether.getboxsettings.api.IBoxSettingsApi;
import me.gethertv.afkrewards.Main;
import me.gethertv.afkrewards.data.AfkZone;
import me.gethertv.afkrewards.data.CmdRewards;
import me.gethertv.afkrewards.data.User;
import me.gethertv.afkrewards.event.AfkRewardsDone;
import me.gethertv.afkrewards.utils.ColorFixer;
import me.gethertv.afkrewards.utils.Timer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.A;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class CheckRegion extends BukkitRunnable {

    private AfkZone afkZone;
    private HashMap<UUID, Long> userdata;
    DecimalFormat formatter = new DecimalFormat("00");

    public CheckRegion(AfkZone afkZone)
    {
        userdata = new HashMap<>();
        this.afkZone = afkZone;
    }


    @Override
    public void run() {

        for(Player player : Bukkit.getOnlinePlayers())
        {

            if(!afkZone.getCuboid().contains(player.getLocation()))
            {
                IBoxSettingsApi iBoxSettingsApi = Main.getInstance().getiBoxSettingsApi();
                if(iBoxSettingsApi!=null)
                {
                    iBoxSettingsApi.enableActionBar(player);
                }
                if(userdata.containsKey(player.getUniqueId()))
                    userdata.remove(player.getUniqueId());

                User user = Main.getInstance().getUserData().remove(player.getUniqueId());
                if(user!=null)
                {
                    user.destroy();
                    Main.getInstance().getUserData().remove(player.getUniqueId());
                }
                continue;
            }
            if(userdata.get(player.getUniqueId())==null)
            {
                IBoxSettingsApi iBoxSettingsApi = Main.getInstance().getiBoxSettingsApi();
                if(iBoxSettingsApi!=null)
                {
                    iBoxSettingsApi.disableActionBar(player);
                }
                Main.getInstance().getUserData().put(player.getUniqueId(),new User(player, afkZone));
                userdata.put(player.getUniqueId(), System.currentTimeMillis()+(afkZone.getSecond()*1000));
                continue;
            }
            String value = getValue(userdata.get(player.getUniqueId()));
            value = value.replace(",", ".");
            double pr = Double.parseDouble(value);
            double valueTemp =  pr *100;
            String procenty = String.format("%.2f", valueTemp);

            CmdRewards reward = afkZone.getReward();
            double chance = 0;
            for (Map.Entry<String, Double> entry : reward.getChance().entrySet()) {
                String permission = entry.getKey();
                Double chanceTemp = entry.getValue();
                if (player.hasPermission(permission)) {
                    if (chanceTemp > chance)
                        chance = chanceTemp;
                }
            }
            if(Main.getInstance().getConfig().getBoolean("boss-bar"))
            {
                User user = Main.getInstance().getUserData().get(player.getUniqueId());
                if(user!=null)
                {

                    user.getBossBar().setProgress((pr > 1) ? 1 : pr);
                    String text = afkZone.getBossName();
                    int second = (int) (user.getFinishSecond() - System.currentTimeMillis()) / 1000;
                    user.getBossBar().setTitle(ColorFixer.addColors(
                            text.replace("{time}", Timer.getTime(second))
                                    .replace("{percent}", procenty)
                                    .replace("{chance}", String.format("%.2f", chance))
                    ));
                }

            }

            if(Main.getInstance().getConfig().getBoolean("title"))
                player.sendTitle(ColorFixer.addColors(Main.getInstance().getConfig().getString("lang.title").replace("{value}", procenty)), ColorFixer.addColors(Main.getInstance().getConfig().getString("lang.sub-title").replace("{value}", procenty)), 10, 22, 10);

            if(Main.getInstance().getConfig().getBoolean("actionbar"))
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                        ColorFixer.addColors(Main.getInstance().getConfig().getString("lang.format")
                                .replace("{time}", getTime(userdata.get(player.getUniqueId())))
                                .replace("{bar}", getBar(value)
                        ))));


            if(userdata.get(player.getUniqueId())<= System.currentTimeMillis())
            {

                AfkRewardsDone afkRewardsDone = new AfkRewardsDone(player);
                Bukkit.getPluginManager().callEvent(afkRewardsDone);
                if (!afkRewardsDone.isCancelled()) {
                    Random random = new Random();
                    double min = 0;
                    double max = 100-chance;
                    double startWin = min + (max - min) * random.nextDouble();
                    double finishWin = startWin+chance;

                    double winTicket = random.nextDouble()*100;

                    if(winTicket >= startWin && finishWin >=winTicket)
                    {
                        for(String cmd : reward.getCmds())
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player.getName()));
                    }


                    User user = Main.getInstance().getUserData().get(player.getUniqueId());
                    if(user!=null)
                        user.destroy();

                    Main.getInstance().getUserData().remove(player.getUniqueId());
                    userdata.remove(player.getUniqueId());
                    continue;
                }
            }

        }
    }

    private String getBar(String value) {
        double procent = Double.parseDouble(value);
        int amount = Main.getInstance().getConfig().getInt("styles.size");
        double temp = amount*procent;
        int green = (int) temp;
        int dark = amount-green;
        String bar = "";
        String colorDone = Main.getInstance().getConfig().getString("styles.active");
        String notReady = Main.getInstance().getConfig().getString("styles.no-active");
        String charStack = Main.getInstance().getConfig().getString("styles.char");
        for(int i = 0; i<green;i++)
            bar+=colorDone+charStack;

        for(int i = 0; i<dark;i++)
            bar+=notReady+charStack;

        String format = Main.getInstance().getConfig().getString("bar-format").replace("{value}", bar);


        return ColorFixer.addColors(format);
    }

    private String getValue(Long time) {
        long sec = time - System.currentTimeMillis();
        int seconds = (int) sec/1000;
        int wynik = Math.abs(seconds-afkZone.getSecond());
        double x = (double) wynik / (double) afkZone.getSecond();
        return String.format("%.4f", x);
    }

    public HashMap<UUID, Long> getUserdata() {
        return userdata;
    }

    private String getTime(Long time)
    {
        long sec = time - System.currentTimeMillis();
        int seconds = (int) sec/1000;
        int p1 = seconds % 60;
        int p2 = seconds / 60;
        int p3 = p2 % 60;
        p2 = p2 / 60;
        String timer = formatter.format(p3) + ":" + formatter.format(p1);
        return timer;
    }
}
