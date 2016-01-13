package com.shdwlf.safetydepositbox;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SafetyDepositBox extends JavaPlugin {

    public static boolean DEBUG;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        DEBUG = getConfig().getBoolean("debug", false);
    }

    @Override
    public void onDisable() {

    }

    public static void debug(String message) {
        if(DEBUG)
            Bukkit.getLogger().info(message);
    }

}