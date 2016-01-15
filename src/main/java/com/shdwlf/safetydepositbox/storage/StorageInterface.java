package com.shdwlf.safetydepositbox.storage;

import com.shdwlf.safetydepositbox.data.Vault;

import java.util.UUID;

public abstract class StorageInterface {

    public abstract void startup();
    public abstract void shutdown();

    /**
     * Cache Vaults of Players when they join the game.
     * Only necessary for storage interfaces which would
     * need to make asynchronous calls.
     * @param owner
     */
    public abstract void cacheVaults(UUID owner);

    /**
     * Store vaults and remove them from the cache.
     * @param owner
     */
    public abstract void coldStore(UUID owner);

    public abstract Vault getVault(UUID owner, int id);
    public abstract Vault getVault(UUID owner);

    /**
     * Flags vaults to be saved either in the next autoSave
     * or instantly with some storage methods. (Flatfile)
     * @param vault
     */
    public abstract void saveVault(Vault vault);

}
