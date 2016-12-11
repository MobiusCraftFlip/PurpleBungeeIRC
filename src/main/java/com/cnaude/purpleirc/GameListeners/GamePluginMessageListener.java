package com.cnaude.purpleirc.GameListeners;

import com.cnaude.purpleirc.ChatMessage;
import com.cnaude.purpleirc.PurpleBot;
import com.cnaude.purpleirc.PurpleIRC;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.jumanjicraft.bungeechat.BungeeChat;
import java.io.IOException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class GamePluginMessageListener
        implements Listener {

    final private PurpleIRC plugin;
    final private BungeeChat bungeeChatServer;

    public GamePluginMessageListener(PurpleIRC plugin) {
        this.plugin = plugin;
        this.bungeeChatServer = (BungeeChat) plugin.getProxy().getPluginManager().getPlugin("BungeeChatBungee");
    }

    @EventHandler
    public void receievePluginMessage(PluginMessageEvent event) throws IOException {
        if (!event.getTag().equalsIgnoreCase("BungeeChat")) {
            return;
        }
        plugin.logDebug("Received BungeeChat event from PluginMessageEvent: " + event.isCancelled());
        if (event.isCancelled()) {
            plugin.logDebug("Ignoring cancelled event.");
        }
        if (bungeeChatServer == null) {
            plugin.logDebug("[receievePluginMessage]: BungeeChatBungee not found.");
            return;
        }
        byte[] bytes = event.getData();
        /*
        if (!bungeeChatServer.shouldBroadcast(ByteStreams.newDataInput(event.getData()).readUTF())) {
            return;
        }
        */
        
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        ChatMessage cm = new ChatMessage(in);        
        
        ProxiedPlayer player = null;
        cm.setMessage(ChatColor.translateAlternateColorCodes('&', cm.getMessage()));
        for (ServerInfo server : plugin.getProxy().getServers().values()) {
            for (ProxiedPlayer p : server.getPlayers()) {
                if (p.getName().equals(cm.getSender())) {
                    player = p;
                }
            }
        }
        if (player != null) {
            if (player.hasPermission("irc.message.gamechat")) {
                for (PurpleBot ircBot : plugin.ircBots.values()) {
                    plugin.logDebug("Calling heroChat from receievePluginMessage for " + ircBot.botNick);
                    ircBot.heroChat(player, cm);
                }
            }
        }
    }
}
