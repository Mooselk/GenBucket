package me.kate.genbuckets.hooks;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import me.kate.genbuckets.Main;
import me.kate.genbuckets.abstraction.MinecraftAbstraction;
import me.kate.genbuckets.abstraction.Minecraft_1_8;
import me.kate.genbuckets.abstraction.Minecraft_1_9;
import me.kate.genbuckets.abstraction.WorldGuardHook;
import me.kate.genbuckets.utils.Bucket;
import me.kate.genbuckets.utils.ConfigValues;
import me.kate.genbuckets.worldguard.WorldGuard_6;
import net.milkbowl.vault.economy.Economy;

public class HookUtils {
	
    private Map<Hooks, Object> enabledHooks = new EnumMap<>(Hooks.class);
    private Economy economy;
    private Set<OfflinePlayer> bypassPlayers = new HashSet<>();
    private ConfigValues configValues;

    public HookUtils(Main main) {
    	
        this.configValues = main.getConfigValues();
        this.economy = main.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        
        PluginManager pm = main.getServer().getPluginManager();
        
        if (configValues.isFactionsHookEnabled() 
        		&& pm.getPlugin("MassiveCore") != null 
        		&& pm.getPlugin("Factions") != null 
        		&& pm.getPlugin("Factions").getDescription().getDepend().contains("MassiveCore")) {
        	
            main.getLogger().info("Hooked into MassiveCore factions");
            enabledHooks.put(Hooks.MASSIVECOREFACTIONS, new MassiveCoreHook());
            
        } else if (configValues.isFactionsHookEnabled() && pm.getPlugin("Factions") != null) {
            
        	main.getLogger().info("Hooked into FactionsUUID/SavageFactions");
            enabledHooks.put(Hooks.FACTIONSUUID, new FactionsUUIDHook());
        
        }
        
        if (configValues.isWorldGuardHookEnabled() && pm.getPlugin("WorldGuard") != null) {
        	
            String pluginVersion = main.getServer().getPluginManager().getPlugin("WorldGuard").getDescription().getVersion();
            
            if (pluginVersion.startsWith("6")) {
                
            	main.getLogger().info("Hooked into WorldGuard 6");
                enabledHooks.put(Hooks.WORLDGUARD, new WorldGuard_6());
            
            }
        }
        
        if (configValues.isWorldBorderHookEnabled() && pm.getPlugin("WorldBorder") != null) {
            main.getLogger().info("Hooked into WorldBorder");
            enabledHooks.put(Hooks.WORLDBORDER, new WorldBorderHook());
        }
        
        if (configValues.isCoreProtectHookEnabled() && pm.getPlugin("CoreProtect") != null) {
            main.getLogger().info("Hooked into CoreProtect");
            enabledHooks.put(Hooks.COREPROTECT, new CoreProtectHook());
        }
        
        if (main.serverIsBeforeFlattening()) {
            main.getLogger().info("Hooked into Minecraft 1.8-1.12");
            enabledHooks.put(Hooks.MINECRAFTONEEIGHT, new Minecraft_1_8());
        }
        
        if (main.serverIsAfterOffhand()) {
            main.getLogger().info("Hooked into Minecraft 1.9+");
            enabledHooks.put(Hooks.MINECRAFTONENINE, new Minecraft_1_9());
        }
    }

    public void clearOffhand(Player player) {
    	
        if (enabledHooks.containsKey(Hooks.MINECRAFTONENINE)) {
            MinecraftAbstraction minecraftHook = (MinecraftAbstraction) enabledHooks.get(Hooks.MINECRAFTONENINE);
            minecraftHook.clearOffhand(player);
        }
        
    }

    public void setData(Block block, byte data) {
    	
        if (enabledHooks.containsKey(Hooks.MINECRAFTONEEIGHT)) {
            MinecraftAbstraction minecraftHook = (MinecraftAbstraction) enabledHooks.get(Hooks.MINECRAFTONEEIGHT);
            minecraftHook.setBlockData(block, data);
        }
        
    }

