package eu.andret.blastpotion;

import eu.andret.blastpotion.config.PotionPattern;
import eu.andret.blastpotion.helper.PluginTest;
import org.assertj.core.api.ThrowableAssert;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlastPotionPluginTest extends PluginTest {
	@Test
	void reloadReplacesPotionsAndRecipes() throws IOException, InvalidConfigurationException {
		// given
		writeConfig("""
				potions:
				  other:
				    explosion-power: 2
				    crafting:
				      shape:
				        - 'TT'
				      mapping:
				        'T': TNT
				""");

		// when
		final int result = plugin.reload();

		// then
		assertThat(result).isEqualTo(1);
		assertThat(plugin.getPotionList()).extracting(PotionPattern::name).containsExactly("other");
		assertThat(server.getRecipe(new NamespacedKey(plugin, "throwable-tnt"))).isNull();
		assertThat(server.getRecipe(new NamespacedKey(plugin, "other"))).isNotNull();
	}

	@Test
	void reloadInvalidYamlKeepsEverything() throws IOException {
		// given
		writeConfig("potions: [");

		// when
		final ThrowableAssert.ThrowingCallable callable = () -> plugin.reload();

		// then
		assertThatThrownBy(callable).isInstanceOf(InvalidConfigurationException.class);
		assertThat(plugin.getPotionList()).extracting(PotionPattern::name).containsExactly("throwable-tnt");
		assertThat(server.getRecipe(new NamespacedKey(plugin, "throwable-tnt"))).isNotNull();
	}

	@Test
	void reloadInvalidContentKeepsEverything() throws IOException {
		// given
		writeConfig("""
				potions:
				  broken:
				    explosion-power: -1
				""");

		// when
		final ThrowableAssert.ThrowingCallable callable = () -> plugin.reload();

		// then
		assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
		assertThat(plugin.getPotionList()).extracting(PotionPattern::name).containsExactly("throwable-tnt");
		assertThat(server.getRecipe(new NamespacedKey(plugin, "throwable-tnt"))).isNotNull();
	}

	@Test
	void disableRemovesRecipes() {
		// when
		server.getPluginManager().disablePlugin(plugin);

		// then
		assertThat(server.getRecipe(new NamespacedKey(plugin, "throwable-tnt"))).isNull();
	}

	@Test
	void findPotion() {
		// when
		final ItemStack itemStack = potion("throwable-tnt").itemStack().asOne();

		// then
		assertThat(plugin.findPotion(itemStack)).map(PotionPattern::name).contains("throwable-tnt");
	}

	@Test
	void findNoPotionInRegularItem() {
		// when
		final ItemStack itemStack = new ItemStack(Material.SPLASH_POTION);

		// then
		assertThat(plugin.findPotion(itemStack)).isEmpty();
	}

	private void writeConfig(@NotNull final String yaml) throws IOException {
		Files.writeString(new File(plugin.getDataFolder(), "config.yml").toPath(), yaml);
	}
}
