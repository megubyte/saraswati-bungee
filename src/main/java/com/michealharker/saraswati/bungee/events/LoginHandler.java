package com.michealharker.saraswati.bungee.events;

import com.michealharker.saraswati.bungee.BungeePlugin;
import com.michealharker.saraswati.messages.BungeeMessage;
import com.michealharker.saraswati.messages.BungeeMessageType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LoginHandler implements Listener {
    private BungeePlugin plugin;

    public LoginHandler(BungeePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent e) {
        String connect = this.plugin.getConfig().getString("connect-message");

        // String replacements!
        connect = connect.replace("{player}", e.getPlayer().getName());
        connect = connect.replace("{uuid}", e.getPlayer().getUniqueId().toString());
        connect = ChatColor.translateAlternateColorCodes('&', connect);

        BungeeMessage bm = new BungeeMessage(e.getPlayer().getUniqueId(), connect, BungeeMessageType.PLAYER_JOIN, null);

        this.plugin.getIRC().relay(bm);
    }
}
