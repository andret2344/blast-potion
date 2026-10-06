package eu.andret.blastpotion;

import eu.andret.blastpotion.helper.ExplosionWorld;
import eu.andret.blastpotion.helper.PluginTest;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BlastPotionListenerTest extends PluginTest {
	@Test
	void splashBlastPotion() {
		// given
		final ThrownPotion thrownPotion = throwPotion(potion("throwable-tnt").itemStack());
		final PotionSplashEvent event = splash(thrownPotion);

		// when
		server.getPluginManager().callEvent(event);

		// then
		assertThat(event.isCancelled()).isTrue();
		assertThat(world.getExplosions()).containsExactly(
				new ExplosionWorld.Explosion(thrownPotion, thrownPotion.getLocation(), 3.25f, false, true));
	}

	@Test
	void splashRegularPotion() {
		// given
		final PotionSplashEvent event = splash(throwPotion(new ItemStack(Material.SPLASH_POTION)));

		// when
		server.getPluginManager().callEvent(event);

		// then
		assertThat(event.isCancelled()).isFalse();
		assertThat(world.getExplosions()).isEmpty();
	}

	@Test
	void splashPotionRemovedFromConfig() {
		// given
		final ItemStack itemStack = potion("throwable-tnt").itemStack();
		plugin.getPotionList().clear();
		final PotionSplashEvent event = splash(throwPotion(itemStack));

		// when
		server.getPluginManager().callEvent(event);

		// then
		assertThat(event.isCancelled()).isFalse();
		assertThat(world.getExplosions()).isEmpty();
	}

	@Test
	void splashCancelledByProtection() {
		// given
		server.getPluginManager().registerEvents(new Listener() {
			@EventHandler(priority = EventPriority.NORMAL)
			public void protect(@NotNull final PotionSplashEvent event) {
				event.setCancelled(true);
			}
		}, plugin);
		final PotionSplashEvent event = splash(throwPotion(potion("throwable-tnt").itemStack()));

		// when
		server.getPluginManager().callEvent(event);

		// then
		assertThat(world.getExplosions()).isEmpty();
	}

	@NotNull
	private ThrownPotion throwPotion(@NotNull final ItemStack itemStack) {
		return world.spawn(new Location(world, 1, 5, 2), ThrownPotion.class, potion -> potion.setItem(itemStack));
	}

	@NotNull
	private PotionSplashEvent splash(@NotNull final ThrownPotion thrownPotion) {
		return new PotionSplashEvent(thrownPotion, null, null, null, Map.of());
	}
}
