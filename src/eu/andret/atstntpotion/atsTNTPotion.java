package eu.andret.atstntpotion;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.Arrays;

public class atsTNTPotion extends JavaPlugin {
    private final ItemStack tnt = new ItemStack(Material.SPLASH_POTION, 1);
    private final Listener listener = new TNTPotionListeners(this);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        PotionMeta potionMeta = (PotionMeta) tnt.getItemMeta();
        potionMeta.setBasePotionData(new PotionData(PotionType.INSTANT_DAMAGE, false, false));
        potionMeta.setDisplayName(((String) getConfig().get("name")).replace('&', '§'));
        String[] lore = ((String) getConfig().get("desc")).replace('&', '§').split("\r");
        potionMeta.setLore(Arrays.asList(lore));
        tnt.setItemMeta(potionMeta);
        ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(this, "potion"), tnt);
        shapedRecipe.shape("@!@", " # ", " $ ");
        shapedRecipe.setIngredient('@', Material.NETHER_STALK);
        shapedRecipe.setIngredient('!', Material.GOLD_BLOCK);
        shapedRecipe.setIngredient('#', Material.TNT);
        shapedRecipe.setIngredient('$', Material.ARROW);
        Bukkit.getServer().addRecipe(shapedRecipe);
    }

    public ItemStack getTNT() {
        return tnt;
    }

}