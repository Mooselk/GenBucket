package me.kate.genbuckets.listeners;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.kate.genbuckets.Main;
import me.kate.genbuckets.timers.GenningTimer;
import me.kate.genbuckets.utils.Bucket;

public class PlayerListener implements Listener {

    private Main main;

    public PlayerListener(Main main) {
        this.main = main;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
    	
        if (event.isCancelled()) {
            if (!main.getHookUtils().getBypassPlayers().contains(event.getPlayer()))
                return;
        }
        
        Bucket matchedBucket = main.getBucketManager().matchBucket(event.getItemInHand());
        if (matchedBucket != null) { // Sorry, you can't use it with offhand atm.
            event.setCancelled(true);
            startGenBucket(matchedBucket, 
            		event.getPlayer(), 
            		event.getBlock(), 
            		event.getBlockAgainst().getFace(
            		event.getBlock()), 
            		event.getItemInHand());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketPlace(PlayerInteractEvent event) {
        
    	if (event.isCancelled()) {
            if (!main.getHookUtils().getBypassPlayers().contains(event.getPlayer()))
                return;
        }
        
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) 
        		&& event.getItem() != null 
        		&& (event.getItem().getType().equals(Material.LAVA_BUCKET) 
        		|| event.getItem().getType().equals(Material.WATER_BUCKET))) {
            
        	Bucket bucket = main.getBucketManager().matchBucket(event.getItem());
            
            if (bucket != null) {
                event.setCancelled(true);
                startGenBucket(bucket, event.getPlayer(), event.getClickedBlock().getRelative(event.getBlockFace()), event.getBlockFace(), event.getItem());
            }
        }
    }

    private boolean noPlayersNearby(Player player) {
        double radius = main.getConfigValues().getPlayerCheckRadius();
        
        if (radius != 0) {
            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                
            	if (!(entity instanceof Player)) {
            		continue;
            	}
            	
                if (!main.getHookUtils().isFriendlyPlayer(player, (Player) entity)) {
                        
                    if (!main.getConfigValues().getNearbyPlayerMessage().equals("")) {
                    	player.sendMessage(main.getConfigValues().getNearbyPlayerMessage());
                    }
                    	
                    return false;
                }
            }
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        
    	if (main.getConfigValues().bucketsDisappearDrop()) {
            Bucket bucket = main.getBucketManager().matchBucket(event.getItemDrop().getItemStack());
            
            if (bucket != null && bucket.isInfinite()) {
                event.getItemDrop().remove();
            }
            
        }
    }
    
    private void startGenBucket(Bucket bucket, Player player, Block block, BlockFace direction, ItemStack removeItem) {
        
    	if (main.getHookUtils().canPlaceHere(player, block.getLocation()) && noPlayersNearby(player) && main.getHookUtils().takeBucketPlaceCost(player, bucket)) {
    		
            
        	if (bucket.getDirection() == Bucket.Direction.HORIZONTAL && (direction.equals(BlockFace.UP) || direction.equals(BlockFace.DOWN))) {
                
            	if (!main.getConfigValues().getWrongDirectionMessage().equals("")) {
                	player.sendMessage(main.getConfigValues().getWrongDirectionMessage());
                }
                return;
            }
        	
            switch (bucket.getDirection()) {
                case UPWARDS:
                    direction = BlockFace.UP;
                    break;
                case DOWNWARDS:
                    direction = BlockFace.DOWN;
                    break;
                default:
                	break;
            }
            
            int limit = main.getConfigValues().getVerticalTravel();
            
            if (direction != BlockFace.UP && direction != BlockFace.DOWN) {
                limit = main.getConfigValues().getHorizontalTravel();
            }
            
            GenningTimer genningTimer = new GenningTimer(player, bucket, block, direction, main, limit);
            genningTimer.runTaskTimer(main, 0L, main.getConfigValues().getBlockSpeedDelay());
            
            if (main.getConfigValues().cancellingEnabled()) {
                main.getUtils().getCurrentGens().put(block.getLocation(), genningTimer);
            }
            
            if (!bucket.isInfinite()) {
                
            	if (!main.getHookUtils().getBypassPlayers().contains(player)) {
                    
            		if (removeItem.getAmount() <= 1) {
                       
            			if (main.serverIsAfterOffhand() && !player.getItemInHand().equals(removeItem)) {
                            main.getHookUtils().clearOffhand(player);
                        
            			} else {
                        	
            				player.setItemInHand(null);
                        
            			}
                    } else {
                        
                    	removeItem.setAmount(removeItem.getAmount() - 1);
                    
                    }
                }
                
            	if (!main.getConfigValues().getPlaceNormalMessage(bucket.getPlacePrice()).equals("") && bucket.getPlacePrice() > 0) {
                	player.sendMessage(main.getConfigValues().getPlaceNormalMessage(bucket.getPlacePrice()));
                }
            	
            } else {
                
            	if (!main.getConfigValues().getPlaceInfiniteMessage(bucket.getPlacePrice()).equals("") && bucket.getPlacePrice() > 0) {
                	player.sendMessage(main.getConfigValues().getPlaceInfiniteMessage(bucket.getPlacePrice()));
                }
            	
            }
        }
    }

