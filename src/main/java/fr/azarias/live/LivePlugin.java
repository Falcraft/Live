package fr.azarias.live;

import com.earth2me.essentials.User;
import fr.azarias.live.AsciiArtCommandExecutor;
import fr.azarias.live.LiveCommandExecutor;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.ess3.api.IEssentials;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LivePlugin extends JavaPlugin {
    private LiveCommandExecutor liveCommandExecutor;
    private AsciiArtCommandExecutor asciiArtCommandExecutor;
    private IEssentials ess;

    public void onEnable() {
        this.saveDefaultConfig();
        try {
            this.liveCommandExecutor = new LiveCommandExecutor(this);
            this.getCommand("live").setExecutor((CommandExecutor)this.liveCommandExecutor);
            this.liveCommandExecutor.load();
            this.getServer().getPluginManager().registerEvents((Listener)this.liveCommandExecutor, (Plugin)this);
            this.asciiArtCommandExecutor = new AsciiArtCommandExecutor(this);
            this.getCommand("aa").setExecutor((CommandExecutor)this.asciiArtCommandExecutor);
            this.asciiArtCommandExecutor.load();
            this.getCommand("aa").setTabCompleter((TabCompleter)this.asciiArtCommandExecutor);
            this.ess = (IEssentials)this.getServer().getPluginManager().getPlugin("Essentials");
        }
        catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public User getUser(Player p) {
        return this.ess.getUser(p);
    }

    public User getUser(String name) {
        return this.ess.getUser(name);
    }

    public User getUser(UUID uuid) {
        return this.ess.getUser(uuid);
    }

    public String config(String cName, String def) {
        String conf = this.getConfig().getString(cName);
        return conf == null ? def : conf;
    }

    public Set<String> configList(String cName) {
        return this.getConfig().getConfigurationSection(cName).getKeys(true);
    }

    public String colorConfig(String path) {
        return this.colored(this.config(path, ""));
    }

    public String colored(String str) {
        return ChatColor.translateAlternateColorCodes((char)'&', (String)str);
    }

    public String colorConfig(String path, String def) {
        String res = this.colorConfig(path);
        return res == null ? def : res;
    }

    public void onDisable() {
        this.liveCommandExecutor.save();
        this.saveConfig();
    }

    public void reload() {
        this.reloadConfig();
        if (this.asciiArtCommandExecutor != null) {
            this.getLogger().info("Reloading ascii");
            this.asciiArtCommandExecutor.load();
        }
    }
}

