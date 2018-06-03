package eu.andret.tntpotion;

import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;

@AllArgsConstructor
public class TNTPotionListeners implements Listener {
    private final atsTNTPotion plugin;

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        for (TNTPotion tntPotion : plugin.getPotions()) {
            if (event.getPotion().getItem().equals(tntPotion.getPotion())) {
                event.getEntity().getLocation().getWorld().createExplosion(event.getEntity().getLocation(), (float) tntPotion.getExplosionPower());
                event.setCancelled(true);
            }
        }
    }
}
