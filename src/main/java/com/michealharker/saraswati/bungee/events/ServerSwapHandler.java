package com.michealharker.saraswati.bungee.events;

import com.michealharker.saraswati.bungee.BungeePlugin;
import com.michealharker.saraswati.messages.BungeeMessage;
import com.michealharker.saraswati.messages.BungeeMessageType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerSwapHandler implements Listener {
    private BungeePlugin plugin;

    public ServerSwapHandler(BungeePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerSwitch(ServerConnectEvent e) {
        if (e.getPlayer().getServer() != null) {
            String msg = this.plugin.getConfig().getString("switch-message");

            // String replacements!
            msg = msg.replace("{player}", e.getPlayer().getName());
            msg = msg.replace("{uuid}", e.getPlayer().getUniqueId().toString());
            msg = msg.replace("{oldserver}", e.getPlayer().getServer().getInfo().getName());
            msg = msg.replace("{newserver}", e.getTarget().getName());

            msg = ChatColor.translateAlternateColorCodes('&', msg);

            BungeeMessage bm = new BungeeMessage(e.getPlayer().getUniqueId(), msg, BungeeMessageType.MISC, null);

            this.plugin.getIRC().relay(bm);
            this.plugin.sendPluginMessage(bm);
        }
    }
}
