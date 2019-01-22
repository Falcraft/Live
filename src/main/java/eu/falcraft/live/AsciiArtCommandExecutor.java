package eu.falcraft.live;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class AsciiArtCommandExecutor
implements CommandExecutor,
TabCompleter {
    private final LivePlugin plugin;
    private final Map<String, String> emojis;

    public AsciiArtCommandExecutor(LivePlugin p) {
        this.plugin = p;
        this.emojis = new HashMap<String, String>();
    }

    public void load() {
        String path = "emojis";
        this.emojis.clear();
        this.plugin.configList("emojis").forEach(k -> this.emojis.put((String)k, this.plugin.config("emojis." + k, "><")));
    }

    private TextComponent listEmojis() {
        return this.emojis.keySet().stream().map(k -> {
            TextComponent temp = new TextComponent(k + ": " + this.emojis.get(k));
            temp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(k).create()));
            temp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/aa " + k));
            return temp;
        }).reduce((a, b) -> new TextComponent(new BaseComponent[]{a, new TextComponent("\n"), b})).orElse(new TextComponent());
    }

    private TextComponent stylizeEmoji(String key, String senderName) {
        TextComponent tc = new TextComponent(senderName + (Object)ChatColor.WHITE + ": " + this.emojis.get(key));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(key).create()));
        return tc;
    }

    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if (strings.length == 0) {
            cs.sendMessage("usage: /aa <emoticon name>");
            return false;
        }
        if ("list".equalsIgnoreCase(strings[0])) {
            cs.spigot().sendMessage((BaseComponent)this.listEmojis());
            return true;
        }
        String selected = strings[0];
        if (!this.emojis.containsKey(selected)) {
            cs.sendMessage("Emoji '" + strings[0] + "' not found");
            return false;
        }
        String name = cs instanceof Player ? this.plugin.getUser((Player)cs).getNickname() : cs.getName();
        this.plugin.getServer().spigot().broadcast((BaseComponent)this.stylizeEmoji(strings[0], name));
        return true;
    }

    public List<String> onTabComplete(CommandSender cs, Command cmnd, String string, String[] strings) {
        String arg = strings.length > 0 ? strings[0] : "";
        return this.emojis.keySet().stream().filter(s -> s.startsWith(arg)).collect(Collectors.toList());
    }
}

