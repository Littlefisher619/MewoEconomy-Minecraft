package net.steepout.meoweco;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MetaDataSafe {

    private static ItemMeta createItemMetaData(Material material){
        return Bukkit.getItemFactory().getItemMeta(material);
    }

    public static ItemStack safeMetaLore(ItemStack i){
        ItemMeta meta = i.getItemMeta();
        if(!i.hasItemMeta())
            meta = createItemMetaData(i.getType());
        List<String> lores = meta.getLore();
        if(!meta.hasLore())
            lores = new ArrayList<>();
        meta.setLore(lores);
        i.setItemMeta(meta);
        return i;
    }

}
