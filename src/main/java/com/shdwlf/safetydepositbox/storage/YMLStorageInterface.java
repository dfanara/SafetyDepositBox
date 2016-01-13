package com.shdwlf.safetydepositbox.storage;

import com.shdwlf.safetydepositbox.SafetyDepositBox;
import com.shdwlf.safetydepositbox.data.Vault;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class YMLStorageInterface implements StorageInterface {

    private final SafetyDepositBox safetyDepositBox;
    private YamlConfiguration configuration;
    private File configurationFile;

    public YMLStorageInterface(SafetyDepositBox safetyDepositBox) {
        this.safetyDepositBox = safetyDepositBox;
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
    public Vault getVault(UUID owner, int id) {
        return new Vault(owner, id, 54);
    }

    @Override
    public Vault getVault(UUID owner) {
        return null;
    }

    @Override
    public void saveVault(Vault vault) {

    }

}
