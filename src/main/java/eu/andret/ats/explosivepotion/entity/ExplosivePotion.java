package eu.andret.ats.explosivepotion.entity;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ExplosivePotion(@NotNull String name, @NotNull ItemStack itemStack, double explosionPower) {
}
