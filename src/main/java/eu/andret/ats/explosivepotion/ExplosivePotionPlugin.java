package eu.andret.ats.explosivepotion;

import eu.andret.arguments.AnnotatedCommand;
import eu.andret.arguments.CommandManager;
import eu.andret.arguments.api.entity.FallbackConstants;
import eu.andret.ats.explosivepotion.entity.ExplosivePotion;
import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ExplosivePotionPlugin extends JavaPlugin {
	@NotNull
	private final List<ExplosivePotion> explosivePotions = new ArrayList<>();

	@Override
	public void onEnable() {
		saveDefaultConfig();
		setupConfig();
		setUpListeners();
		setupCommand();
		new Metrics(this, 10681);
	}

	private void setupCommand() {
		final AnnotatedCommand<ExplosivePotionPlugin> command = CommandManager.registerCommand(ExplosivePotionCommand.class, this);
		command.getOptions().setAutoTranslateColors(true);
		command.addTypeCompleter(ExplosivePotion.class, () -> explosivePotions.stream().map(ExplosivePotion::getName).toList());
		command.addTypeMapper(ExplosivePotion.class, name -> explosivePotions.stream()
						.filter(x -> x.getName().equalsIgnoreCase(name))
						.findAny()
						.orElse(null),
				FallbackConstants.ON_NULL);
	}

	private void setupConfig() {
		final ConfigurationSection potionsSection = getConfig().getConfigurationSection("explosivePotions");
		if (potionsSection == null) {
			return;
		}

		Optional.of(potionsSection)
				.map(section -> section.getKeys(false))
				.stream()
				.flatMap(Collection::stream)
				.map(potionsSection::getConfigurationSection)
				.filter(Objects::nonNull)
				.forEach(current -> {
					final ItemStack potion = new ItemStack(Material.SPLASH_POTION);
					final PotionMeta itemMeta = (PotionMeta) potion.getItemMeta();
					if (itemMeta == null) {
						return;
					}
					itemMeta.setBasePotionData(new PotionData(PotionType.UNCRAFTABLE, false, false));
					itemMeta.setDisplayName(String.valueOf(current.getString("item.name")).replace('&', '\u00A7'));
					itemMeta.setLore(current.getStringList("item.lore").stream().map(s -> s.replace('&', '\u00A7')).toList());
					potion.setItemMeta(itemMeta);
					explosivePotions.add(new ExplosivePotion(current.getName(), potion, current.getDouble("explosion-power")));
					final List<String> shape = current.getStringList("crafting.shape");
					final Map<Character, Material> mapping = new HashMap<>();
					final ConfigurationSection configurationSection = current.getConfigurationSection("crafting.mapping");
					Objects.requireNonNull(configurationSection).getKeys(false).forEach(key -> Optional.of(key)
							.map(configurationSection::getString)
							.map(Material::getMaterial)
							.ifPresent(material -> mapping.put(key.charAt(0), material)));
					createRecipe(potion, shape, mapping);
				});
	}

	private void createRecipe(@NotNull final ItemStack target,
							  @NotNull final List<String> shape,
							  @NotNull final Map<Character, Material> mapping) {
		final ShapedRecipe recipe = new ShapedRecipe(createKey(target), target);
		recipe.shape(shape.toArray(new String[]{}));
		mapping.forEach(recipe::setIngredient);
		getServer().addRecipe(recipe);
	}

	@NotNull
	private NamespacedKey createKey(@NotNull final ItemStack itemStack) {
		return new NamespacedKey(
				this,
				Optional.of(itemStack)
						.map(ItemStack::getItemMeta)
						.map(ItemMeta::getDisplayName)
						.map(String::toLowerCase)
						.map(name -> name.replaceAll("\\u00A7[\\da-f]", ""))
						.map(name -> name.replaceAll("[^a-z\\d/._-]", ""))
						.orElse(getDescription().getFullName())
		);
	}

	private void setUpListeners() {
		getServer().getPluginManager().registerEvents(new ExplosivePotionListener(this), this);
	}

	@NotNull
	public Optional<ExplosivePotion> getExplosivePotion(@NotNull final ThrownPotion thrownPotion) {
		return explosivePotions.stream()
				.filter(explosivePotion -> thrownPotion.getItem().equals(explosivePotion.getItemStack()))
				.findFirst();
	}
}
