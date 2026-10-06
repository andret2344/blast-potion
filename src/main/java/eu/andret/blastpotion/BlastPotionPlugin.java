package eu.andret.blastpotion;

import eu.andret.blastpotion.command.PlaceholderCondition;
import eu.andret.blastpotion.command.PotionParameterType;
import eu.andret.blastpotion.config.ConfigLoader;
import eu.andret.blastpotion.config.PotionPattern;
import org.bstats.bukkit.Metrics;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BlastPotionPlugin extends JavaPlugin {
	@NotNull
	private final List<PotionPattern> potionList = new ArrayList<>();
	@NotNull
	private final NamespacedKey potionKey = new NamespacedKey(this, "potion");
	@NotNull
	private final ConfigLoader configLoader = new ConfigLoader(this);

	@Override
	public void onEnable() {
		saveDefaultConfig();
		applyPotions(configLoader.loadPotions(getConfig()));
		getServer().getPluginManager().registerEvents(new BlastPotionListener(this), this);
		setUpCommand();
		new Metrics(this, 10681);
	}

	@Override
	public void onDisable() {
		// The recipes stay on the server otherwise, and enabling the plugin again would register them twice
		applyPotions(List.of());
	}

	/**
	 * Loads the config again. When it is invalid, nothing changes and the exception says why.
	 *
	 * @return the number of potions loaded
	 * @throws IOException                   when the config file cannot be read
	 * @throws InvalidConfigurationException when the config is not valid YAML
	 * @throws IllegalArgumentException      when the content of the config is invalid
	 */
	public int reload() throws IOException, InvalidConfigurationException {
		final YamlConfiguration config = new YamlConfiguration();
		config.load(new File(getDataFolder(), "config.yml"));
		final List<PotionPattern> potions = configLoader.loadPotions(config);
		reloadConfig();
		applyPotions(potions);
		return potions.size();
	}

	private void applyPotions(@NotNull final List<PotionPattern> potions) {
		potionList.stream()
				.map(PotionPattern::recipe)
				.filter(Objects::nonNull)
				.forEach(recipe -> getServer().removeRecipe(recipe.getKey(), false));
		potionList.clear();
		potionList.addAll(potions);
		potionList.stream()
				.map(PotionPattern::recipe)
				.filter(Objects::nonNull)
				.forEach(recipe -> getServer().addRecipe(recipe, false));
		getServer().updateRecipes();
	}

	private void setUpCommand() {
		final Lamp<BukkitCommandActor> lamp = BukkitLamp.builder(this)
				.parameterTypes(types -> types
						.addParameterType(PotionPattern.class, new PotionParameterType(() -> potionList)))
				.commandCondition(new PlaceholderCondition())
				.build();
		lamp.register(new BlastPotionCommand(this));
	}

	/**
	 * Finds the potion the given item was made as, if it still is in the config.
	 */
	@NotNull
	public Optional<PotionPattern> findPotion(@NotNull final ItemStack itemStack) {
		return Optional.ofNullable(itemStack.getPersistentDataContainer().get(potionKey, PersistentDataType.STRING))
				.flatMap(this::getPotion);
	}

	@NotNull
	public Optional<PotionPattern> getPotion(@NotNull final String name) {
		return potionList.stream()
				.filter(potion -> potion.name().equals(name))
				.findFirst();
	}

	@NotNull
	public List<PotionPattern> getPotionList() {
		return potionList;
	}

	@NotNull
	public NamespacedKey getPotionKey() {
		return potionKey;
	}
}
