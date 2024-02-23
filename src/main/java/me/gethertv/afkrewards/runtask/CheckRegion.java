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

import java.text.DecimalFormat;
import java.util.*;

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
        IBoxSettingsApi iBoxSettingsApi = Main.getInstance().getiBoxSettingsApi();

        for (Player player : Bukkit.getOnlinePlayers()) {
            handlePlayerActivity(player, iBoxSettingsApi);
        }
    }

    private void handlePlayerActivity(Player player, IBoxSettingsApi iBoxSettingsApi) {
        if (!afkZone.getCuboid().contains(player.getLocation())) {
            handlePlayerNotInZone(player, iBoxSettingsApi);
        } else {
            handlePlayerInZone(player, iBoxSettingsApi);
        }
    }

    private void handlePlayerNotInZone(Player player, IBoxSettingsApi iBoxSettingsApi) {
        if(!userdata.containsKey(player.getUniqueId()))
            return;

        if (iBoxSettingsApi != null) {
            iBoxSettingsApi.enableActionBar(player);
        }

        userdata.remove(player.getUniqueId());

        List<User> users = Main.getInstance().getUserData().get(afkZone.getName());
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                user.destroy();
                iterator.remove();
                break;
            }
        }
        Main.getInstance().getUserData().put(afkZone.getName(), users);
    }

    private void handlePlayerInZone(Player player, IBoxSettingsApi iBoxSettingsApi) {
        if (userdata.get(player.getUniqueId()) == null) {
            addUserToZone(player, iBoxSettingsApi);
        } else {
            updateUserInZone(player);
        }
    }

    private void addUserToZone(Player player, IBoxSettingsApi iBoxSettingsApi) {
        if (iBoxSettingsApi != null) {
            iBoxSettingsApi.disableActionBar(player);
        }

        List<User> users = Main.getInstance().getUserData().get(afkZone.getName());
        users.add(new User(player, afkZone));
        userdata.put(player.getUniqueId(), System.currentTimeMillis() + (afkZone.getSecond() * 1000));
    }

    private void updateUserInZone(Player player) {
        String value = getValue(userdata.get(player.getUniqueId()));
        value = value.replace(",", ".");
        double pr = Double.parseDouble(value);
        double valueTemp =  pr * 100;
        String procenty = String.format("%.2f", valueTemp);

        CmdRewards reward = afkZone.getReward();
        double chance = calculateChance(player, reward);

        updateBossBar(player, pr, procenty, chance);

        if(afkZone.isTitle())
            player.sendTitle(ColorFixer.addColors(Main.getInstance().getConfig().getString("lang.title").replace("{value}", procenty)), ColorFixer.addColors(Main.getInstance().getConfig().getString("lang.sub-title").replace("{value}", procenty)), 10, 22, 10);

        if(afkZone.isActionBar())
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                    ColorFixer.addColors(Main.getInstance().getConfig().getString("lang.format")
                            .replace("{time}", getTime(userdata.get(player.getUniqueId())))
                            .replace("{bar}", getBar(value)
                            ))));

        if(userdata.get(player.getUniqueId()) <= System.currentTimeMillis()) {
            handleRewards(player, reward, chance);
        }
    }

    private double calculateChance(Player player, CmdRewards reward) {
        double chance = 0;
        for (Map.Entry<String, Double> entry : reward.getChance().entrySet()) {
            String permission = entry.getKey();
            double chanceTemp = entry.getValue();
            if (player.hasPermission(permission)) {
                if (chanceTemp > chance)
                    chance = chanceTemp;
            }
        }
        return chance;
    }

    private void updateBossBar(Player player, double pr, String procenty, double chance) {
        if(afkZone.isBossBar()) {
            List<User> users = Main.getInstance().getUserData().get(afkZone.getName());
            for (User user : users) {
                if (!user.getPlayer().getUniqueId().equals(player.getUniqueId()))
                    continue;

                user.getBossBar().setProgress((pr > 1) ? 1 : pr);
                String text = afkZone.getBossName();
                int second = (int) (userdata.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
                user.getBossBar().setTitle(ColorFixer.addColors(
                        text.replace("{time}", Timer.getTime(second))
                                .replace("{percent}", procenty)
                                .replace("{chance}", String.format("%.2f", chance))
                ));
            }
        }
    }

    private void handleRewards(Player player, CmdRewards reward, double chance) {
        AfkRewardsDone afkRewardsDone = new AfkRewardsDone(player);
        Bukkit.getPluginManager().callEvent(afkRewardsDone);
        if (!afkRewardsDone.isCancelled()) {
            if(isWinningTicket(chance)) {
                for(String cmd : reward.getCmds())
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player.getName()));
            }
            cleanupUserData(player);
        }
    }

    private boolean isWinningTicket(double chance) {
        Random random = new Random();
        double min = 0;
        double max = 100-chance;
        double startWin = min + (max - min) * random.nextDouble();
        double finishWin = startWin + chance;

        double winTicket = random.nextDouble() * 100;

        return winTicket >= startWin && finishWin >= winTicket;
    }

    private void cleanupUserData(Player player) {
        List<User> users = Main.getInstance().getUserData().get(afkZone.getName());
        users.forEach(user -> {
            if(user.getPlayer().getUniqueId().equals(player.getUniqueId()))
            {
                user.clear(afkZone);
            }
        });
        userdata.put(player.getUniqueId(), System.currentTimeMillis() + (afkZone.getSecond() * 1000));
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

    public AfkZone getAfkZone() {
        return afkZone;
    }
}
