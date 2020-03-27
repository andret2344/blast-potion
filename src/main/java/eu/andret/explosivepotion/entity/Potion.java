package eu.andret.explosivepotion.entity;

import lombok.Value;
import org.bukkit.inventory.ItemStack;

@Value
public class Potion {
	ItemStack itemStack;
	double explosionPower;
}