    @EventHandler
    public void onShopClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null 
        		&& event.getView().getTitle() != null 
        		&& event.getView().getTitle().equals(main.getConfigValues().getGUITitle())) {
            event.setCancelled(true);
            
            Player player = (Player) event.getWhoClicked();
            
            if (event.getCurrentItem() != null 
            		&& event.getCurrentItem().hasItemMeta() 
            		&& event.getCurrentItem().getItemMeta().hasDisplayName()) {
            	
                Bucket bucket = main.getBucketManager().fromShopName(event.getCurrentItem().getItemMeta().getDisplayName());
                
                if (bucket != null) {
                    
                	double price = bucket.getBuyPrice();
                    int amount = 1;
                    
                    if (event.getClick().equals(ClickType.SHIFT_LEFT) || event.getClick().equals(ClickType.SHIFT_RIGHT)) {
                        amount = 16;
                        price *= main.getConfigValues().getBulkBuyAmount();
                    }
                    
                    if (main.getHookUtils().takeShopMoney(player, price)) {
                        ItemStack item = bucket.getItem();
                        item.setAmount(amount);
                        
                        Map<?,?> excessItems;
                        
                        if (!main.getConfigValues().shopShouldDropItem()) {
                            if (player.getInventory().firstEmpty() == -1) {
                                if (!main.getConfigValues().getNoSpaceBuyMessage().equals("")) {
                                	player.sendMessage(main.getConfigValues().getNoSpaceBuyMessage());
                                }
                                return;
                            }
                        }
                        
                        excessItems = player.getInventory().addItem(item);
                        
                        for (Object excessItem : excessItems.values()) {
                        	
                            int itemCount = ((ItemStack) excessItem).getAmount();
                            
                            while (itemCount > 64) {
                                ((ItemStack) excessItem).setAmount(64);
                                player.getWorld().dropItemNaturally(player.getLocation(), (ItemStack) excessItem);
                                itemCount = itemCount - 64;
                            }
                            
                            if (itemCount > 0) {
                                ((ItemStack) excessItem).setAmount(itemCount);
                                player.getWorld().dropItemNaturally(player.getLocation(), (ItemStack) excessItem);
                            }
                            
                        }
                        
                        if (!main.getConfigValues().getBuyConfirmationMessage(amount).equals("")) {
                        	player.sendMessage(main.getConfigValues().getBuyConfirmationMessage(amount));
                        }
                        
                    }
                    
                } else if (main.getConfigValues().getExitName().equals(event.getCurrentItem().getItemMeta().getDisplayName())) {
                	player.closeInventory();
                }
            }
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (main.getConfigValues().cancellingEnabled()) {
            Location b = event.getBlock().getLocation();
            if (main.getUtils().getCurrentGens().containsKey(b)) {
                main.getUtils().getCurrentGens().get(b).cancel();
                main.getUtils().getCurrentGens().remove(b);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (main.getConfigValues().cancellingEnabled()) {
            Location b = event.getBlock().getLocation();
            if (main.getUtils().getCurrentGens().containsKey(b)) {
                main.getUtils().getCurrentGens().get(b).cancel();
                main.getUtils().getCurrentGens().remove(b);
            }
        }
    }
}