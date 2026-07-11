package me.sheynor.sleepvote;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SleepVotePlugin extends JavaPlugin {

    private VoteManager voteManager;
    private Lang lang;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        lang = new Lang(this);
        voteManager = new VoteManager(this);

        getServer().getPluginManager().registerEvents(new SleepListener(voteManager), this);
        getCommand("sleepvote").setExecutor(this::onSleepVoteCommand);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    public Lang getLang() {
        return lang;
    }

    private boolean onSleepVoteCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.get("command-players-only")));
            return true;
        }
        if (args.length != 1 || !(args[0].equalsIgnoreCase("yes") || args[0].equalsIgnoreCase("no"))) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.get("command-usage")));
            return true;
        }

        voteManager.vote(player, args[0].equalsIgnoreCase("yes"));
        return true;
    }
}
