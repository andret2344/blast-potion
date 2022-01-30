package eu.andret.ats.explosivepotion.entity;

import lombok.Value;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Value
public class ExplosivePotion {
	@NotNull
	String name;
	@NotNull
	ItemStack itemStack;
	double explosionPower;
}
