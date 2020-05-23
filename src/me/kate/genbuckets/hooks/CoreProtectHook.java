package me.kate.genbuckets.hooks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

public class CoreProtectHook {
	
	private CoreProtectAPI api =  ((CoreProtect) Bukkit.getServer().getPluginManager().getPlugin("CoreProtect")).getAPI();

    void logRemoval(String player, Location location, Material material, byte damage) {
        api.logRemoval(player + " (GenBucket)", location, material, damage);
    }

    void logPlacement(String player, Location location, Material material, byte damage) {
        api.logPlacement(player + " (GenBucket)", location, material, damage);
    }
    
}
