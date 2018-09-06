package fr.azarias.live;

import com.earth2me.essentials.User;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.ess3.api.IEssentials;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;




















public final class LivePlugin
  extends JavaPlugin
{
  private LiveCommandExecutor liveCommandExecutor;
  private AsciiArtCommandExecutor asciiArtCommandExecutor;
  private IEssentials ess;
  
  public LivePlugin() {}
  
  public void onEnable()
  {
    saveDefaultConfig();
    try
    {
      getCommand("live").setExecutor(this.liveCommandExecutor = new LiveCommandExecutor(this));
      liveCommandExecutor.load();
      getServer().getPluginManager().registerEvents(liveCommandExecutor, this);
      

      getCommand("aa").setExecutor(this.asciiArtCommandExecutor = new AsciiArtCommandExecutor(this));
      asciiArtCommandExecutor.load();
      getCommand("aa").setTabCompleter(asciiArtCommandExecutor);
      ess = ((IEssentials)getServer().getPluginManager().getPlugin("Essentials"));
    } catch (IOException ex) {
      getLogger().log(Level.SEVERE, null, ex);
    }
  }
  
  public User getUser(Player p) {
    return ess.getUser(p);
  }
  
  public User getUser(String name) {
    return ess.getUser(name);
  }
  
  public User getUser(UUID uuid) {
    return ess.getUser(uuid);
  }
  






  public String config(String cName, String def)
  {
    String conf = getConfig().getString(cName);
    return conf == null ? def : conf;
  }
  





  public Set<String> configList(String cName)
  {
    return getConfig().getConfigurationSection(cName).getKeys(true);
  }
  





  public String colorConfig(String path)
  {
    return colored(config(path, ""));
  }
  
  public String colored(String str) {
    return ChatColor.translateAlternateColorCodes('&', str);
  }
  
  public String colorConfig(String path, String def) {
    String res = colorConfig(path);
    return res == null ? def : res;
  }
  
  public void onDisable()
  {
    liveCommandExecutor.save();
    saveConfig();
  }
  
  public void reload() {
    reloadConfig();
    if (asciiArtCommandExecutor != null) {
      getLogger().info("Reloading ascii");
      asciiArtCommandExecutor.load();
    }
  }
}
