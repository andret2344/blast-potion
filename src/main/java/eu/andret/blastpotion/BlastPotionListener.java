package eu.andret.blastpotion;

import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.jetbrains.annotations.NotNull;

public class BlastPotionListener implements Listener {
	@NotNull
	private final BlastPotionPlugin plugin;

	public BlastPotionListener(@NotNull final BlastPotionPlugin plugin) {
		this.plugin = plugin;
	}

	// After protections cancelling at NORMAL or lower, which then stop the explosion too
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPotionSplash(@NotNull final PotionSplashEvent event) {
		final ThrownPotion thrownPotion = event.getPotion();
		plugin.findPotion(thrownPotion.getItem()).ifPresent(potion -> {
			event.setCancelled(true);
			// The potion as the source makes the explosion count as the thrower's, for damage and for protections
			thrownPotion.getWorld().createExplosion(thrownPotion, thrownPotion.getLocation(),
					potion.explosionPower(), false, true);
		});
	}
}
