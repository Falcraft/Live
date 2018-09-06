package fr.azarias.live;

import com.earth2me.essentials.User;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.ess3.api.IUser;
import net.ess3.api.events.NickChangeEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;



























public class LiveCommandExecutor
  implements CommandExecutor, Listener
{
  private final LivePlugin plugin;
  private final Gson gson;
  private Map<String, LiveData> livePlayers;
  
  public LiveCommandExecutor(LivePlugin p)
  {
    plugin = p;
    livePlayers = new HashMap();
    gson = new GsonBuilder().enableComplexMapKeySerialization().create();
  }
  










  public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings)
  {
    if ("live".equalsIgnoreCase(cmnd.getName())) {
      if (strings.length == 1) {
        String arg = strings[0];
        switch (arg.toUpperCase()) {
        case "HELP": 
          return help(cs);
        case "RELOAD": 
          if (sendMessageWithPermission(cs, "live.reload", "Reloading.."))
            return reload();
          break; }
        User u;
        if (((u = plugin.getUser(arg)) != null) && 
          (sendMessageWithPermission(cs, "live.toggle", "Toggle live"))) {
          return togglePlayerLive(u);
        }
      }
      else {
        return toggleLive(cs, strings);
      }
    }
    return false;
  }
  





  private boolean help(CommandSender cs)
  {
    ArrayList<String> res = new ArrayList();
    if (cs.hasPermission("live.reload")) {
      res.add("&2/live reload &r: reloads the plugin configuration");
    }
    if (cs.hasPermission("live.toggle")) {
      res.add("&2/live <pseudo> &r: toggles live state for the given player");
    }
    
    if (cs.hasPermission("live.link.get")) {
      res.add("&2/live link <pseudo> &r: get url to player's channel");
    }
    if ((cs instanceof Player)) {
      if (cs.hasPermission("live.link.set")) {
        res.add("&2/live link <url> &r: sets the url for your channel");
      }
      if (cs.hasPermission("live.use")) {
        res.add("&2/live &r: toggles the live");
      }
    }
    cs.sendMessage(plugin.colored(String.join("\n", res)));
    return true;
  }
  
  private boolean reload() {
    plugin.reload();
    return true;
  }
  



  public void load()
    throws IOException
  {
    Path path = getLiveFile();
    if (Files.exists(path, new LinkOption[0])) {
      livePlayers = getLivePlayers(path);
    } else {
      plugin.getLogger().log(Level.INFO, "Live file not found");
    }
  }
  


  public void save()
  {
    try
    {
      String json = gson.toJson(livePlayers);
      Path f = getLiveFile();
      if (!Files.exists(f, new LinkOption[0])) {
        Files.createFile(f, new FileAttribute[0]);
      }
      Files.write(f, json.getBytes(), new OpenOption[0]);
    } catch (IOException ex) {
      plugin.getLogger().log(Level.SEVERE, null, ex);
    }
  }
  
  private boolean sendMessageWithPermission(CommandSender cs, String permName, String message) {
    boolean hasPerm = cs.hasPermission(permName);
    if (!hasPerm) {
      cs.sendMessage(plugin.colorConfig("permission_denied", "Permission denied"));
    } else {
      cs.sendMessage(message);
    }
    return hasPerm;
  }
  





  private Map<String, LiveData> getLivePlayers(Path f)
  {
    try
    {
      String fileContent = new String(Files.readAllBytes(f));
      
      Type typeOfMap = new TypeToken() {}.getType();
      return (Map)gson.fromJson(fileContent, typeOfMap);
    } catch (IOException ex) {
      plugin.getLogger().log(Level.SEVERE, null, ex);
    }
    return new HashMap();
  }
  






  private Path getLiveFile()
    throws IOException
  {
    Path path = plugin.getDataFolder().toPath();
    if (!Files.exists(path, new LinkOption[0])) {
      Files.createDirectory(path, new FileAttribute[0]);
    }
    return path.resolve("live.json");
  }
  






  private boolean toggleLive(CommandSender cs, String[] args)
  {
    boolean ret = false;
    if (args.length > 0) {
      if ((args[0].equalsIgnoreCase("link")) && (args.length > 1)) {
        ret = link(cs, args[1]);
      } else {
        ret = false;
      }
    } else if ((cs.hasPermission("live.use")) && ((cs instanceof Player))) {
      ret = togglePlayerLive(plugin.getUser((Player)cs));
    }
    return ret;
  }
  
  private boolean link(CommandSender cs, String link) {
    boolean ret = false;
    User u = plugin.getUser(link);
    if (u != null) {
      LiveData ld = getLiveData(u);
      ret = (ld != null) && (ld.getLiveLink() != null);
      if (!ret) {
        cs.sendMessage(plugin.colorConfig("inform.no_link", "Link not found"));
      } else if (cs.hasPermission("live.link.get")) {
        String s = plugin.colorConfig("inform.see_link", "~").replaceAll("~", ld.getLiveLink());
        sendMessageWithPermission(cs, "live.link.get", s);
      }
    } else if ((cs.hasPermission("live.link.set")) && ((cs instanceof Player))) {
      ret = setLiveLink((Player)cs, link);
    } else {
      cs.sendMessage(ChatColor.RED + "Operation not possible");
    }
    return ret;
  }
  
  private boolean setLiveLink(Player p, String url) {
    try {
      new URL(url);
      getLiveData(plugin.getUser(p)).setLiveLink(url);
      String dfltMessage = ChatColor.GREEN + "Link set";
      p.sendMessage(plugin.colorConfig("inform.link_set", dfltMessage));
      return true;
    } catch (MalformedURLException ex) {
      String dfltMessage = ChatColor.RED + "Invalid URL";
      p.sendMessage(plugin.colorConfig("inform.invalid_ur", dfltMessage)); }
    return false;
  }
  





  private boolean togglePlayerLive(User u)
  {
    boolean isLive = isLive(u.getBase());
    LiveData ld = getLiveData(u);
    if (!isLive) {
      ld.setOriginalNick(u.getNickname());
    }
    ld.setIsLive(!isLive);
    u.setNickname(isLive ? ld.getOriginalNick() : stamped(u));
    u.setDisplayNick();
    informLiveToggle(ld, isLive ? "inform.end" : "inform.start");
    return true;
  }
  





  private void informLiveToggle(LiveData cs, String informPath)
  {
    if (plugin.getConfig().getBoolean(informPath + ".active")) {
      String msgPath = cs.getLiveLink() != null ? ".message_link" : ".message";
      String msg = plugin.colorConfig(informPath + msgPath, "");
      if (msg.isEmpty()) {
        msg = plugin.colorConfig(informPath + ".message", "");
      }
      
      msg = msg.replaceAll("~", cs.getOriginalNick()).replaceAll("#", cs.getLiveLink());
      plugin.getServer().broadcastMessage(plugin.colored(msg));
    }
  }
  





  @EventHandler
  public void onNickChange(NickChangeEvent ev)
  {
    User us = plugin.getUser(ev.getAffected().getBase());
    if (isLive(us.getBase())) {
      ev.setCancelled(true);
      getLiveData(us).setOriginalNick(ev.getValue());
      User affected = plugin.getUser(ev.getAffected().getBase().getUniqueId());
      affected.setNickname(stamped(ev.getValue()));
      affected.setDisplayNick();
    }
  }
  





  private String stamped(User u)
  {
    return stamped(u.getNickname());
  }
  





  private String stamped(String s)
  {
    return String.format("%s%s%s", new Object[] { plugin.colorConfig("stamp"), ChatColor.RESET, s });
  }
  






  private LiveData getLiveData(User uid)
  {
    String str = uid.getBase().getUniqueId().toString();
    if (!livePlayers.containsKey(str)) {
      livePlayers.put(str, new LiveData(uid.getNickname()));
    }
    LiveData d = (LiveData)livePlayers.get(str);
    return d;
  }
  





  private boolean isLive(AnimalTamer p)
  {
    String UUID = p.getUniqueId().toString();
    return (livePlayers.containsKey(UUID)) && (((LiveData)livePlayers.get(UUID)).isLive());
  }
}
