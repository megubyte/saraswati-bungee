package com.michealharker.saraswati.irc;

public abstract class IRCMethods {
	/**
	 * @param channel
	 *            channel message was sent to
	 * @param sender
	 *            user who sent the message
	 * @param message
	 *            content of the message
	 */
	protected void onMessage(String channel, String sender, String message) {
		/* can be overridden */
	}

	/**
	 * @param channel
	 *            channel message was sent to
	 * @param sender
	 *            user who sent the message
	 * @param action
	 *            content of the action
	 */
	protected void onAction(String channel, String sender, String action) {
		/* can be overridden */}

	/**
	 * @param channel
	 *            channel sender parted from
	 * @param sender
	 *            user who parted
	 */
	protected void onPart(String channel, String sender) {
		/* can be overridden */}

	/**
	 * @param channel
	 *            channel sender joined to
	 * @param sender
	 *            user who joined
	 */
	protected void onJoin(String channel, String sender) {
		/* can be overridden */}

	/**
	 * @param sender
	 *            user who quit
	 * @param reason
	 *            quit reason
	 */
	protected void onQuit(String sender, String reason) {
		/* can be overridden */}

	/**
	 * @param channel
	 *            channel where it happened
	 * @param opsender
	 *            OP who issued the kick
	 * @param victim
	 *            user who was kicked
	 * @param reason
	 *            kick reason
	 */
	protected void onKick(String channel, String opsender, String victim, String reason) {
		/* can be overridden */}

	/**
	 * @param channel
	 *            channel that was set -m
	 */
	protected void onRemoveModerated(String channel) {
		/* can be overridden */}

	/**
	 * @param channel
	 *            channel that was set +m
	 */
	protected void onSetModerated(String channel) {
		/* can be overridden */}

	protected void onConnect() {
		/* can be overridden */}
}
