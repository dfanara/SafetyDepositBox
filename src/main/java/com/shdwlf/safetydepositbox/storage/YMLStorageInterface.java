package com.shdwlf.safetydepositbox.storage;

import com.shdwlf.safetydepositbox.SafetyDepositBox;
import com.shdwlf.safetydepositbox.data.Vault;
import com.shdwlf.safetydepositbox.util.ItemSerialization;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class YMLStorageInterface extends AutoSavingStorageInterface {

    private final SafetyDepositBox safetyDepositBox;

    private HashMap<UUID, HashMap<Integer, Vault>> vaultCache;

    public YMLStorageInterface(SafetyDepositBox safetyDepositBox) {
        this.safetyDepositBox = safetyDepositBox;
        this.vaultCache = new HashMap<>();
    }

    @Override
    public void startup() {
        File file = new File(safetyDepositBox.getDataFolder(), "storage");
        if(!file.exists())
            file.mkdir();
    }

    @Override
    public void shutdown() {
        autoSave();
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
            return createNewVault(owner, id, safetyDepositBox.getConfig().getInt("vault-size", 54));

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
        for(Map.Entry<UUID, HashMap<Integer, Vault>> data : vaultCache.entrySet()) {
            YamlConfiguration vaultStore = getPlayerStore(data.getKey(), true);
            File vaultFile = getPlayerFile(data.getKey());

            HashMap<Integer, Vault> vaults = data.getValue();
            for(Map.Entry<Integer, Vault> vault : vaults.entrySet()) {
                if(vault.getValue().hasPendingChanges()) {
                    saveVaultToYML(vault.getValue(), vaultStore);
                    vault.getValue().resetSaveRequest();
                }
            }

            try {
                vaultStore.save(vaultFile);
            } catch (IOException e) {
                SafetyDepositBox.debug("Could Not Safe Vault: " + vaultFile.toString());
                e.printStackTrace();
            }
        }

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

    protected void saveVaultToYML(Vault vault, YamlConfiguration vaultStore) {
        SafetyDepositBox.debug("Serializing Vault: " + vault.getOwner().toString() + ":" + vault.getId());
        try {
            if(vault.isEmpty()) {
                vaultStore.set(vault.getId() + "", null); //Don't store data for empty vaults
            }else {
                String inv = ItemSerialization.toBase64(vault.getInventory().getContents());
                vaultStore.set(vault.getId() + ".size", vault.getSize());
                vaultStore.set(vault.getId() + ".data", inv);
            }
        } catch (Exception e) {
            SafetyDepositBox.debug("Could Not Save Vault: " + vault.getOwner().toString() + ".yml");
            e.printStackTrace();
        }
    }

    @Override
    public void cacheVaults(UUID owner) {
        YamlConfiguration vaultStore = getPlayerStore(owner);
        if(vaultStore == null)
            return;

        Set<String> keys = vaultStore.getKeys(false);
        for(String s : keys) {
            try {
                int vaultSize = vaultStore.getInt(s + ".size");
                int vaultId = Integer.parseInt(s);
                Vault vault = new Vault(
                        owner,
                        vaultId,
                        vaultSize,
                        ItemSerialization.fromBase64(vaultStore.getString(s + ".data"), vaultSize)
                );

                if(!vaultCache.containsKey(owner))
                    vaultCache.put(owner, new HashMap<Integer, Vault>());

                HashMap<Integer, Vault> vaults = vaultCache.get(owner);
                vaults.put(Integer.parseInt(s), vault);
                SafetyDepositBox.debug("Cached Vault: " + vault.getOwner().toString() + ":" + vault.getId());
            } catch (Exception e) {
                SafetyDepositBox.debug("Could Not Cache: " + owner.toString() + ":" + s);
                e.printStackTrace();
            }
        }
    }

    protected File getPlayerFile(UUID owner) {
        return new File(safetyDepositBox.getDataFolder(), "storage" + File.separator + owner.toString() + ".yml");
    }

    protected YamlConfiguration getPlayerStore(UUID owner, boolean createIfMissing) {
        File file = getPlayerFile(owner);
        if(!file.exists()) {
            if(createIfMissing) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                return null;
            }
        }

        YamlConfiguration vaultStore = new YamlConfiguration();
        try {
            vaultStore.load(file);
        } catch (Exception e) {
            SafetyDepositBox.debug("Could Not Load Vault File: " + owner.toString() + ".yml");
            e.printStackTrace();
        }
        return vaultStore;
    }

    protected YamlConfiguration getPlayerStore(UUID owner) {
        return getPlayerStore(owner, false);
    }
}
