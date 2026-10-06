package eu.andret.blastpotion.config;

import eu.andret.blastpotion.BlastPotionPlugin;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class ConfigLoader {
	private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
	@NotNull
	private final BlastPotionPlugin plugin;

	public ConfigLoader(@NotNull final BlastPotionPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Reads the potions from the config. Builds their recipes but does not register them, so nothing changes on the
	 * server when the config turns out to be invalid.
	 *
	 * @throws IllegalArgumentException when the config is invalid, with a message for the server admin
	 */
	@NotNull
	public List<PotionPattern> loadPotions(@NotNull final ConfigurationSection config) {
		final ConfigurationSection potions = config.getConfigurationSection("potions");
		if (potions == null) {
			return Collections.emptyList();
		}
		return Optional.of(potions)
				.map(section -> section.getKeys(false))
				.stream()
				.flatMap(Collection::stream)
				.map(potions::getConfigurationSection)
				.filter(Objects::nonNull)
				.map(this::loadPotion)
				.toList();
	}

	@NotNull
	private PotionPattern loadPotion(@NotNull final ConfigurationSection configurationSection) {
		final double explosionPower = configurationSection.getDouble("explosion-power");
		if (explosionPower <= 0) {
			throw new IllegalArgumentException("Missing or not positive explosion power at '"
					+ configurationSection.getCurrentPath() + ".explosion-power'.");
		}
		final ItemStack itemStack = loadItem(configurationSection);
		return new PotionPattern(configurationSection.getName(), itemStack, (float) explosionPower,
				loadRecipe(configurationSection, itemStack));
	}

	@NotNull
	private ItemStack loadItem(@NotNull final ConfigurationSection configurationSection) {
		final ItemStack itemStack = new ItemStack(Material.SPLASH_POTION);
		final PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
		potionMeta.setBasePotionType(PotionType.AWKWARD);
		potionMeta.setEnchantmentGlintOverride(true);
		// The custom name, as the game names a potion after its potion type and ignores the item name.
		// Not italic, which a custom name is by default.
		Optional.of(configurationSection)
				.map(section -> section.getString("item.name"))
				.map(MINI_MESSAGE::deserialize)
				.map(name -> name.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
				.ifPresent(potionMeta::customName);
		potionMeta.lore(configurationSection.getStringList("item.lore").stream()
				.map(MINI_MESSAGE::deserialize)
				.map(text -> text.colorIfAbsent(NamedTextColor.WHITE)
						.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
				.toList());
		// The thrown potion carries this, so the listener knows which potion exploded
		potionMeta.getPersistentDataContainer()
				.set(plugin.getPotionKey(), PersistentDataType.STRING, configurationSection.getName());
		itemStack.setItemMeta(potionMeta);
		// Hides the "No Effects" line of the awkward potion
		itemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
				.addHiddenComponents(DataComponentTypes.POTION_CONTENTS)
				.build());
		return itemStack;
	}

	@Nullable
	private ShapedRecipe loadRecipe(@NotNull final ConfigurationSection configurationSection,
			@NotNull final ItemStack potion) {
		if (!configurationSection.isConfigurationSection("crafting")) {
			return null;
		}
		final List<String> shape = configurationSection.getStringList("crafting.shape");
		if (shape.isEmpty()) {
			throw new IllegalArgumentException("Potion '" + configurationSection.getName()
					+ "' has a crafting section without a shape.");
		}
		final ShapedRecipe recipe = new ShapedRecipe(createKey(configurationSection.getName()), potion);
		recipe.shape(shape.toArray(new String[0]));
		final ConfigurationSection mapping = configurationSection.getConfigurationSection("crafting.mapping");
		if (mapping != null) {
			mapping.getKeys(false).forEach(key -> recipe.setIngredient(key.charAt(0),
					loadMaterial(mapping.getString(key), mapping.getCurrentPath() + "." + key)));
		}
		return recipe;
	}

	// Potion names are unique config keys, so they make unique recipe keys
	@NotNull
	private NamespacedKey createKey(@NotNull final String potionName) {
		return new NamespacedKey(plugin, potionName.toLowerCase(Locale.ROOT).replaceAll("[^a-z\\d/._-]", "_"));
	}

	@NotNull
	private Material loadMaterial(@Nullable final String name, @NotNull final String path) {
		if (name == null) {
			throw new IllegalArgumentException("Missing material at '" + path + "'.");
		}
		final Material material = Material.getMaterial(name);
		if (material == null) {
			throw new IllegalArgumentException("Unknown material '" + name + "' at '" + path + "'.");
		}
		return material;
	}
}
