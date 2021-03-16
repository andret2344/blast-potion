package eu.andret.ats.explosivepotion;

import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;

import java.util.Optional;

@AllArgsConstructor
public class ExplosivePotionListener implements Listener {
	private final ExplosivePotionPlugin plugin;

	@EventHandler
	public void onPotionSplash(final PotionSplashEvent event) {
		Optional.of(event)
				.map(PotionSplashEvent::getPotion)
				.map(plugin::getPotion)
				.flatMap(x -> x)
				.ifPresent(potion -> Optional.of(event)
						.map(PotionSplashEvent::getEntity)
						.map(Entity::getLocation)
						.ifPresent(location -> Optional.of(location)
								.map(Location::getWorld)
								.ifPresent(world -> {
									event.setCancelled(true);
									world.createExplosion(location, (float) potion.getExplosionPower());
								})));
	}
}
