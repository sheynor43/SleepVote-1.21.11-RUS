package me.sheynor.sleepvote;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Where a given plugin message should be displayed to a player.
 */
public enum MessageTarget {
    CHAT,
    ACTIONBAR;

    public void send(Player player, String rawMessage) {
        String message = ChatColor.translateAlternateColorCodes('&', rawMessage);
        if (this == ACTIONBAR) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        } else {
            player.sendMessage(message);
        }
    }

    public static MessageTarget fromConfig(String value, MessageTarget fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return MessageTarget.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
