package com.shdwlf.safetydepositbox.data;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Vault {

    private UUID owner;
    private int id;
    private Inventory inventory;

    public Vault(UUID owner, int id, int size, ItemStack... items) {
        if(owner == null)
            throw new IllegalArgumentException("Owner UUID must not be null");
        if(size % 9 != 0 || size > 54)
            throw new IllegalArgumentException("Size must me a multiple of nine and no greater than 54");
        this.id = id;
        this.owner = owner;
        this.inventory = Bukkit.createInventory(null, size, "Vault #" + id);
        this.inventory.addItem(items);
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public int getSize() {
        return this.getSize();
    }

    public int getId() {
        return this.id;
    }

    public UUID getOwner() {
        return this.owner;
    }

}
