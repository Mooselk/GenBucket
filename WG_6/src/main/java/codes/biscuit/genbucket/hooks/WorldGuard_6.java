package codes.biscuit.genbucket.hooks;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class WorldGuard_6 implements WorldGuardHook {

    public boolean checkLocationBreakFlag(Chunk chunk, Player p) {
        WorldGuardPlugin worldGuardPlugin = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        RegionContainer container = worldGuardPlugin.getRegionContainer();
        Block[] cornerBlocks = {chunk.getBlock(0,chunk.getWorld().getMaxHeight()/2, 0), chunk.getBlock(0,chunk.getWorld().getMaxHeight()/2, 15),
                chunk.getBlock(15,chunk.getWorld().getMaxHeight()/2, 0), chunk.getBlock(15,chunk.getWorld().getMaxHeight()/2, 15)};
        for (Block currentBlock : cornerBlocks) {
            RegionQuery query = container.createQuery();

            ApplicableRegionSet set = query.getApplicableRegions(currentBlock.getLocation());
            LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(p);
            if (set.queryState(localPlayer, DefaultFlag.BLOCK_BREAK) == StateFlag.State.DENY) return false; // If block break is set to deny
        }
        return true;
    }

    public boolean checkLocationBreakFlag(Location singleBlock, Player p) {
        WorldGuardPlugin worldGuardPlugin = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        RegionContainer container = worldGuardPlugin.getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(singleBlock);
        LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(p);
        return set.queryState(localPlayer, DefaultFlag.BLOCK_BREAK) != StateFlag.State.DENY;
    }
}
