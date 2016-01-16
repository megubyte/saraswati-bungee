package com.michealharker.saraswati.bungee;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.michealharker.saraswati.messages.BungeeMessage;

import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeMessageHandler implements Listener {
	private BungeePlugin plugin;

	public BungeeMessageHandler(BungeePlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPluginMessage(PluginMessageEvent e) throws IOException {
		if (!e.getTag().equals("BungeeCord")) return;
		
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
		
		in.readUTF(); // FORWARD
		in.readUTF(); // ALL
		String subchannel = in.readUTF();
		
		if (subchannel.equals("Saraswati")) {
			short len = in.readShort();
			byte[] data = new byte[len];
			
			in.readFully(data);
			
			DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(data));
			String json = msgin.readUTF();					
			BungeeMessage msg = new BungeeMessage(json);
			
			this.plugin.getIRC().relay(msg);
		}
	}
}
