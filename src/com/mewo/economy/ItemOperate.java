package com.mewo.economy;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ItemOperate
{
    public static boolean hasItem(Player player, Material material,Integer amount)
    {
        int amountFound = 0;
        ItemStack[] arrayOfItemStack;
        int j = (arrayOfItemStack = player.getInventory().getContents()).length;
        for (int i = 0; i < j; i++)
        {
            ItemStack item = arrayOfItemStack[i];
            if ((item != null) && (item.getType() == material) ) {
                amountFound += item.getAmount();
            }
        }
        //player.sendMessage("Found:"+amountFound);
        return amountFound >= amount;
    }

    public static boolean takeItem(Player player, Material material,Integer amount)
    {
        player.sendMessage("take " + material.toString() + "*" + amount);
        if (amount <= 0) {
            return true;
        }
        int itemsToTake = amount;

        ItemStack[] contents = player.getInventory().getContents();
        ItemStack current = null;
        for (int i = 0; i < contents.length; i++)
        {
            current = contents[i];
            if ((current != null) && (current.getType() == material) )
            {
                if (current.getAmount() > itemsToTake)
                {
                    current.setAmount(current.getAmount() - itemsToTake);
                    return true;
                }
                itemsToTake -= current.getAmount();
                player.getInventory().setItem(i, new ItemStack(Material.AIR));
            }
            if (itemsToTake <= 0) {
                return true;
            }
        }
        return false;
    }

    public static void sendItem(Player player, Material material,Integer amount){
        PlayerInventory inventory = player.getInventory();
        ItemStack itemstack = new ItemStack(material, amount);
        inventory.addItem(itemstack);
    }
}
