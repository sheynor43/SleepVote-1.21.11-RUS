package me.sheynor.sleepvote;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Loads the message file matching the "language" config option (en/ru)
 * from the plugin's data folder, falling back to the bundled defaults.
 */
public class Lang {

    private static final String DEFAULT_LANGUAGE = "ru";

    private final YamlConfiguration messages;

    public Lang(JavaPlugin plugin) {
        String language = plugin.getConfig().getString("language", DEFAULT_LANGUAGE).trim().toLowerCase();
        if (!language.equals("en") && !language.equals("ru")) {
            plugin.getLogger().warning("Unknown language '" + language + "' in config.yml, falling back to '"
                    + DEFAULT_LANGUAGE + "'.");
            language = DEFAULT_LANGUAGE;
        }

        String resourcePath = "lang/" + language + ".yml";
        File langFile = new File(plugin.getDataFolder(), resourcePath);
        if (!langFile.exists()) {
            plugin.saveResource(resourcePath, false);
        }

        YamlConfiguration loaded = YamlConfiguration.loadConfiguration(langFile);
        try (Reader defaultReader = new InputStreamReader(plugin.getResource(resourcePath), StandardCharsets.UTF_8)) {
            loaded.setDefaults(YamlConfiguration.loadConfiguration(defaultReader));
        } catch (Exception e) {
            plugin.getLogger().warning("Could not load bundled defaults for " + resourcePath + ": " + e.getMessage());
        }

        this.messages = loaded;
    }

    public String get(String key) {
        return messages.getString(key, key);
    }
}
