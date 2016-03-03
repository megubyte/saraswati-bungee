package com.michealharker.saraswati.bungee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import com.michealharker.saraswati.bungee.events.LoginHandler;
import com.michealharker.saraswati.bungee.events.LogoutEvent;
import com.michealharker.saraswati.bungee.events.ServerSwapHandler;
import com.michealharker.saraswati.messages.BungeeMessage;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeePlugin extends Plugin {
	private IRCBot irc;

	public void onEnable() {
		this.irc = new IRCBot(this);
		this.irc.run();
		
		this.getProxy().getPluginManager().registerListener(this, new BungeeMessageHandler(this));
		this.getProxy().getPluginManager().registerListener(this, new LoginHandler(this));
		this.getProxy().getPluginManager().registerListener(this, new LogoutEvent(this));
		this.getProxy().getPluginManager().registerListener(this, new ServerSwapHandler(this));

		this.getProxy().registerChannel("BungeeCord");
	}
	
	public void onDisable() {
		
	}

	public void sendPluginMessage(BungeeMessage message) {
		for (ServerInfo s : this.getProxy().getServers().values()) {
			s.sendData("BungeeCord", message.toByteArray());
		}
	}
	
	public IRCBot getIRC() {
		return this.irc;
	}

	public Configuration getConfig() {
		File f = new File(this.getDataFolder(), "");
		
		if (!f.exists()) {
			f.mkdir();
		}
		
		f = new File(this.getDataFolder(), "/config.yml");
		if (!f.exists()) {
			try {
				InputStream in = getClass().getResourceAsStream("/config.yml");
				Files.copy(in, f.toPath());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		ConfigurationProvider p = ConfigurationProvider.getProvider(YamlConfiguration.class);
		
		try {
			return p.load(f);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
}
