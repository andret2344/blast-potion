package eu.andret.atstntpotion;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.meta.ItemMeta;

public class TNTPotionListeners implements Listener {
    private final Plugin plugin;

    public TNTPotionListeners(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        ItemMeta meta1 = event.getPotion().getItem().getItemMeta();
        ItemMeta meta2 = plugin.getTNT().getItemMeta();
        Location loc = event.getEntity().getLocation();
        if (meta1.getDisplayName().equals(meta2.getDisplayName())) {
            float power;
            try {
                power = (float) plugin.getConfig().getDouble("explode-power");
            } catch (Exception ex) {
                power = 3.25;
            }
            loc.getWorld().createExplosion(loc, a);
            event.setCancelled(true);
        }
    }
}
