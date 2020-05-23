package me.kate.genbuckets.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import me.kate.genbuckets.Main;
import me.kate.genbuckets.timers.GenningTimer;
import net.md_5.bungee.api.ChatColor;

public class Utils {

    private Main main;
    private List<Recipe> recipeList = new ArrayList<>();
    private Map<Location, GenningTimer> currentGens = new HashMap<>();

    public Utils(Main main) {
        this.main = main;
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> colorLore(List<String> lore) {
        for (int i = 0; i < lore.size(); i++) {
            lore.set(i, color(lore.get(i)));
        }
        return lore; // For convenience
    }

    public ItemStack itemFromString(String rawItem) {
        Material material;
        String[] rawSplit;
        
        if (rawItem.contains(":")) {
            rawSplit = rawItem.split(":");
        } else {
            rawSplit = new String[] {rawItem};
        }
        
        try {
            material = Material.valueOf(rawSplit[0]);
        } catch (IllegalArgumentException ex) {
            material = Material.DIRT;
        }
        
        short damage = 0;
        
        if (rawSplit.length > 1) {
            try {
                damage = Short.valueOf(rawSplit[1]);
            } catch (IllegalArgumentException ignored) {}
        }
        
        return new ItemStack(material, 1, damage);
    }

    public ItemStack addGlow(ItemStack item) {
        item.addUnsafeEnchantment(Enchantment.LUCK, 1);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    @SuppressWarnings("deprecation")
	public void registerRecipes() {
        if (main.getConfigValues().getRecipeBuckets() != null) {
            for (String bucketID : main.getConfigValues().getRecipeBuckets()) {
                Bucket bucket = main.getBucketManager().getBucket(bucketID);
                ItemStack item = bucket.getItem();
                item.setAmount(bucket.getRecipeAmount());
                ShapedRecipe newRecipe = new ShapedRecipe(item);
                if (bucket.getRecipeShape() != null) {
                    newRecipe.shape(bucket.getRecipeShape().get(0), bucket.getRecipeShape().get(1), bucket.getRecipeShape().get(2));
                } else continue;
                if (bucket.getIngredients() != null) {
                    for (Map.Entry<Character, ItemStack> ingredient : bucket.getIngredients().entrySet()) {
                        char symbol = ingredient.getKey();
                        ItemStack ingredientItem = ingredient.getValue();
                        if (ingredientItem.getData().getData() != 1) {
                            newRecipe.setIngredient(symbol, ingredientItem.getData());
                        } else {
                            newRecipe.setIngredient(symbol, ingredientItem.getType());
                        }
                    }
                } else continue;
                main.getServer().addRecipe(newRecipe);
                recipeList.add(newRecipe);
            }
        }
    }

    public void reloadRecipes() {
        List<Recipe> oldRecipes = new ArrayList<>();
        {
            Iterator<Recipe> allRecipes = main.getServer().recipeIterator();
            while(allRecipes.hasNext())	{
                
            	Recipe recipe = allRecipes.next();
                
                if(!recipeList.contains(recipe))
                    oldRecipes.add(recipe);
            }
        }
        
        main.getServer().clearRecipes();
        recipeList.clear();
        
        for (Recipe recipe : oldRecipes) 
        	main.getServer().addRecipe(recipe);
        
        registerRecipes();
    }

    public void updateConfig() {
        if (main.getConfigValues().getConfigVersion() < 1.3) {
            Map<String, Object> oldValues = new HashMap<>();
            for (String oldKey : main.getConfig().getKeys(true)) {
                oldValues.put(oldKey, main.getConfig().get(oldKey));
            }
            main.saveResource("config.yml", true);
            main.reloadConfig();
            for (String newKey : main.getConfig().getKeys(true)) {
                if (oldValues.containsKey(newKey)) {
                    main.getConfig().set(newKey, oldValues.get(newKey));
                }
            }
            main.getConfig().set("config-version", 1.3);
            main.saveConfig();
        }
    }

    public Map<Location, GenningTimer> getCurrentGens() {
        return currentGens;
    }
}