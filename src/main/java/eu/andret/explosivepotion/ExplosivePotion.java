package eu.andret.explosivepotion;

import eu.andret.explosivepotion.entity.Potion;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExplosivePotion extends JavaPlugin {
    private final List<Potion> potions = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setUpListeners();
        List<?> sections = getConfig().getList("potions");
        for (Object section : sections) {
            ConfigurationSection current = getConfig().createSection("current", (Map<?, ?>) section);
            ItemStack potion = new ItemStack(Material.SPLASH_POTION);
            PotionMeta itemMeta = (PotionMeta) potion.getItemMeta();
            itemMeta.setBasePotionData(new PotionData(PotionType.INSTANT_DAMAGE, false, false));
            itemMeta.setDisplayName(current.getString("item.name").replace('&', '\u00A7'));
            itemMeta.setLore(current.getStringList("item.lore").stream().map(s -> s.replace('&', '\u00A7')).collect(Collectors.toList()));
            potion.setItemMeta(itemMeta);
            potions.add(new Potion(potion, current.getDouble("explosion-power")));
            List<String> shape = current.getStringList("crafting.shape");
            Map<Character, Material> mapping = new HashMap<>();
            ConfigurationSection configurationSection = current.getConfigurationSection("crafting.mapping");
            for (String key : configurationSection.getKeys(false)) {
                Material mat = Material.getMaterial(configurationSection.getString(key));
                if (mat != null) {
                    mapping.put(key.charAt(0), mat);
                }
            }
            createRecipe(potion, shape, mapping);
        }
    }

    private void createRecipe(ItemStack target, List<String> shape, Map<Character, Material> mapping) {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, (getDescription().getFullName() + "-" + target.getItemMeta().getDisplayName()).replace(' ', '_')), target);
        recipe.shape(shape.toArray(new String[]{}));
        for (Map.Entry<Character, Material> entry : mapping.entrySet()) {
            recipe.setIngredient(entry.getKey(), entry.getValue());
        }
        getServer().addRecipe(recipe);
    }

    private void setUpListeners() {
        getServer().getPluginManager().registerEvents(new ExplosivePotionListener(this), this);
    }

    Potion getPotion(ThrownPotion thrownPotion) {
        for (Potion potion : potions) {
            if (thrownPotion.getItem().equals(potion.getPotion())) {
                return potion;
            }
        }
        return null;
    }
}