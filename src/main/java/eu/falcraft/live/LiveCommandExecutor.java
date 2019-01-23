package eu.falcraft.live;

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
import java.util.logging.Level;

import com.earth2me.essentials.User;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.ess3.api.events.NickChangeEvent;
import net.md_5.bungee.api.ChatColor;

public class LiveCommandExecutor
implements CommandExecutor,
Listener {
    private final LivePlugin plugin;
    private final Gson gson;
    private Map<String, LiveData> livePlayers;

    public LiveCommandExecutor(LivePlugin p) {
        this.plugin = p;
        this.livePlayers = new HashMap<String, LiveData>();
        this.gson = new GsonBuilder().enableComplexMapKeySerialization().create();
    }

    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if ("live".equalsIgnoreCase(cmnd.getName())) {
            if (strings.length == 1) {
                String arg = strings[0];
                switch (arg.toUpperCase()) {
                    case "HELP": {
                        return this.help(cs);
                    }
                    case "RELOAD": {
                        if (!this.sendMessageWithPermission(cs, "live.reload", "Reloading..")) break;
                        return this.reload();
                    }
                }
                User u = this.plugin.getUser(arg);
                if (u != null && this.sendMessageWithPermission(cs, "live.toggle", "Toggle live")) {
                    return this.togglePlayerLive(u);
                }
            } else {
                return this.toggleLive(cs, strings);
            }
        }
        return false;
    }

    private boolean help(CommandSender cs) {
        ArrayList<String> res = new ArrayList<String>();
        if (cs.hasPermission("live.reload")) {
            res.add("&2/live reload &r: reloads the plugin configuration");
        }
        if (cs.hasPermission("live.toggle")) {
            res.add("&2/live <pseudo> &r: toggles live state for the given player");
        }
        if (cs.hasPermission("live.link.get")) {
            res.add("&2/live link <pseudo> &r: get url to player's channel");
        }
        if (cs instanceof Player) {
            if (cs.hasPermission("live.link.set")) {
                res.add("&2/live link <url> &r: sets the url for your channel");
            }
            if (cs.hasPermission("live.use")) {
                res.add("&2/live &r: toggles the live");
            }
        }
        cs.sendMessage(this.plugin.colored(String.join((CharSequence)"\n", res)));
        return true;
    }

    private boolean reload() {
        this.plugin.reload();
        return true;
    }

    public void load() throws IOException {
        Path path = this.getLiveFile();
        if (Files.exists(path, new LinkOption[0])) {
            this.livePlayers = this.getLivePlayers(path);
        } else {
            this.plugin.getLogger().log(Level.INFO, "Live file not found");
        }
    }

    public void save() {
        try {
            String json = this.gson.toJson(this.livePlayers);
            Path f = this.getLiveFile();
            if (!Files.exists(f, new LinkOption[0])) {
                Files.createFile(f, new FileAttribute[0]);
            }
            Files.write(f, json.getBytes(), new OpenOption[0]);
        }
        catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    private boolean sendMessageWithPermission(CommandSender cs, String permName, String message) {
        boolean hasPerm = cs.hasPermission(permName);
        if (!hasPerm) {
            cs.sendMessage(this.plugin.colorConfig("permission_denied", "Permission denied"));
        } else {
            cs.sendMessage(message);
        }
        return hasPerm;
    }

    private Map<String, LiveData> getLivePlayers(Path f) {
        try {
            String fileContent = new String(Files.readAllBytes(f));
            Type typeOfMap = new TypeToken<Map<String, LiveData>>(){
				private static final long serialVersionUID = 1L;}.getType();
            Type typeOfMap2 = typeOfMap;
            return (Map) this.gson.fromJson(fileContent, typeOfMap2);
        }
        catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, null, ex);
            return new HashMap<String, LiveData>();
        }
    }

    private Path getLiveFile() throws IOException {
        Path path = this.plugin.getDataFolder().toPath();
        if (!Files.exists(path, new LinkOption[0])) {
            Files.createDirectory(path, new FileAttribute[0]);
        }
        return path.resolve("live.json");
    }

    private boolean toggleLive(CommandSender cs, String[] args) {
        boolean ret = false;
        if (args.length > 0) {
            ret = args[0].equalsIgnoreCase("link") && args.length > 1 ? this.link(cs, args[1]) : false;
        } else if (cs.hasPermission("live.use") && cs instanceof Player) {
            ret = this.togglePlayerLive(this.plugin.getUser((Player)cs));
        }
        return ret;
    }

    private boolean link(CommandSender cs, String link) {
        boolean ret = false;
        User u = this.plugin.getUser(link);
        if (u != null) {
            LiveData ld = this.getLiveData(u);
            boolean bl = ret = ld != null && ld.getLiveLink() != null;
            if (!ret) {
                cs.sendMessage(this.plugin.colorConfig("inform.no_link", "Link not found"));
            } else if (cs.hasPermission("live.link.get")) {
                String s = this.plugin.colorConfig("inform.see_link", "~").replaceAll("~", ld.getLiveLink());
                this.sendMessageWithPermission(cs, "live.link.get", s);
            }
        } else if (cs.hasPermission("live.link.set") && cs instanceof Player) {
            ret = this.setLiveLink((Player)cs, link);
        } else {
            cs.sendMessage((Object)ChatColor.RED + "Operation not possible");
        }
        return ret;
    }

    private boolean setLiveLink(Player p, String url) {
        try {
            new URL(url);
            this.getLiveData(this.plugin.getUser(p)).setLiveLink(url);
            String dfltMessage = (Object)ChatColor.GREEN + "Link set";
            p.sendMessage(this.plugin.colorConfig("inform.link_set", dfltMessage));
            return true;
        }
        catch (MalformedURLException ex) {
            String dfltMessage = (Object)ChatColor.RED + "Invalid URL";
            p.sendMessage(this.plugin.colorConfig("inform.invalid_ur", dfltMessage));
            return false;
        }
    }

    private boolean togglePlayerLive(User u) {
        boolean isLive = this.isLive((AnimalTamer)u.getBase());
        LiveData ld = this.getLiveData(u);
        if (!isLive) {
            ld.setOriginalNick(u.getNickname());
        }
        ld.setIsLive(!isLive);
        u.setNickname(isLive ? ld.getOriginalNick() : this.stamped(u));
        u.setDisplayNick();
        this.informLiveToggle(ld, isLive ? "inform.end" : "inform.start");
        return true;
    }

    private void informLiveToggle(LiveData cs, String informPath) {
        if (this.plugin.getConfig().getBoolean(informPath + ".active")) {
            String msgPath = cs.getLiveLink() != null ? ".message_link" : ".message";
            String msg = this.plugin.colorConfig(informPath + msgPath, "");
            if (msg.isEmpty()) {
                msg = this.plugin.colorConfig(informPath + ".message", "");
            }
            msg = msg.replaceAll("~", cs.getOriginalNick()).replaceAll("#", cs.getLiveLink());
            this.plugin.getServer().broadcastMessage(this.plugin.colored(msg));
        }
    }

    @EventHandler
    public void onNickChange(NickChangeEvent ev) {
        User us = this.plugin.getUser(ev.getAffected().getBase());
        if (this.isLive((AnimalTamer)us.getBase())) {
            ev.setCancelled(true);
            this.getLiveData(us).setOriginalNick(ev.getValue());
            User affected = this.plugin.getUser(ev.getAffected().getBase().getUniqueId());
            affected.setNickname(this.stamped(ev.getValue()));
            affected.setDisplayNick();
        }
    }

    private String stamped(User u) {
        return this.stamped(u.getNickname());
    }

    private String stamped(String s) {
        return String.format("%s%s%s", new Object[]{this.plugin.colorConfig("stamp"), ChatColor.RESET, s});
    }

    private LiveData getLiveData(User uid) {
        String str = uid.getBase().getUniqueId().toString();
        if (!this.livePlayers.containsKey(str)) {
            this.livePlayers.put(str, new LiveData(uid.getNickname()));
        }
        LiveData d = this.livePlayers.get(str);
        return d;
    }

    private boolean isLive(AnimalTamer p) {
        String UUID2 = p.getUniqueId().toString();
        return this.livePlayers.containsKey(UUID2) && this.livePlayers.get(UUID2).isLive();
    }

}

