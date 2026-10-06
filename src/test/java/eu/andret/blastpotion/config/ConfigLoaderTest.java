package eu.andret.blastpotion.config;

import eu.andret.blastpotion.helper.PluginTest;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.assertj.core.api.ThrowableAssert;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

class ConfigLoaderTest extends PluginTest {
	private final YamlConfiguration config = new YamlConfiguration();

	@Test
	void loadShippedConfig() {
		// when
		final List<PotionPattern> result = plugin.getPotionList();

		// then
		assertThat(result).extracting(PotionPattern::name).containsExactly("throwable-tnt");
		final PotionPattern potion = potion("throwable-tnt");
		assertThat(potion.explosionPower()).isEqualTo(3.25f);
		assertThat(potion.itemStack().getType()).isEqualTo(Material.SPLASH_POTION);
		assertThat(server.getRecipe(new NamespacedKey(plugin, "throwable-tnt"))).isNotNull();
	}

	@Test
	void loadPotionItem() throws InvalidConfigurationException {
		// given
		loadConfig("""
				potions:
				  named:
				    explosion-power: 2
				    item:
				      name: '<red>Grenade'
				      lore:
				        - 'first'
				        - '<italic><green>second'
				""");

		// when
		final List<PotionPattern> result = new ConfigLoader(plugin).loadPotions(config);

		// then
		final PotionMeta meta = (PotionMeta) result.getFirst().itemStack().getItemMeta();
		assertThat(meta.customName())
				.isEqualTo(Component.text("Grenade", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
		assertThat(meta.lore()).containsExactly(
				Component.text("first", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
				Component.text("second", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true));
		assertThat(meta.getBasePotionType()).isEqualTo(PotionType.AWKWARD);
		assertThat(meta.getEnchantmentGlintOverride()).isTrue();
		assertThat(meta.getPersistentDataContainer().get(plugin.getPotionKey(), PersistentDataType.STRING))
				.isEqualTo("named");
		assertThat(result.getFirst().itemStack().getData(DataComponentTypes.TOOLTIP_DISPLAY))
				.extracting(TooltipDisplay::hiddenComponents)
				.isEqualTo(Set.of(DataComponentTypes.POTION_CONTENTS));
	}

	@Test
	void loadDefaults() throws InvalidConfigurationException {
		// given
		loadConfig("""
				potions:
				  plain:
				    explosion-power: 1.5
				""");

		// when
		final List<PotionPattern> result = new ConfigLoader(plugin).loadPotions(config);

		// then
		final PotionPattern plain = result.getFirst();
		assertThat(plain.explosionPower()).isEqualTo(1.5f);
		assertThat(plain.itemStack().getItemMeta().hasCustomName()).isFalse();
		assertThat(plain.itemStack().getItemMeta().lore()).isNullOrEmpty();
		assertThat(plain.recipe()).isNull();
	}

	@Test
	void loadCraftingUnderSanitizedKey() throws InvalidConfigurationException {
		// given
		loadConfig("""
				potions:
				  My Potion:
				    explosion-power: 1
				    crafting:
				      shape:
				        - 'TT'
				      mapping:
				        'T': TNT
				""");

		// when
		final List<PotionPattern> result = new ConfigLoader(plugin).loadPotions(config);

		// then
		assertThat(result.getFirst().recipe()).isNotNull()
				.extracting(ShapedRecipe::getKey)
				.isEqualTo(new NamespacedKey(plugin, "my_potion"));
		assertThat(server.getRecipe(new NamespacedKey(plugin, "my_potion"))).isNull();
	}

	@ParameterizedTest
	@MethodSource("invalidConfigs")
	void rejectInvalidConfig(@NotNull final String yaml, @NotNull final String message)
			throws InvalidConfigurationException {
		// given
		loadConfig(yaml);

		// when
		final ThrowableAssert.ThrowingCallable callable = () -> new ConfigLoader(plugin).loadPotions(config);

		// then
		assertThatThrownBy(callable)
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(message);
	}

	@NotNull
	static Stream<Arguments> invalidConfigs() {
		return Stream.of(
				argumentSet("missing explosion power", """
								potions:
								  silent:
								    item:
								      name: 'Silent'
								""",
						"Missing or not positive explosion power at 'potions.silent.explosion-power'."),
				argumentSet("negative explosion power", """
								potions:
								  weak:
								    explosion-power: -2
								""",
						"Missing or not positive explosion power at 'potions.weak.explosion-power'."),
				argumentSet("crafting without shape", """
								potions:
								  shapeless:
								    explosion-power: 1
								    crafting:
								      mapping:
								        'T': TNT
								""",
						"Potion 'shapeless' has a crafting section without a shape."),
				argumentSet("unknown crafting material", """
								potions:
								  typo:
								    explosion-power: 1
								    crafting:
								      shape:
								        - 'TT'
								      mapping:
								        'T': TNTT
								""",
						"Unknown material 'TNTT' at 'potions.typo.crafting.mapping.T'."));
	}

	@Test
	void loadWithoutPotions() throws InvalidConfigurationException {
		// given
		loadConfig("""
				other: 1
				""");

		// when
		final List<PotionPattern> result = new ConfigLoader(plugin).loadPotions(config);

		// then
		assertThat(result).isEmpty();
	}

	private void loadConfig(@NotNull final String yaml) throws InvalidConfigurationException {
		config.loadFromString(yaml);
	}
}
