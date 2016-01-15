package com.shdwlf.safetydepositbox.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import org.bukkit.inventory.ItemStack;

/**
 * @author bobacadodl
 * Based off of https://gist.github.com/yukinoraru/4162806
 * Modified to use reflection, and updated for 1.8 by bobacadodl
 * Please leave this header here if you are using the class
 */
public class ItemSerialization {
    private final static String textBase64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    public static String toBase64(ItemStack[] items) throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        Object nbtTagCompoundRoot = ReflectionUtils.instantiateObject("NBTTagCompound", ReflectionUtils.PackageType.MINECRAFT_SERVER);
        Object nbtTagListItems = ReflectionUtils.instantiateObject("NBTTagList", ReflectionUtils.PackageType.MINECRAFT_SERVER);


        for(int i=0;i<items.length;i++) {
            if(items[i]==null) continue;
            Object nbtTagCompoundItem = ReflectionUtils.instantiateObject("NBTTagCompound", ReflectionUtils.PackageType.MINECRAFT_SERVER);

            Object nmsItem = ReflectionUtils.invokeMethod(null, "CraftItemStack", ReflectionUtils.PackageType.CRAFTBUKKIT_INVENTORY, "asNMSCopy", items[i]);

            if(nmsItem!=null)
                ReflectionUtils.invokeMethod(nmsItem, "ItemStack", ReflectionUtils.PackageType.MINECRAFT_SERVER, "save", nbtTagCompoundItem);

            //store slot info
            ReflectionUtils.invokeMethod(nbtTagCompoundItem, "NBTTagCompound", ReflectionUtils.PackageType.MINECRAFT_SERVER, "setByte", "Slot", (byte) i);

            ReflectionUtils.invokeMethod(nbtTagListItems, "NBTTagList", ReflectionUtils.PackageType.MINECRAFT_SERVER, "add", nbtTagCompoundItem);
        }

        ReflectionUtils.invokeMethod(nbtTagCompoundRoot, "NBTTagCompound", ReflectionUtils.PackageType.MINECRAFT_SERVER, "set", "list", nbtTagListItems);

        ReflectionUtils.invokeMethod(null, "NBTCompressedStreamTools", ReflectionUtils.PackageType.MINECRAFT_SERVER, "a", nbtTagCompoundRoot, dataOutput);

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    public static ItemStack[] fromBase64(String data, int size) throws InvocationTargetException, NoSuchMethodException, ClassNotFoundException, IllegalAccessException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());

        Object nbtTagCompoundRoot = ReflectionUtils.invokeMethod(null, "NBTCompressedStreamTools", ReflectionUtils.PackageType.MINECRAFT_SERVER, "a", new DataInputStream(inputStream));

        int type = (byte) ReflectionUtils.invokeMethod(nbtTagCompoundRoot, "NBTTagCompound", ReflectionUtils.PackageType.MINECRAFT_SERVER, "getTypeId");
        Object nbtTagListItems = ReflectionUtils.invokeMethod(nbtTagCompoundRoot, "NBTTagCompound", ReflectionUtils.PackageType.MINECRAFT_SERVER, "getList", "list", type);

        int listSize = (int) ReflectionUtils.invokeMethod(nbtTagListItems, "NBTTagList", ReflectionUtils.PackageType.MINECRAFT_SERVER, "size");

        ItemStack[] ret = new ItemStack[size];
        for (int i = 0; i < listSize; i++) {
            Object nbtTagCompoundItem = ReflectionUtils.invokeMethod(nbtTagListItems, "NBTTagList", ReflectionUtils.PackageType.MINECRAFT_SERVER, "get", i);
            int slot = (byte) ReflectionUtils.invokeMethod(nbtTagCompoundItem, "NBTTagCompound", ReflectionUtils.PackageType.MINECRAFT_SERVER, "getByte", "Slot");
            Object nmsItem = ReflectionUtils.invokeMethod(null, "ItemStack", ReflectionUtils.PackageType.MINECRAFT_SERVER, "createStack", nbtTagCompoundItem);
            ret[slot] = (ItemStack) ReflectionUtils.invokeMethod(null, "CraftItemStack", ReflectionUtils.PackageType.CRAFTBUKKIT_INVENTORY, "asBukkitCopy", nmsItem);
        }
        return ret;
    }
}