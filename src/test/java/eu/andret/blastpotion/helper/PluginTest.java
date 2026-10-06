package eu.andret.blastpotion.helper;

import eu.andret.blastpotion.BlastPotionPlugin;
import eu.andret.blastpotion.config.PotionPattern;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

/**
 * Starts a mocked server with the plugin loaded from the shipped {@code config.yml} before every test.
 */
public abstract class PluginTest {
	protected ServerMock server;
	protected BlastPotionPlugin plugin;
	protected ExplosionWorld world;

	@BeforeEach
	void setUpServer() {
		server = MockBukkit.mock();
		plugin = MockBukkit.load(BlastPotionPlugin.class);
		world = new ExplosionWorld();
		server.addWorld(world);
	}

	@AfterEach
	void tearDownServer() {
		MockBukkit.unmock();
	}

	@NotNull
	protected PotionPattern potion(@NotNull final String name) {
		return plugin.getPotion(name).orElseThrow();
	}
}
