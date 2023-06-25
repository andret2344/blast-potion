package eu.andret.ats.explosivepotion;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExplosivePotionListener implements Listener {
	@NotNull
	private final ExplosivePotionPlugin plugin;

	public ExplosivePotionListener(@NotNull final ExplosivePotionPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPotionSplash(final PotionSplashEvent event) {
		Optional.of(event)
				.map(PotionSplashEvent::getPotion)
				.flatMap(plugin::getExplosivePotion)
				.ifPresent(potion -> Optional.of(event)
						.map(PotionSplashEvent::getPotion)
						.map(Entity::getLocation)
						.ifPresent(location -> Optional.of(location)
								.map(Location::getWorld)
								.ifPresent(world -> {
									event.setCancelled(true);
									world.createExplosion(location, (float) potion.explosionPower());
								})));
	}
}
