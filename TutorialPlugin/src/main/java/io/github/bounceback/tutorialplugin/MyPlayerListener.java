package io.github.bounceback.tutorialplugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public final class MyPlayerListener implements Listener {
    @EventHandler(priority=EventPriority.LOW)
    public void onLogin(PlayerLoginEvent event) {
        // Your code here...
    }
}