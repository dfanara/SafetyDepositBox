package com.shdwlf.safetydepositbox.storage;

import com.shdwlf.safetydepositbox.SafetyDepositBox;
import com.shdwlf.safetydepositbox.data.Vault;
import com.shdwlf.safetydepositbox.util.ItemUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class YMLStorageInterface extends AutoSavingStorageInterface {

    private final SafetyDepositBox safetyDepositBox;
    private YamlConfiguration configuration;
    private File configurationFile;

    private HashMap<UUID, HashMap<Integer, Vault>> vaultCache;

    public YMLStorageInterface(SafetyDepositBox safetyDepositBox) {
        this.safetyDepositBox = safetyDepositBox;
        this.vaultCache = new HashMap<>();
    }

    @Override
    public void startup() {
        this.configurationFile = new File(safetyDepositBox.getDataFolder(), "storage.yml");

        if (!this.configurationFile.exists()) {
            try {
                this.configurationFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        configuration = new YamlConfiguration();
        try {
            configuration.load(configurationFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        autoSave();

        try {
            configuration.save(configurationFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void coldStore(UUID owner) {
        HashMap<Integer, Vault> vaults = vaultCache.get(owner);
        if (vaults != null) {
            for (Vault vault : vaults.values()) {
                SafetyDepositBox.debug("Requesting Cold Store: " + vault.getOwner().toString() + ":" + vault.getId());
                vault.requestRemoveFromCache();
            }
        }
    }

    protected Vault createNewVault(UUID owner, int id, int size) {
        if (!vaultCache.containsKey(owner))
            vaultCache.put(owner, new HashMap<Integer, Vault>());
        HashMap<Integer, Vault> vaults = vaultCache.get(owner);
        Vault vault = new Vault(owner, id, size);
        vaults.put(id, vault);
        return vault;
    }

    @Override
    public Vault getVault(UUID owner, int id) {
        HashMap<Integer, Vault> vaults = vaultCache.get(owner);
        if (vaults == null || vaults.get(id) == null)
            return createNewVault(owner, id, 54); //TODO: Get default vault size.

        return vaults.get(id);
    }

    @Override
    public Vault getVault(UUID owner) {
        return getVault(owner, 1);
    }

    @Override
    public void saveVault(Vault vault) {
        vault.requestSave();
    }

    @Override
    public int autoSaveFrequency() {
        return 20 * 60 * 5; //AutoSave every 5 minutes
    }

    @Override
    public void autoSave() {
        //Add new items back to the configuration yml
        for(Map.Entry<UUID, HashMap<Integer, Vault>> data : vaultCache.entrySet()) {
            HashMap<Integer, Vault> vaults = data.getValue();
            for(Map.Entry<Integer, Vault> vault : vaults.entrySet()) {
                if(vault.getValue().hasPendingChanges()) {
                    saveVaultToYML(vault.getValue());
                    vault.getValue().resetSaveRequest();
                }
            }
        }

        //Save the new configuration yml
        try {
            configuration.save(configurationFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Remove items from the cache
        for (Map.Entry<UUID, HashMap<Integer, Vault>> data : vaultCache.entrySet()) {
            HashMap<Integer, Vault> vaults = data.getValue();
            UUID uuid = data.getKey();
            ArrayList<Integer> toRemove = new ArrayList<>();
            for(Map.Entry<Integer, Vault> vData : vaults.entrySet()) {
                vData.getValue().resetSaveRequest();
                if (vData.getValue().isPendingCacheRemoval())
                    toRemove.add(vData.getKey()); //Schedule Vault for removal
            }
            for(int i : toRemove)
                vaults.remove(i);
        }
    }

    protected void saveVaultToYML(Vault vault) {
        SafetyDepositBox.debug("Serializing Vault: " + vault.getOwner().toString() + ":" + vault.getId());
        String inv = ItemUtils.toBase64(vault.getInventory());
        configuration.set(vault.getOwner().toString() + "." + vault.getId(), inv);
    }

    @Override
    public void cacheVaults(UUID owner) {
        //Load vaults into vault cache from config on startup.
        if(!configuration.contains(owner.toString()))
            return;
        ConfigurationSection section = configuration.getConfigurationSection(owner.toString());
        Set<String> keys = section.getKeys(false);
        for(String s : keys) {
            try {
                Inventory inventory = ItemUtils.fromBase64(section.getString(s));
                Vault vault = new Vault(owner, Integer.parseInt(s), inventory.getSize(), inventory.getContents());
                if(!vaultCache.containsKey(owner))
                    vaultCache.put(owner, new HashMap<Integer, Vault>());
                HashMap<Integer, Vault> vaults = vaultCache.get(owner);
                vaults.put(Integer.parseInt(s), vault);
                SafetyDepositBox.debug("Cached Vault: " + vault.getOwner().toString() + ":" + vault.getId());
            } catch (IOException e) {
                SafetyDepositBox.debug("Could Not Load From Cache: " + owner.toString() + ":" + s);
                e.printStackTrace();
            }
//            List<String> raw = section.getStringList(s);
//            HashMap<Integer, ItemStack> items = new HashMap<>();
//            int inventorySize = 54; //TODO: Get default inventory size
//            int inventoryId = Integer.parseInt(s);
//            for(String line : raw) {
//                if(line.startsWith("@")) {
//                    if(line.charAt(1) == 's') {
//                        inventorySize = Integer.parseInt(line.substring(2));
//                    }
//                }else {
//                    int slot = Integer.parseInt(line.substring(0, 2));
//                    items.put(slot, ItemUtils.fromString(line.substring(2)));
//                }
//            }
//
//            if(!vaultCache.containsKey(owner))
//                vaultCache.put(owner, new HashMap<Integer, Vault>());
//            HashMap<Integer, Vault> vaults = vaultCache.get(owner);
//            Vault vault = new Vault(owner, inventoryId, inventorySize);
//
//            for(HashMap.Entry<Integer, ItemStack> is : items.entrySet()) {
//                vault.getInventory().setItem(is.getKey(), is.getValue());
//            }
//
//            vaults.put(inventoryId, vault);
//            SafetyDepositBox.debug("Cached Vault: " + vault.getOwner().toString() + ":" + vault.getId());
        }
    }

}
