package com.shdwlf.safetydepositbox.storage;

public abstract class AutoSavingStorageInterface extends StorageInterface {

    /**
     * How often to call the autoSave method
     * @return
     */
    abstract int autoSaveFrequency();
    abstract void autoSave();

}
