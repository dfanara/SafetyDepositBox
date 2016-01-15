package com.shdwlf.safetydepositbox.listener;

import com.shdwlf.safetydepositbox.SafetyDepositBox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final SafetyDepositBox plugin;

    public PlayerListener(SafetyDepositBox plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getVaultManager().cacheVaults(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getVaultManager().coldStoreVaults(event.getPlayer().getUniqueId());
    }

}
