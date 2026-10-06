package eu.andret.blastpotion.config;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PotionPattern(@NotNull String name, @NotNull ItemStack itemStack, float explosionPower,
		@Nullable ShapedRecipe recipe) {
}
