package com.michealharker.saraswati.bungee;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.michealharker.saraswati.messages.BungeeMessageType;
import net.md_5.bungee.api.ChatColor;
import org.jibble.pircbot.Colors;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;

import com.michealharker.saraswati.messages.BungeeMessage;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class IRCBot extends PircBot implements Runnable {
	private BungeePlugin plugin;
	private List<String> channels;

	public IRCBot(BungeePlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		String nick = this.plugin.getConfig().getString("bot.nick", "saraswati");
		String host = this.plugin.getConfig().getString("bot.host", "irc.freenode.net");
		int port = this.plugin.getConfig().getInt("bot.port", 6697);
		
		try {
			this.plugin.getLogger().info("Connecting to IRC");
			this.setName(nick);
			this.setLogin("saraswati");
			this.setEncoding("UTF8");
			this.connect(host, port);
			this.plugin.getLogger().info("Connected to IRC");
		} catch (IrcException | IOException ex) {
			this.plugin.getLogger().warning("Failed to connect to IRC:");
			ex.printStackTrace();
		}
		
		this.channels = this.plugin.getConfig().getStringList("bot.channels");
		
		for(String c : this.channels) {
			this.joinChannel(c);
		}
	}
	
	public void disable() {
		this.disconnect();
	}
	
	public void relay(BungeeMessage message) {
		for (String c : this.channels) {
			String msg = this.ircColorify(message.message);
			this.sendMessage(c, msg);
		}
	}

	@Override
	protected void onJoin(String channel, String sender, String login, String hostname) {
		String ircmsg = this.plugin.getConfig().getString("irc-relay.join");

		ircmsg = ChatColor.translateAlternateColorCodes('&', ircmsg);

		ircmsg = ircmsg.replace("{nick}", sender);
		ircmsg = ircmsg.replace("{channel}", channel);

		BungeeMessage bm = new BungeeMessage(null, ircmsg, BungeeMessageType.IRC_MESSAGE, null);
		this.plugin.sendPluginMessage(bm);
	}

	@Override
	protected void onPart(String channel, String sender, String login, String hostname) {
		String ircmsg = this.plugin.getConfig().getString("irc-relay.part");

		ircmsg = ChatColor.translateAlternateColorCodes('&', ircmsg);

		ircmsg = ircmsg.replace("{nick}", sender);
		ircmsg = ircmsg.replace("{channel}", channel);

		BungeeMessage bm = new BungeeMessage(null, ircmsg, BungeeMessageType.IRC_MESSAGE, null);
		this.plugin.sendPluginMessage(bm);
	}

	@Override
	protected void onMessage(String channel, String sender, String login, String hostname, String message) {
		if (message.equalsIgnoreCase(this.plugin.getConfig().getString("bot.prefix") + "p")) {
			if (this.plugin.getProxy().getOnlineCount() > 0) {
				String names = "";
				Collection<ProxiedPlayer> p = this.plugin.getProxy().getPlayers();
				
				for (ProxiedPlayer pl : p) {
					if (p != null)
						names += pl.getName() + " ";
				}
				
				this.sendMessage(channel, Colors.BOLD + "Players online: " + Colors.NORMAL + names);
			} else {
				this.sendMessage(channel, "There's nobody online right now.");
			}
		}

		String ircmsg = this.plugin.getConfig().getString("irc-relay.message");

		ircmsg = ChatColor.translateAlternateColorCodes('&', ircmsg);

		ircmsg = ircmsg.replace("{nick}", sender);
		ircmsg = ircmsg.replace("{message}", message);
		ircmsg = ircmsg.replace("{channel}", channel);

		BungeeMessage bm = new BungeeMessage(null, ircmsg, BungeeMessageType.IRC_MESSAGE, null);
		this.plugin.sendPluginMessage(bm);
	}

	private String ircColorify(String message) {
		message = message.replace("§0", Colors.NORMAL);
		message = message.replace("§1", Colors.DARK_BLUE);
		message = message.replace("§2", Colors.DARK_GREEN);
		message = message.replace("§3", Colors.CYAN);
		message = message.replace("§4", Colors.RED);
		message = message.replace("§5", Colors.PURPLE);
		message = message.replace("§6", Colors.OLIVE);
		message = message.replace("§7", Colors.LIGHT_GRAY);
		message = message.replace("§8", Colors.DARK_GRAY);
		message = message.replace("§9", Colors.BLUE);
		message = message.replace("§a", Colors.GREEN);
		message = message.replace("§b", Colors.CYAN);
		message = message.replace("§c", Colors.RED);
		message = message.replace("§d", Colors.MAGENTA);
		message = message.replace("§e", Colors.YELLOW);
		message = message.replace("§f", Colors.NORMAL);
		message = message.replace("§l", Colors.BOLD);
		message = message.replace("§n", Colors.UNDERLINE);
		message = message.replace("§r", Colors.NORMAL);
		
		return message;
	}
}
