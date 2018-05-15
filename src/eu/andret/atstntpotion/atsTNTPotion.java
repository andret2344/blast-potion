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
    private static ItemStack tnt;
    private static FileConfiguration config;
    private static atsTNTPotion instance;

    @Override
    public void onEnable() {
        atsTNTPotion.instance = this;
        atsTNTPotion.config = getConfig();
        atsTNTPotion.config.addDefault("name", "&5ThrowableTNT");
        atsTNTPotion.config.addDefault("desc", "&2Throw it\r&2to explode!");
        atsTNTPotion.config.addDefault("explode-power", 3.25);
        atsTNTPotion.config.options().copyDefaults(true);
        saveConfig();
        atsTNTPotion.tnt = new ItemStack(Material.SPLASH_POTION, 1);
        PotionMeta potionMeta = (PotionMeta) atsTNTPotion.tnt.getItemMeta();
        potionMeta.setBasePotionData(new PotionData(PotionType.INSTANT_DAMAGE, false, false));
        atsTNTPotion.tnt.setItemMeta(potionMeta);
        ItemMeta meta = atsTNTPotion.tnt.getItemMeta();
        meta.setDisplayName(((String) atsTNTPotion.config.get("name")).replace('&', '�'));
        String[] lore = ((String) atsTNTPotion.config.get("desc")).replace('&', '�').split("\r");
        meta.setLore(Arrays.asList(lore));
        atsTNTPotion.tnt.setItemMeta(meta);
        ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(this, "potion"), atsTNTPotion.tnt);
        shapedRecipe.shape("@!@", " # ", " $ ");
        shapedRecipe.setIngredient('@', Material.NETHER_STALK);
        shapedRecipe.setIngredient('!', Material.GOLD_BLOCK);
        shapedRecipe.setIngredient('#', Material.TNT);
        shapedRecipe.setIngredient('$', Material.ARROW);
        Bukkit.getServer().addRecipe(shapedRecipe);
        Bukkit.getPluginManager().registerEvents(new TNTPotionListeners(), this);
    }

    public static ItemStack getTNT() {
        return atsTNTPotion.tnt;
    }

    public static atsTNTPotion getInstance() {
        return atsTNTPotion.instance;
    }

}