package com.michealharker.saraswati.bungee.events;

import com.michealharker.saraswati.bungee.BungeePlugin;
import com.michealharker.saraswati.messages.BungeeMessage;
import com.michealharker.saraswati.messages.BungeeMessageType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LogoutEvent implements Listener {
    private BungeePlugin plugin;

    public LogoutEvent(BungeePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogoutEvent(PlayerDisconnectEvent e) {
        String disconnect = this.plugin.getConfig().getString("disconnect-message");

        // String replacements!
        disconnect = disconnect.replace("{player}", e.getPlayer().getName());
        disconnect = disconnect.replace("{uuid}", e.getPlayer().getUniqueId().toString());
        disconnect = ChatColor.translateAlternateColorCodes('&', disconnect);

        BungeeMessage bm = new BungeeMessage(e.getPlayer().getUniqueId(), disconnect, BungeeMessageType.PLAYER_QUIT, null);

        this.plugin.getIRC().relay(bm);
        this.plugin.sendPluginMessage(bm);
    }
}
