package eu.andret.ats.explosivepotion.entity;

import lombok.Value;
import org.bukkit.inventory.ItemStack;

@Value
public class Potion {
	String name;
	ItemStack itemStack;
	double explosionPower;
}
