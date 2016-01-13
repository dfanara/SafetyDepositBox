package com.shdwlf.safetydepositbox.storage;

import com.shdwlf.safetydepositbox.SafetyDepositBox;
import com.shdwlf.safetydepositbox.data.Vault;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

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

        if(!this.configurationFile.exists()) {
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
        try {
            configuration.save(configurationFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    void coldStore(UUID owner) {
        HashMap<Integer, Vault> vaults = vaultCache.get(owner);
        if(vaults != null) {
            for(Vault vault : vaults.values())
                vault.requestRemoveFromCache();
        }
    }

    protected Vault createNewVault(UUID owner, int id, int size) {
        //TODO: Insert vault into cache and then return new vault.
        return null;
    }

    @Override
    public Vault getVault(UUID owner, int id) {
        //TODO: Load from cached vaults
        HashMap<Integer, Vault> vaults = vaultCache.get(owner);
        if(vaults == null || vaults.get(id) == null)
            return createNewVault(owner, id, 54); //TODO: Get default vault size.

        return new Vault(owner, id, 54);
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
    int autoSaveFrequency() {
        return 20 * 60 * 5; //AutoSave every 5 minutes
    }

    @Override
    void autoSave() {
        try {
            configuration.save(configurationFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO: Loop through vaults and remove from cache if necessary.
    }

    @Override
    void cacheVaults(UUID owner) {
        //Ignore. All vaults are loaded from YML on startup.
    }

}
