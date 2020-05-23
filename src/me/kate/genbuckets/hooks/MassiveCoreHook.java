package me.kate.genbuckets.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;

public class MassiveCoreHook {
	
	boolean hasNoFaction(Player player) {
        return !MPlayer.get(player).hasFaction();
    }

    boolean isWilderness(Location location) {
        return BoardColl.get().getFactionAt(PS.valueOf(location)).isNone();
    }

    boolean locationIsNotFaction(Location location, Player player) {
        Faction locFaction = BoardColl.get().getFactionAt(PS.valueOf(location));
        Faction pFaction = MPlayer.get(player).getFaction();
        return !locFaction.equals(pFaction);
    }

    boolean isFriendlyPlayer(Player player, Player otherPlayer) {
        MPlayer mPlayer = MPlayer.get(player);
        MPlayer otherMPlayer = MPlayer.get(otherPlayer);
        Rel relation = mPlayer.getRelationTo(otherMPlayer);
        return relation == Rel.MEMBER || relation == Rel.ALLY || relation == Rel.TRUCE;
    }
    
}
