package eu.andret.explosivepotion;

import eu.andret.explosivepotion.entity.Potion;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExplosivePotion extends JavaPlugin {
	private final List<Potion> potions = new ArrayList<>();

	@Override
	public void onEnable() {
		saveDefaultConfig();
		setUpListeners();
		Optional.of(getConfig())
				.map(config -> config.getList("potions"))
				.stream()
				.flatMap(Collection::stream)
				.map(section -> getConfig().createSection("current", (Map<?, ?>) section))
				.forEach(current -> {
					ItemStack potion = new ItemStack(Material.SPLASH_POTION);
					PotionMeta itemMeta = (PotionMeta) potion.getItemMeta();
					itemMeta.setBasePotionData(new PotionData(PotionType.UNCRAFTABLE, false, false));
					itemMeta.setDisplayName(current.getString("item.name").replace('&', '\u00A7'));
					itemMeta.setLore(current.getStringList("item.lore").stream().map(s -> s.replace('&', '\u00A7')).collect(Collectors.toList()));
					potion.setItemMeta(itemMeta);
					potions.add(new Potion(potion, current.getDouble("explosion-power")));
					List<String> shape = current.getStringList("crafting.shape");
					Map<Character, Material> mapping = new HashMap<>();
					ConfigurationSection configurationSection = current.getConfigurationSection("crafting.mapping");
					Objects.requireNonNull(configurationSection).getKeys(false).forEach(key -> {
						Material mat = Material.getMaterial(Objects.requireNonNull(configurationSection.getString(key)));
						if (mat != null) {
							mapping.put(key.charAt(0), mat);
						}
					});
					createRecipe(potion, shape, mapping);
				});
	}

	private void createRecipe(ItemStack target, List<String> shape, Map<Character, Material> mapping) {
		ShapedRecipe recipe = new ShapedRecipe(createKey(target), target);
		recipe.shape(shape.toArray(new String[]{}));
		mapping.forEach(recipe::setIngredient);
		getServer().addRecipe(recipe);
	}

	private NamespacedKey createKey(ItemStack itemStack) {
		return new NamespacedKey(
				this,
				Optional.of(itemStack)
						.map(ItemStack::getItemMeta)
						.map(ItemMeta::getDisplayName)
						.map(String::toLowerCase)
						.map(name -> name.replaceAll("\\u00A7[\\da-f]", ""))
						.map(name -> name.replaceAll("[^a-z0-9/._-]", ""))
						.orElse(getDescription().getFullName())
		);
	}

	private void setUpListeners() {
		getServer().getPluginManager().registerEvents(new ExplosivePotionListener(this), this);
	}

	Optional<Potion> getPotion(ThrownPotion thrownPotion) {
		return potions.stream()
				.filter(potion -> thrownPotion.getItem().equals(potion.getItemStack()))
				.findFirst();
	}
}
