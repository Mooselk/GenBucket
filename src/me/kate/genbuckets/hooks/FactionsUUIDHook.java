package me.kate.genbuckets.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Relation;

public class FactionsUUIDHook {
	
	boolean hasFaction(Player p) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(p);
        return fPlayer.hasFaction();
    }

    boolean isNotWilderness(Location loc) {
        FLocation fLoc = new FLocation(loc);
        Faction fLocFaction = Board.getInstance().getFactionAt(fLoc);
        return !fLocFaction.isWilderness();
    }

    boolean locationIsFactionClaim(Location loc, Player p) {
        Faction locFaction = Board.getInstance().getFactionAt(new FLocation(loc));
        Faction pFaction = FPlayers.getInstance().getByPlayer(p).getFaction();
        return locFaction.equals(pFaction);
    }

    boolean isFriendlyPlayer(Player p, Player otherP) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(p);
        FPlayer otherFPlayer = FPlayers.getInstance().getByPlayer(otherP);
        Relation relation = fPlayer.getRelationTo(otherFPlayer);
        return relation == Relation.MEMBER || relation == Relation.ALLY || relation == Relation.TRUCE;
    }
    
}