    public boolean canPlaceHere(Player player, Location location) {
        
    	if (!player.hasPermission("genbucket.use")) {
            cannotPlaceNoPermission(player);
            return false;
        }
        
        if (location.getBlockY() > configValues.getMaxY()) {
            cannotPlaceYLevel(player);
            return false;
        }
        
        if (enabledHooks.containsKey(Hooks.MASSIVECOREFACTIONS)) {
            MassiveCoreHook massiveCoreHook = (MassiveCoreHook) enabledHooks.get(Hooks.MASSIVECOREFACTIONS);
            
            if (configValues.needsFaction() && massiveCoreHook.hasNoFaction(player)) {
                cannotPlaceNoFaction(player);
                return false;
            }
            
            if (!massiveCoreHook.isWilderness(location) && massiveCoreHook.locationIsNotFaction(location, player)) {
                onlyClaim(player);
                return false;
            }
            
            if (configValues.cantPlaceWilderness() && massiveCoreHook.isWilderness(location)) {
                onlyClaim(player);
                return false;
            }
            
        } else if (enabledHooks.containsKey(Hooks.FACTIONSUUID)) {
            FactionsUUIDHook factionsUUIDHook = (FactionsUUIDHook) enabledHooks.get(Hooks.FACTIONSUUID);
            
            if (configValues.needsFaction() && !factionsUUIDHook.hasFaction(player)) {
                cannotPlaceNoFaction(player);
                return false;
            }
            
            if (factionsUUIDHook.isNotWilderness(location) && !factionsUUIDHook.locationIsFactionClaim(location, player)) {
                onlyClaim(player);
                return false;
            }
            
            if (configValues.cantPlaceWilderness() && !factionsUUIDHook.isNotWilderness(location)) {
                onlyClaim(player);
                return false;
            }
        }
        return true;
    }

    public boolean canGenChunk(Player player, Chunk chunk) {
        Location middle = chunk.getBlock(7, 63, 7).getLocation();
        
        if (enabledHooks.containsKey(Hooks.MASSIVECOREFACTIONS)) {
            MassiveCoreHook massiveCoreHook = (MassiveCoreHook) enabledHooks.get(Hooks.MASSIVECOREFACTIONS);
            
            if (configValues.needsFaction() && massiveCoreHook.hasNoFaction(player))
                return false;

            if (!massiveCoreHook.isWilderness(middle) && massiveCoreHook.locationIsNotFaction(middle, player))
                return false;
            
            return !configValues.cantPlaceWilderness() || !massiveCoreHook.isWilderness(middle);
        
        } else if (enabledHooks.containsKey(Hooks.FACTIONSUUID)) {
            FactionsUUIDHook factionsUUIDHook = (FactionsUUIDHook) enabledHooks.get(Hooks.FACTIONSUUID);
            
            if (configValues.needsFaction() && !factionsUUIDHook.hasFaction(player))
                return false;

            if (factionsUUIDHook.isNotWilderness(middle) && !factionsUUIDHook.locationIsFactionClaim(middle, player))
                return false;
            
            return !configValues.cantPlaceWilderness() || factionsUUIDHook.isNotWilderness(middle);
        }
        return true;
    }

    public boolean canGenBlock(Player player, Location block, boolean horizontal) {
        if (enabledHooks.containsKey(Hooks.WORLDGUARD)) {
            
        	WorldGuardHook worldGuardHook = (WorldGuardHook) enabledHooks.get(Hooks.WORLDGUARD);
            
            if (!worldGuardHook.canBreakBlock(block, player))
                return false;
            
        }
        
        if (configValues.getSpongeCheckRadius() > 0) {
            
        	double radius = configValues.getSpongeCheckRadius();
            
            for (double x = block.getX() - radius; x < block.getX() + radius; x++) {
                for (double y = block.getY() - radius; y < block.getY() + radius; y++) {
                    for (double z = block.getZ() - radius; z < block.getZ() + radius; z++) {
                       
                    	Block b = new Location(block.getWorld(), x, y, z).getBlock();
                        if (!b.getLocation().equals(block) && b.getType() == Material.SPONGE)
                            return false;

                    }
                }
            }
        }
        
        if (horizontal) {
            
        	if (enabledHooks.containsKey(Hooks.WORLDBORDER)) {
                WorldBorderHook worldBorderHook = (WorldBorderHook) enabledHooks.get(Hooks.WORLDBORDER);
                
                if (!worldBorderHook.isInsideBorder(block))
                    return false;
                
            }
            
//        	WorldBorder border = block.getWorld().getWorldBorder();
            
//        	double x = block.getBlockX();
//          double z = block.getBlockZ();
//          double size = border.getSize() / 2;
//            
//          Location center = border.getCenter();
//            
            
            
            return true;
//          return !((x >= center.clone().add(size, 0, 0).getX() || z >= center.clone().add(0, 0, size).getZ()) || (x <= center.clone().subtract(size,0,0).getX() || (z <= center.clone().subtract(0,0, size).getZ())));
        }
        return true;
    }

