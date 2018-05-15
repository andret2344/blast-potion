package eu.andret.atstntpotion;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.meta.ItemMeta;

public class TNTPotionListeners implements Listener {
    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        try {
            ItemMeta meta1 = event.getPotion().getItem().getItemMeta();
            ItemMeta meta2 = atsTNTPotion.getTNT().getItemMeta();
            Location loc = event.getEntity().getLocation();
            if (meta1.getDisplayName().equals(meta2.getDisplayName())) {
                float a = (float) atsTNTPotion.getInstance().getConfig().getDouble("explode-power");
                loc.getWorld().createExplosion(loc, a);
                event.setCancelled(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
