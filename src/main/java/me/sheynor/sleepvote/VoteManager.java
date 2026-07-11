package me.sheynor.sleepvote;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VoteManager {

    private final SleepVotePlugin plugin;
    private final Set<UUID> voted = new HashSet<>();
    private final Set<UUID> votedYes = new HashSet<>();
    private boolean voting = false;
    private World voteWorld;

    public VoteManager(SleepVotePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isVoting() {
        return voting;
    }

    public void startVote(Player starter) {
        voting = true;
        voted.clear();
        votedYes.clear();
        voteWorld = starter.getWorld();

        Lang lang = plugin.getLang();
        String startMessage = lang.get("vote-start").replace("%player%", starter.getName());
        for (Player p : voteWorld.getPlayers()) {
            sendVoteStartMessage(p, startMessage);
        }

        int duration = plugin.getConfig().getInt("vote.duration-seconds");
        MessageTarget countdownTarget = MessageTarget.fromConfig(
                plugin.getConfig().getString("display.countdown"), MessageTarget.CHAT);

        new BukkitRunnable() {
            int timeLeft = duration;

            @Override
            public void run() {
                if (!voting) {
                    cancel();
                    return;
                }
                if (timeLeft <= 0) {
                    endVote();
                    cancel();
                    return;
                }
                if (countdownTarget == MessageTarget.ACTIONBAR || timeLeft <= 5 || timeLeft % 5 == 0) {
                    String message = lang.get("vote-countdown").replace("%seconds%", String.valueOf(timeLeft));
                    for (Player p : voteWorld.getPlayers()) {
                        countdownTarget.send(p, message);
                    }
                }
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void sendVoteStartMessage(Player player, String startMessage) {
        Lang lang = plugin.getLang();
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', startMessage));

        TextComponent yes = new TextComponent(" " + lang.get("vote-yes-button") + " ");
        yes.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        yes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sleepvote yes"));

        TextComponent no = new TextComponent(" " + lang.get("vote-no-button") + " ");
        no.setColor(net.md_5.bungee.api.ChatColor.RED);
        no.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sleepvote no"));

        player.spigot().sendMessage(yes, no);
    }

    public void vote(Player player, boolean yes) {
        Lang lang = plugin.getLang();
        if (!voting) {
            return;
        }
        if (voted.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.get("vote-already-voted")));
            return;
        }
        if (!player.getWorld().equals(voteWorld)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.get("vote-wrong-world")));
            return;
        }

        voted.add(player.getUniqueId());
        if (yes) {
            votedYes.add(player.getUniqueId());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.get("vote-registered-yes")));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.get("vote-registered-no")));
        }
    }

    private void endVote() {
        voting = false;
        Lang lang = plugin.getLang();
        MessageTarget resultTarget = MessageTarget.fromConfig(
                plugin.getConfig().getString("display.result"), MessageTarget.CHAT);

        int total = voteWorld.getPlayers().size();
        double percent = total == 0 ? 0 : votedYes.size() * 100.0 / total;
        double required = plugin.getConfig().getDouble("vote.required-percent");

        if (percent >= required) {
            skipNight(resultTarget);
        } else {
            String message = lang.get("vote-result-fail");
            for (Player p : voteWorld.getPlayers()) {
                resultTarget.send(p, message);
            }
        }
    }

    private void skipNight(MessageTarget resultTarget) {
        int speed = plugin.getConfig().getInt("time.skip-speed");
        Lang lang = plugin.getLang();
        World world = voteWorld;

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            long time = world.getTime();
            if (time >= 0L && time <= 12000L) {
                String message = lang.get("vote-result-success");
                for (Player p : world.getPlayers()) {
                    resultTarget.send(p, message);
                    p.setStatistic(Statistic.TIME_SINCE_REST, 0);
                }
                task.cancel();
                return;
            }
            world.setTime(time + speed);
        }, 0L, 1L);
    }
}
