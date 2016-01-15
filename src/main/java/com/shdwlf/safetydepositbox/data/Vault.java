package com.shdwlf.safetydepositbox.data;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Vault {

    private UUID owner;
    private int id;
    private Inventory inventory;

    /**
     * True if vault has been changed since last loaded.
     */
    private boolean pendingChanges = false;

    /**
     * True if the vault should remain in the cache after being saved
     */
    private boolean keepCached = true;

    public Vault(UUID owner, int id, int size, ItemStack... items) {
        if(owner == null)
            throw new IllegalArgumentException("Owner UUID must not be null");
        if(size % 9 != 0 || size > 54)
            throw new IllegalArgumentException("Size must me a multiple of nine and no greater than 54");
        this.id = id;
        this.owner = owner;
        this.inventory = Bukkit.createInventory(null, size, "Vault #" + id);

        for(int i = 0; i < items.length; i++)
            if(items[i] != null)
                this.inventory.setItem(i, items[i]);
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public int getSize() {
        return this.inventory.getSize();
    }

    public int getId() {
        return this.id;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public boolean hasPendingChanges() {
        return this.pendingChanges;
    }

    public boolean isPendingCacheRemoval() {
        return !this.keepCached;
    }

    public void requestSave() {
        this.pendingChanges = true;
    }

    public void resetSaveRequest() {
        this.pendingChanges = false;
    }

    public void requestRemoveFromCache() {
        this.keepCached = false;
    }
}