    private void cannotPlaceNoFaction(Player player) {
    	
        if (!configValues.getNoFactionMessage().equals(""))
            player.sendMessage(configValues.getNoFactionMessage());
        
    }

    private void onlyClaim(Player player) {
    	
        if (!configValues.getOnlyClaimMessage().equals(""))
            player.sendMessage(configValues.getOnlyClaimMessage());
        
    }

//    private void cannotPlaceRegion(Player player) {
//        if (!configValues.getRegionProtectedMessage().equals("")) {
//            player.sendMessage(configValues.getRegionProtectedMessage());
//        }
//    }

    private void cannotPlaceNoPermission(Player player) {
    	
        if (!configValues.getNoPermissionPlaceMessage().equals(""))
            player.sendMessage(configValues.getNoPermissionPlaceMessage());
        
    }

    private void cannotPlaceYLevel(Player player) {
    	
        if (!configValues.getCannotPlaceYLevelMessage().equals(""))
            player.sendMessage(configValues.getCannotPlaceYLevelMessage());
        
    }

    public boolean takeBucketPlaceCost(Player player, Bucket bucket) {
    	
        if (bypassPlayers.contains(player))
            return true
            		;
        if (hasMoney(player, bucket.getPlacePrice())) {
            removeMoney(player, bucket.getPlacePrice());
            return true;
            
        } else {
            
        	if (!configValues.notEnoughMoneyPlaceMessage(bucket.getPlacePrice()).equals("")) {
            	player.sendMessage(configValues.notEnoughMoneyPlaceMessage(bucket.getPlacePrice()));
            }
        	
            return false;
        }
    }

    public boolean takeShopMoney(Player player, double amount) {
        
    	if (bypassPlayers.contains(player))
            return true;
        
        if (hasMoney(player, amount)) {
            removeMoney(player, amount);
            return true;
        
        } else {
            
        	if (!configValues.notEnoughMoneyBuyMessage(amount).equals("")) {
                player.sendMessage(configValues.notEnoughMoneyBuyMessage(amount));
            }
        	
            return false;
        }
    }

    public boolean isFriendlyPlayer(Player player, Player player2) {
    	
        if (enabledHooks.containsKey(Hooks.MASSIVECOREFACTIONS)) {
            MassiveCoreHook massiveCoreHook = (MassiveCoreHook)enabledHooks.get(Hooks.MASSIVECOREFACTIONS);
            return massiveCoreHook.isFriendlyPlayer(player, player2);
            
        } else if (enabledHooks.containsKey(Hooks.FACTIONSUUID)) {
            
        	FactionsUUIDHook factionsUUIDHook = (FactionsUUIDHook) enabledHooks.get(Hooks.FACTIONSUUID);
            return factionsUUIDHook.isFriendlyPlayer(player, player2);
        
        } else {
            return false; // Should the default be true or false? Idk what would work best.
        }
    }

    private boolean hasMoney(Player player, double money) {
        return economy.has(player, money);
    }

    private void removeMoney(Player player, double money) {
        economy.withdrawPlayer(player, money);
    }

    public void logRemoval(Player player, Location location, Material mat, byte damage) {
        
    	if (enabledHooks.containsKey(Hooks.COREPROTECT)) {
            CoreProtectHook coreProtectHook = (CoreProtectHook) enabledHooks.get(Hooks.COREPROTECT);
            coreProtectHook.logRemoval(player.getName(), location, mat, damage);
        }
    	
    }

    public void logPlacement(Player player, Location location, Material mat, byte damage) {
        
    	if (enabledHooks.containsKey(Hooks.COREPROTECT)) {
            CoreProtectHook coreProtectHook = (CoreProtectHook)enabledHooks.get(Hooks.COREPROTECT);
            coreProtectHook.logPlacement(player.getName(), location, mat, damage);
        }
    	
    }

    public Set<OfflinePlayer> getBypassPlayers() {
        return bypassPlayers;
    }

    enum Hooks {
    	
        FACTIONSUUID,
        MASSIVECOREFACTIONS,
        COREPROTECT,
        WORLDGUARD,
        WORLDBORDER,
        MINECRAFTONEEIGHT,
        MINECRAFTONENINE
        
    }
}
