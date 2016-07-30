package com.michealharker.saraswati.irc;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

//import org.jibble.pircbot.IrcException;
//import org.jibble.pircbot.PircBot;

import com.michealharker.saraswati.bungee.BungeePlugin;
import com.michealharker.saraswati.messages.BungeeMessage;
import com.michealharker.saraswati.messages.BungeeMessageType;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SaraswatiIRC extends GenericIRCBot {

	private final BungeePlugin plugin;
	private final List<String> channels;
	private final List<String> moderated;

	protected SaraswatiIRC(InetSocketAddress socketAddr, boolean useSsl, IRCConnectionInfo info,
			@Nullable String servPassword, BungeePlugin plugin) {
		super(socketAddr, useSsl, info, servPassword);
		this.plugin = plugin;

		@Nullable
		List<String> confchannels = plugin.getConfig().getStringList("bot.channels");
		if (confchannels == null)
			throw new IllegalStateException("list of channels must not be null");
		this.channels = confchannels;
		this.moderated = new ArrayList<>();
	}

	public static SaraswatiIRC makeFromPlugin(BungeePlugin plugin) {
		final String nick = plugin.getConfig().getString("bot.nick", "saraswati");
		final String host = plugin.getConfig().getString("bot.host", "irc.freenode.net");
		final int port = plugin.getConfig().getInt("bot.port", 6697);
		final boolean ssl = plugin.getConfig().getBoolean("bot.ssl", true);
		final String servPassword = plugin.getConfig().getString("bot.servpassword", null);

		final InetSocketAddress addr = new InetSocketAddress(host, port);

		if (nick == null)
			throw new IllegalStateException("nick must not be null");
		final IRCConnectionInfo connInfo = new IRCConnectionInfo(nick, "saraswati", "saraswati irc bot");

		return new SaraswatiIRC(addr, ssl, connInfo, servPassword, plugin);
	}

	@Override
	protected void onConnect() {
		for (String c : this.channels) {
			if (c != null)
				this.joinChannel(c);
		}
	}

	@Override
	protected void logMessage(String msg) {
		this.plugin.getLogger().info(msg);
	}

	public void disable() {
		this.disconnect();
	}

	public void relay(BungeeMessage message) {
		final @Nullable String mcMsg = message.message;
		if (mcMsg != null) {
			for (String c : this.channels) {
				final String msg = ircColorify(mcMsg);
				if (!this.moderated.contains(c)) {
					if (c != null)
						this.sendMessage(c, msg);
				}
			}
		}
	}

	@Override
	protected void onSetModerated(String channel) {
		final BungeeMessage bm = new BungeeMessage(null, "", BungeeMessageType.MODERATED, Boolean.TRUE);
		this.plugin.sendPluginMessage(bm);
		this.moderated.add(channel);
	}

	@Override
	protected void onRemoveModerated(String channel) {
		final BungeeMessage bm = new BungeeMessage(null, "", BungeeMessageType.MODERATED, Boolean.FALSE);
		this.plugin.sendPluginMessage(bm);
		this.moderated.remove(channel);
	}

	@Override
	protected void onKick(String channel, String opsender, String victim, String reason) {
		String ircmsg = this.plugin.getConfig().getString("irc-relay.kick");

		ircmsg = ChatColor.translateAlternateColorCodes('&', ircmsg);

		ircmsg = ircmsg.replace("{channel}", channel);
		ircmsg = ircmsg.replace("{nick}", opsender);
		ircmsg = ircmsg.replace("{victim}", victim);
		ircmsg = ircmsg.replace("{reason}", reason);

		final BungeeMessage bm = new BungeeMessage(null, ircmsg, BungeeMessageType.IRC_KICK, null);
		this.plugin.sendPluginMessage(bm);
	}

	@Override
	protected void onAction(String channel, String sender, String action) {
		String ircmsg = this.plugin.getConfig().getString("irc-relay.me");

		ircmsg = ChatColor.translateAlternateColorCodes('&', ircmsg);

		ircmsg = ircmsg.replace("{nick}", sender);
		ircmsg = ircmsg.replace("{action}", action);

		final BungeeMessage bm = new BungeeMessage(null, ircmsg, BungeeMessageType.IRC_MESSAGE, null);
		this.plugin.sendPluginMessage(bm);
	}

	@Override
	protected void onQuit(String sender, String reason) {
		String ircmsg = this.plugin.getConfig().getString("irc-relay.quit");

		ircmsg = ChatColor.translateAlternateColorCodes('&', ircmsg);

		ircmsg = ircmsg.replace("{nick}", sender);
		ircmsg = ircmsg.replace("{reason}", reason);

		final BungeeMessage bm = new BungeeMessage(null, ircmsg, BungeeMessageType.IRC_QUIT, null);
		this.plugin.sendPluginMessage(bm);
	}

	@Override
	protected void onJoin(String channel, String sender) {
		String ircmsg = this.plugin.getConfig().getString("irc-relay.join");

		ircmsg = ChatColor.translateAlternateColorCodes('&', ircmsg);

		ircmsg = ircmsg.replace("{nick}", sender);
		ircmsg = ircmsg.replace("{channel}", channel);

		final BungeeMessage bm = new BungeeMessage(null, ircmsg, BungeeMessageType.IRC_MESSAGE, null);
		this.plugin.sendPluginMessage(bm);
	}

	@Override
	protected void onPart(String channel, String sender) {
		String ircmsg = this.plugin.getConfig().getString("irc-relay.part");

		ircmsg = ChatColor.translateAlternateColorCodes('&', ircmsg);

		ircmsg = ircmsg.replace("{nick}", sender);
		ircmsg = ircmsg.replace("{channel}", channel);

		BungeeMessage bm = new BungeeMessage(null, ircmsg, BungeeMessageType.IRC_MESSAGE, null);
		this.plugin.sendPluginMessage(bm);
	}

	@Override
	protected void onMessage(String channel, String sender, String message) {
		if (message.equalsIgnoreCase(this.plugin.getConfig().getString("bot.prefix") + "p")) {
			if (this.plugin.getProxy().getOnlineCount() > 0) {
				String names = "";
				Collection<ProxiedPlayer> p = this.plugin.getProxy().getPlayers();

				for (ProxiedPlayer pl : p) {
					if (pl != null)
						names += pl.getName() + " ";
				}

				this.sendMessage(channel, MircColors.BOLD + "Players online: " + MircColors.NORMAL + names);
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

	@SuppressWarnings("null") // this should be safe.
	private static String ircColorify(String message) {
		return message//
				.replace("§0", MircColors.NORMAL)//
				.replace("§1", MircColors.BLUE)//
				.replace("§2", MircColors.GREEN)//
				.replace("§3", MircColors.LIGHT_CYAN)//
				.replace("§4", MircColors.LIGHT_RED)//
				.replace("§5", MircColors.PURPLE)//
				.replace("§6", MircColors.ORANGE)//
				.replace("§7", MircColors.LIGHT_GRAY)//
				.replace("§8", MircColors.GRAY)//
				.replace("§9", MircColors.LIGHT_BLUE)//
				.replace("§a", MircColors.LIGHT_GREEN)//
				.replace("§b", MircColors.LIGHT_CYAN)//
				.replace("§c", MircColors.LIGHT_RED)//
				.replace("§d", MircColors.PINK)//
				.replace("§e", MircColors.YELLOW)//
				.replace("§f", MircColors.NORMAL)//
				.replace("§l", MircColors.BOLD)//
				.replace("§n", MircColors.UNDERLINE)//
				.replace("§r", MircColors.NORMAL);
	}
}
