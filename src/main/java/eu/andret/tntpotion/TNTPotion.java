package eu.andret.tntpotion;

import lombok.Value;
import org.bukkit.inventory.ItemStack;

@Value
public class TNTPotion {
    private ItemStack potion;
    private double explosionPower;
}
