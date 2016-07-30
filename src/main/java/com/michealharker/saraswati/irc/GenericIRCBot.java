package com.michealharker.saraswati.irc;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.jdt.annotation.Nullable;

/**
 * you will have to override the constructor and use all of
 * <ul>
 * <li>{@link GenericIRCBot#setNick(String)},</li>
 * <li>{@link GenericIRCBot#setUser(String)},</li>
 * <li>{@link GenericIRCBot#setGecos(String)},</li>
 * </ul>
 */
public abstract class GenericIRCBot extends IRCMethods implements Runnable {

	/* Settings */
	final InetSocketAddress socketAddr;
	final boolean useSsl;

	final IRCConnectionInfo info;
	final @Nullable String servPassword;

	/* state */
	private volatile boolean connecting = true;
	private volatile @Nullable Socket clientSocket = null;
	private volatile @Nullable OutputStream out = null;
	private volatile boolean connected = false;
	final Object lock = new Object();
	
	/* Constants */
	static final Charset utf8;
	static {
		@Nullable
		Charset myutf = Charset.forName("UTF-8");
		if (myutf == null)
			throw new NullPointerException("Charset.forName return null");
		utf8 = myutf;
	}

	protected GenericIRCBot(InetSocketAddress socketAddr, boolean useSsl, IRCConnectionInfo info,
			@Nullable String servPassword) {
		this.socketAddr = socketAddr;
		this.useSsl = useSsl;
		this.info = info;
		this.servPassword = servPassword;
	}

	@Override
	public void run() {
		logMessage("GenericIRCBot starting...");
		start();
		logMessage("GenericIRCBot started.");

	}

	/**
	 * override this if you have a place you want to direct info-level log
	 * messages
	 */
	protected void logMessage(String msg) {
		System.out.println(msg);
	}

	void sendRawLine(String message) {
		String msg = message;
		/* normalize line endings */
		while (msg.endsWith("\n") || msg.endsWith("\r") || msg.endsWith("\0")) {
			msg = msg.substring(0, msg.length() - 1);
		}
		msg = msg.replace('\n', '?');
		msg = msg.replace('\r', '?');
		msg = msg.replace('\0', '?');
		logMessage("[IRC] --> " + msg);
		msg += "\r\n";
		synchronized (this.lock) {
			try {
				final @Nullable OutputStream os = this.out;
				if (os != null) {
					os.write(msg.getBytes(utf8));
					os.flush();
				}
			} catch (IOException e) {
				new RuntimeException("Sending raw line", e).printStackTrace();
			}
		}
	}

	private void start() {
		Thread irc = new IRCThread(this);
		irc.start();
		synchronized (this.lock) {
			while (!this.isConnected()) {
				try {
					this.lock.wait(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (!irc.isAlive()) {
					throw new IllegalStateException("IRC thread died on initial connection attempt");
				}
			}
		}
	}

	void connectionLost() {
		synchronized (this.lock) {
			try {
				this.out = null;
				final @Nullable Socket sock = this.clientSocket;
				if (sock != null) {
					sock.close();
				}
			} catch (Throwable e) {
				new RuntimeException("Could not close socket", e).printStackTrace();
			} finally {
				this.clientSocket = null;
			}
		}
	}

	boolean isConnclosed(IOException e) {
		if (e instanceof SocketException || e instanceof EOFException)
			return true;
		Throwable cause = e.getCause();
		if (cause != null && cause instanceof IOException)
			return isConnclosed((IOException) cause); // recursion! :D
		return false;
	}

	Socket makeSocket(InetSocketAddress addr, boolean ssl) throws IOException {
		@Nullable
		Socket sock;
		if (ssl) {
			SocketFactory sslsf = SSLSocketFactory.getDefault();
			sock = sslsf.createSocket(addr.getHostString(), addr.getPort());
		} else {
			sock = new Socket(addr.getHostString(), addr.getPort());
		}
		if (sock == null)
			throw new IllegalStateException("Created null socket");
		return sock;
	}

	boolean isConnecting() {
		synchronized (this.lock) {
			return this.connecting;
		}
	}

	void setSocket(Socket s, OutputStream os) {
		synchronized (this.lock) {
			this.clientSocket = s;
			this.out = os;
		}
	}

	protected void disconnect() {
		logMessage("GenericIRCBot shutting down");
		synchronized (this.lock) {
			this.connecting = false;
		}
		try {
			final @Nullable Socket sock = this.clientSocket;
			if (sock != null) {
				sock.close();
			}
		} catch (IOException e) {
			throw new RuntimeException("Error closing socket!", e);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		disconnect();
	}

	protected void joinChannel(String c) {
		sendRawLine("JOIN " + c);
	}

	protected void sendMessage(String channel, String message) {
		if (!channel.startsWith("#")) {
			new IllegalArgumentException("Channel is not starting with #: " + channel).printStackTrace();
		}
		sendRawLine("PRIVMSG " + channel + " :" + message);
	}

	boolean isConnected() {
		return this.connected;
	}

	void setConnected(boolean connected) {
		this.connected = connected;
	}

}