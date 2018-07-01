package eu.andret.explosivepotion;

import eu.andret.explosivepotion.entity.Potion;
import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;

@AllArgsConstructor
public class ExplosivePotionListener implements Listener {
    private final ExplosivePotion plugin;

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        Potion potion = plugin.getPotion(event.getPotion());
        if (potion != null) {
            Location location = event.getEntity().getLocation();
            location.getWorld().createExplosion(location, (float) potion.getExplosionPower());
            event.setCancelled(true);
        }
    }
}
