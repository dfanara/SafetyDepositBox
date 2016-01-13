package com.shdwlf.safetydepositbox.storage;

import com.shdwlf.safetydepositbox.data.Vault;

import java.util.UUID;

public interface StorageInterface {

    void startup();
    void shutdown();

    Vault getVault(UUID owner, int id);
    Vault getVault(UUID owner);

    void saveVault(Vault vault);

}
