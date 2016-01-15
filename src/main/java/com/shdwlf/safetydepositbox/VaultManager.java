package com.shdwlf.safetydepositbox;

import com.shdwlf.safetydepositbox.data.Vault;
import com.shdwlf.safetydepositbox.storage.StorageInterface;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.UUID;

public class VaultManager implements Listener {

    private final SafetyDepositBox safetyDepositBox;
    private final StorageInterface storageInterface;
    private HashMap<UUID, Vault> openVaults = new HashMap<>();

    public VaultManager(SafetyDepositBox safetyDepositBox, StorageInterface storageInterface) {
        this.safetyDepositBox = safetyDepositBox;
        this.storageInterface = storageInterface;

        Bukkit.getPluginManager().registerEvents(this, safetyDepositBox);
    }

    /**
     * @param player Player opening the vault
     * @param id     ID of vault to open
     */
    public void openVault(Player player, int id) {
        SafetyDepositBox.debug("Opening Vault: " + player.getUniqueId().toString() + ":" + id);
        Vault vault = storageInterface.getVault(player.getUniqueId(), id);
        openVaults.put(player.getUniqueId(), vault);
        player.openInventory(vault.getInventory());
    }

    /**
     * Listen for player to close their Safety Deposit Box and save it.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID closed = event.getPlayer().getUniqueId();
        if(openVaults.containsKey(closed)) {
            Vault vault = openVaults.remove(closed);
            SafetyDepositBox.debug("Closing Vault: " + vault.getOwner().toString() + ":" + vault.getId());
            storageInterface.saveVault(vault);
        }
    }

    public void cacheVaults(UUID owner) {
        SafetyDepositBox.debug("Caching Vaults: " + owner.toString());
        storageInterface.cacheVaults(owner);
    }

    public void coldStoreVaults(UUID owner) {
        SafetyDepositBox.debug("ColdStoring Vaults: " + owner.toString());
        storageInterface.coldStore(owner);
    }
}
