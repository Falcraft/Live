/*
* MIT License
* 
* Copyright (c) 2017-2019 Azarias Boutin
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/

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

public class AsciiArtCommandExecutor implements CommandExecutor, TabCompleter {
    private final LivePlugin plugin;
    private final Map<String, String> emojis;
    private final IUserFactory mUserFactory;

    public AsciiArtCommandExecutor(LivePlugin p, IUserFactory factory) {
        this.plugin = p;
        this.mUserFactory = factory;
        this.emojis = new HashMap<String, String>();
    }

    public void load() {
        this.emojis.clear();
        this.plugin.configList("emojis")
                .forEach(k -> this.emojis.put((String) k, this.plugin.config("emojis." + k, "><")));
    }

    private TextComponent listEmojis() {
        return this.emojis.keySet().stream().map(k -> {
            TextComponent temp = new TextComponent(k + ": " + this.emojis.get(k));
            temp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(k).create()));
            temp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/aa " + k));
            return temp;
        }).reduce((a, b) -> new TextComponent(new BaseComponent[] { a, new TextComponent("\n"), b }))
                .orElse(new TextComponent());
    }

    private TextComponent stylizeEmoji(String key, String senderName) {
        TextComponent tc = new TextComponent(senderName + (Object) ChatColor.WHITE + ": " + this.emojis.get(key));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(key).create()));
        return tc;
    }

    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if (strings.length == 0) {
            cs.sendMessage("usage: /aa <emoticon name>");
            return false;
        }
        if ("list".equalsIgnoreCase(strings[0])) {
            cs.spigot().sendMessage((BaseComponent) this.listEmojis());
            return true;
        }
        String selected = strings[0];
        if (!this.emojis.containsKey(selected)) {
            cs.sendMessage("Emoji '" + strings[0] + "' not found");
            return false;
        }
        String name = cs instanceof Player ? this.mUserFactory.getUser((Player) cs).getNickName() : cs.getName();
        this.plugin.getServer().spigot().broadcast((BaseComponent) this.stylizeEmoji(strings[0], name));
        return true;
    }

    public List<String> onTabComplete(CommandSender cs, Command cmnd, String string, String[] strings) {
        String arg = strings.length > 0 ? strings[0] : "";
        return this.emojis.keySet().stream().filter(s -> s.startsWith(arg)).collect(Collectors.toList());
    }
}
