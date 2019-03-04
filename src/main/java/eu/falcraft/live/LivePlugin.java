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

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

import com.earth2me.essentials.IEssentials;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public final class LivePlugin extends JavaPlugin {
    private LiveCommandExecutor liveCommandExecutor;
    private AsciiArtCommandExecutor asciiArtCommandExecutor;

    public void onEnable() {
        this.saveDefaultConfig();
        try {
            IEssentials ess = (IEssentials) this.getServer().getPluginManager().getPlugin("Essentials");
            IUserFactory userFactory = ess == null ? new DefaultUserFactory() : new EssentialsUserFactory(ess);

            this.liveCommandExecutor = new LiveCommandExecutor(this, userFactory);
            this.getCommand("live").setExecutor((CommandExecutor) this.liveCommandExecutor);
            this.liveCommandExecutor.load();
            this.getServer().getPluginManager().registerEvents((Listener) this.liveCommandExecutor, (Plugin) this);
            this.asciiArtCommandExecutor = new AsciiArtCommandExecutor(this, userFactory);
            this.getCommand("aa").setExecutor((CommandExecutor) this.asciiArtCommandExecutor);
            this.asciiArtCommandExecutor.load();
            this.getCommand("aa").setTabCompleter((TabCompleter) this.asciiArtCommandExecutor);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, null, ex);
        }
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
        return ChatColor.translateAlternateColorCodes((char) '&', (String) str);
    }

    public String colorConfig(String path, String def) {
        String res = this.colorConfig(path);
        return res == null ? def : res;
    }

    public void onDisable() {
        this.liveCommandExecutor.unload();
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
