package com.michealharker.saraswati.irc;

public class IRCConnectionInfo {
	private final String nickname;
	private final String username;
	private final String gecos;

	public IRCConnectionInfo(String nickname, String username, String gecos) {
		if (nickname.contains(" "))
			throw new IllegalArgumentException("nickname must not contain spaces");
		if (username.contains(" "))
			throw new IllegalArgumentException("username must not contain spaces");
		if (!nickname.matches("[a-z_\\[\\]\\\\^{}|`][a-z0-9_\\[\\]\\\\^{}|`-]*"))
			throw new IllegalArgumentException("nickname is not a valid IRC nickname");
		this.nickname = nickname;
		this.username = username;
		this.gecos = gecos;
	}

	public String getNickname() {
		return this.nickname;
	}

	public String getUsername() {
		return this.username;
	}

	public String getGecos() {
		return this.gecos;
	}

}
