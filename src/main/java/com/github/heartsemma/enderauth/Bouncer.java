package com.github.heartsemma.enderauth;

import java.util.HashSet;

import org.spongepowered.api.entity.living.player.Player;

import com.github.heartsemma.enderauth.Utilities.UUIDUtils;

/**
  * Used to keep track of authenticated users. 
 */
public class Bouncer {
	
	private static Bouncer bouncer;
	public static Bouncer getInstance(){ 
		if(bouncer==null){
			bouncer = new Bouncer();
		}
		return bouncer; 
	}
	
	private HashSet<byte[]> list; //Made of byte[] UUIDs. 
	private Main main;
	
	public Bouncer(){
		list = new HashSet<byte[]>();
		main = Main.getInstance();
	}
	
	/**
	 * @param player (Player to add to whitelist)
	 * 
	 * <br><br>Adds a user to the authenticated whitelist and allows them to perform regular actions as a player.
	 */
	public void addUser(Player player){
		list.add(UUIDUtils.getUUID(player));
	}
	
	/**
	 * @param player (The player to be searched for)
	 * @return A boolean representing the player's presence in the list.
	 * 
	 * <br><br>Returns true if the passed player is in the ArrayList, false if not.
	 */
	public boolean isOnList(Player player){
		byte[] uuid = UUIDUtils.getUUID(player);
		main.getLogger().debug("Checking if player " + new String(uuid) + " is in the list of authenticated users.");
		
		if(list.contains(uuid)){
			main.getLogger().debug("Player " + new String(uuid) + " found.");
			return true;
		}

		main.getLogger().debug("Player " + new String(uuid) + " not found.");
		return false;
	}
	
	/** @param player (The player to be removed from the list)
	 * 
	 * <br><br> Removed the passed player from the list. If the passed player does not exist in the list, nothing happens.
	 */
	public void removeUser(Player player){
		byte[] uuid = UUIDUtils.getUUID(player);
		main.getLogger().debug("Removing player " + new String(uuid) + " from list of authenticated users.");
		
		if(list.remove(uuid)){
			main.getLogger().debug("User successfully taken of the list.");
		} else {
			main.getLogger().error("EnderAuth was told to remove a specified player from the list but was unable to find them.");
		}
	}
}
