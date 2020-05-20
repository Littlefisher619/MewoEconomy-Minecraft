package net.steepout.meoweco;

import com.mewo.economy.DataManager;
import com.mewo.economy.MarketGUI;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Overseer implements Listener {

    private static final String IN_STOCK_LABEL = "@in stock";

    static ItemStack setupItemStackMarks(ItemStack t){
        if(!t.getItemMeta().hasLore() || !t.getItemMeta().getLore().contains(IN_STOCK_LABEL)) {
            List<String> lores = new ArrayList<>();
            if (t.getItemMeta().hasLore())
                lores.addAll(t.getItemMeta().getLore());
            lores.add(IN_STOCK_LABEL);
            ItemMeta meta = t.getItemMeta();
            meta.setLore(lores);
            t.setItemMeta(meta);
            MarketGUI.instance.getDataManager().universalItemAppend(t.getType(), t.getAmount());
        }
        return t;
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemPicked(EntityPickupItemEvent e){
        if(e.getEntity() instanceof Player){
            ((Player) e.getEntity()).getInventory().forEach(item -> {
                if(item != null && item.getType() != Material.AIR){
                    setupItemStackMarks(item);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChestOpen(InventoryOpenEvent e){
        if(e.getPlayer() instanceof  Player && e.getInventory().getHolder() instanceof Block){
            e.getPlayer().getInventory().forEach(item -> {
                if(item != null && item.getType() != Material.AIR){
                    setupItemStackMarks(item);
                }
            });
            e.getInventory().forEach(item -> {
                if(item != null && item.getType() != Material.AIR){
                    setupItemStackMarks(item);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRecipeCraft(CraftItemEvent e){
        if(!e.isCancelled() && e.getResult() == Event.Result.ALLOW){
            Recipe recipe = e.getRecipe();
            if(recipe instanceof FurnaceRecipe){

                MarketGUI.instance.getDataManager().universalItemRemove(((FurnaceRecipe) recipe).getInput().getType(),
                        ((FurnaceRecipe) recipe).getInput().getAmount());
            }
            if(recipe instanceof ShapedRecipe){
                ((ShapedRecipe) recipe).getIngredientMap().forEach((c,item) -> {
                    MarketGUI.instance.getDataManager().universalItemRemove(item.getType(),item.getAmount());
                });
            }
            if(recipe instanceof  ShapelessRecipe){
                ((ShapelessRecipe) recipe).getIngredientList()
                        .forEach(item -> MarketGUI.instance.getDataManager()
                                .universalItemRemove(item.getType(),item.getAmount()));
            }
        }
    }

    @EventHandler
    public void onEntityDamaged(EntityDamageByBlockEvent e){
        if(e.getEntity() instanceof Item) {
            if(e.getDamager().isLiquid() || e.getDamager().getType() == Material.CACTUS){
                ItemStack t = ((Item) e.getEntity()).getItemStack();
                if(t.hasItemMeta() && t.getItemMeta().hasLore() && t.getItemMeta().getLore().contains(IN_STOCK_LABEL)){
                    MarketGUI.instance.getDataManager().universalItemAppend(t.getType(),t.getAmount());
                }
            }
        }
    }



}
