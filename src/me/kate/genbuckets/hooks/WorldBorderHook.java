package me.kate.genbuckets.hooks;

import org.bukkit.Location;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;

public class WorldBorderHook {

	boolean isInsideBorder(Location location) {
        BorderData border = Config.Border(location.getWorld().getName());
        if (border != null) {
            return border.insideBorder(location);
        } else {
            return true;
        }
    }
	
}
