package me.kate.genbuckets.abstraction;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface WorldGuardHook {

//  boolean checkChunkBreakFlag(Chunk chunk, Player p);

  boolean canBreakBlock(Location block, Player player);
  
}
