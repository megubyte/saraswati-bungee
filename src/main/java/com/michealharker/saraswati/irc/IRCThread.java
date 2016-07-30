package com.michealharker.saraswati.irc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import org.eclipse.jdt.annotation.Nullable;

final class IRCThread extends Thread {
	/**
	 * 
	 */
	private final GenericIRCBot bot;

	/**
	 * @param genericIRCBot
	 */
	IRCThread(GenericIRCBot genericIRCBot) {
		this.bot = genericIRCBot;
	}

	private static final String ACT_START = "\u0001ACTION ";

	@Override
	public void run() {
		while (this.bot.isConnecting()) {
			this.bot.logMessage("GenericIRCBot connecting...");
			try (Socket s = this.bot.makeSocket(this.bot.socketAddr, this.bot.useSsl);
					InputStream is = s.getInputStream();
					OutputStream os = s.getOutputStream();) {

				if (os == null)
					throw new IllegalStateException("OutputStream from socket must not be null");
				this.bot.setSocket(s, os);
				try {
					Thread.sleep(500);
				} catch (InterruptedException ie) {
					new RuntimeException("interrupted wait", ie).printStackTrace();
				}
				this.bot.logMessage("GenericIRCBot Sending pass-nick-user");
				final @Nullable String pw = this.bot.servPassword;
				if (pw != null && pw.length() > 0)
					os.write(("PASS " + this.bot.servPassword + "\r\n").getBytes(GenericIRCBot.utf8));
				os.write(("USER " + this.bot.info.getUsername() + " * * :" + this.bot.info.getGecos() + "\r\n")
						.getBytes(GenericIRCBot.utf8));
				os.write(("NICK " + this.bot.info.getNickname() + "\r\n").getBytes(GenericIRCBot.utf8));
				os.flush();
				this.bot.logMessage("GenericIRCBot sent pass-nick-user");
				synchronized (this.bot.lock) {
					this.bot.setConnected(true);
					this.bot.lock.notify();
				}
				this.bot.logMessage("GenericIRCBot connected.");
				this.bot.onConnect();

				BufferedReader in = new BufferedReader(new InputStreamReader(is, GenericIRCBot.utf8));

				String line;
				while (null != (line = in.readLine())) {
					try {
						if (line.matches("^:[^ ]+ 372 .*")) {
							// MOTD, ignore
						} else {
							this.bot.logMessage("[IRC] <-- " + line);
						}
						if (line.matches("^:[^ ]+ PRIVMSG #[^ ]* :.*")) {
							String[] parts = line.split(" ", 4);
							String nick = parts[0].substring(1).split("!")[0];
							String chan = parts[2];
							String msg = parts[3].substring(1);
							if (chan == null || nick == null || msg == null)
								throw new FormatFieldNotFoundException(line);
							if (msg.startsWith(ACT_START)) {
								final @Nullable String action = msg.substring(ACT_START.length(), msg.length() - 1);
								if (action != null)
									this.bot.onAction(chan, nick, action);
							} else {
								this.bot.onMessage(chan, nick, msg);
							}
						} else if (line.matches("^:[^ ]+ KICK #[^ ]* [^ ]+ :.*")) {
							String[] parts = line.split(" ", 5);
							String nick = parts[0].substring(1).split("!")[0];
							String chan = parts[2];
							String victim = parts[3];
							String msg = parts[4].substring(1);
							if (chan == null || nick == null || victim == null || msg == null)
								throw new FormatFieldNotFoundException(line);
							this.bot.onKick(chan, nick, victim, msg);
						} else if (line.matches("^:[^ ]+ JOIN #[^ ]*")) {
							String[] parts = line.split(" ", 5);
							String nick = parts[0].substring(1).split("!")[0];
							String chan = parts[2];
							if (chan == null || nick == null)
								throw new FormatFieldNotFoundException(line);
							this.bot.onJoin(chan, nick);
						} else if (line.matches("^:[^ ]+ PART #[^ ]* :.*")) {
							String[] parts = line.split(" ", 4);
							String nick = parts[0].substring(1).split("!")[0];
							String chan = parts[2];
							if (chan == null || nick == null)
								throw new FormatFieldNotFoundException(line);
							this.bot.onPart(chan, nick);
						} else if (line.matches("^:[^ ]+ QUIT :.*")) {
							String[] parts = line.split(" ", 3);
							String nick = parts[0].substring(1).split("!")[0];
							String msg = parts[2].substring(1);
							if (nick == null || msg == null)
								throw new FormatFieldNotFoundException(line);
							this.bot.onQuit(nick, msg);
						} else if (line.matches("^:[^ ]+ MODE #[^ ]* ([^ ]*)\\+[^ -]m.*")) {
							String[] parts = line.split(" ", 4);
							String chan = parts[2];
							if (chan == null)
								throw new FormatFieldNotFoundException(line);
							this.bot.onSetModerated(chan);
						} else if (line.matches("^:[^ ]+ MODE #[^ ]* ([^ ]*)-[^+ ]m.*")) {
							String[] parts = line.split(" ", 4);
							String chan = parts[2];
							if (chan == null)
								throw new FormatFieldNotFoundException(line);
							this.bot.onRemoveModerated(chan);
						} else if (line.matches("^PING.*")) {
							char[] pingpong = line.toCharArray();
							if (pingpong[1] != 'I') {
								this.bot.logMessage(
										"Something, somewhere, went terribly wrong when replying to a PING message.");
							} else {
								pingpong[1] = 'O';
								this.bot.sendRawLine(new String(pingpong));
							}
						}
					} catch (IndexOutOfBoundsException | FormatFieldNotFoundException e) {
						this.bot.logMessage("GenericIRCBot had trouble parsing a message: " + line);
						e.printStackTrace();
					}

				}
				this.bot.connectionLost();

			} catch (IOException e) {
				if (this.bot.isConnclosed(e)) {
					this.bot.logMessage("GenericIRCBot lost connection: " + e);
					this.bot.connectionLost();
					// socket is now closed, ignore and retry
				} else {
					// something went wrong
					this.bot.logMessage("GenericIRCBot encountered IO exception" + e);
					throw new RuntimeException(e);
				}
			}
			this.bot.logMessage("GenericIRCBot reconnecting in 5 seconds...");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ie) {
				throw new RuntimeException(ie);
			}
			this.bot.logMessage("GenericIRCBot reconnecting.");
		}
		this.bot.logMessage("GenericIRCBot IRC thread shutting down");
	}

	@SuppressWarnings("serial")
	private static class FormatFieldNotFoundException extends Exception {

		public FormatFieldNotFoundException(String line) {
			super("Parsing line: " + line);
		}
	}
}