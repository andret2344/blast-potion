package eu.andret.explosivepotion.entity;

import lombok.Value;
import org.bukkit.inventory.ItemStack;

@Value
public class Potion {
    private ItemStack potion;
    private double explosionPower;
}
