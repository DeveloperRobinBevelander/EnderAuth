package com.github.heartsemma.enderauth.Listeners;

import org.slf4j.Logger;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

import com.github.heartsemma.enderauth.Bouncer;
import com.github.heartsemma.enderauth.Main;
import com.google.common.base.Preconditions;

/*
 * Prevents unauthenticated players from:
 * - Breaking Blocks
 * - Placing Blocks
 * - Moving
 * - Entering non-enderauth commands
 */
public class PlayerInteractEvents {
	
	private final Main main = Main.getInstance();
	private final Logger logger = main.getLogger();
	private final Bouncer bouncer = Bouncer.getInstance();
	
	/**
	 * @param event (The event that occured)
	 * @param player (The player that triggered the event)
	 * @return A boolean that's true or false based on whether or not the player was cleared to perform the action.
	 * 
	 * <br> This function uses the Bouncer class to check the "authenticated player list" for the specified player. If they are not there,
	 * it cancels the event.
	 */
	private boolean vetAction(Cancellable event, Player player){
		logger.debug("Vetting player " + player.getName() + " to perform an action unknown action.");
		logger.debug("Checking for null parameters 'player' and 'event'.");
		Preconditions.checkNotNull(player);
		Preconditions.checkNotNull(event);
		
		boolean authenticated = false;
		
		if(!bouncer.isOnList(player)){
			event.setCancelled(true);
			authenticated = true;
		}
		
		return authenticated;
	}
	
	@Listener
	public void onBreakBlockEvent(ChangeBlockEvent.Break event, @First Player player){
		if(!vetAction(event, player)){;
			logger.debug("The Player " + player.getName() + " attempted to break blocks but needs to authenticate with EnderAuth first.");
		}
	}
	
	@Listener 
	public void onPlaceBlockEvent(ChangeBlockEvent.Place event, @First Player player){
		if(!vetAction(event, player)){;
			logger.debug("The Player " + player.getName() + " attempted to place blocks but needs to authenticate with EnderAuth first.");
		}
	}
	
	@Listener
	public void onMoveEvent(MoveEntityEvent event, @First Player player){
		if(!vetAction(event, player)){;
			logger.debug("The Player " + player.getName() + " attempted to move but needs to authenticate with EnderAuth first.");
		}
	}
}
