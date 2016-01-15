package com.shdwlf.safetydepositbox;

import com.shdwlf.safetydepositbox.listener.PlayerListener;
import com.shdwlf.safetydepositbox.storage.YMLStorageInterface;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SafetyDepositBox extends JavaPlugin {

    public static boolean DEBUG;

    private VaultManager vaultManager;
    private YMLStorageInterface storageInterface;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        DEBUG = getConfig().getBoolean("debug", true); //TODO Change back
        storageInterface = new YMLStorageInterface(this);
        storageInterface.startup();
        vaultManager = new VaultManager(this, storageInterface);

        if(storageInterface.autoSaveFrequency() > 0)
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                @Override
                public void run() {
                    storageInterface.autoSave();
                }
            },
            storageInterface.autoSaveFrequency(), storageInterface.autoSaveFrequency());

        for(Player player : Bukkit.getOnlinePlayers()) {
            vaultManager.cacheVaults(player.getUniqueId());
        }

        registerListeners();
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void onDisable() {
        storageInterface.shutdown();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        vaultManager.openVault((Player) sender, Integer.parseInt(args[0]));
        return true;
    }

    public static void debug(String message) {
        if(DEBUG)
            Bukkit.getLogger().info(message);
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }
}