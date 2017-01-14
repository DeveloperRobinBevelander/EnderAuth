/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 heartsemma and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.heartsemma.enderauth.Listeners;

//Main
import com.github.heartsemma.enderauth.Main;

//Sponge Listener Packages
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.network.ClientConnectionEvent;

//Sponge Player
import org.spongepowered.api.entity.living.player.Player;

//Optional
import java.util.Optional;


public class ClientJoinEvent {
	
	//Returns true if the associated player is registered, false if not.
	public boolean isRegistered(Player player){
		//TODO: Introduce logic for determining status of registration.
		return true;
	}
	
	/*
	 * This is called after a player has successfully connected to a server.
	 * The goal is to prevent this player from doing anything until they have successfully authenticated themselves via our plugin.
	 * If the player has not registered yet they will be forced to now.
	 */
	@Listener
	public void onClientJoinEvent(ClientConnectionEvent.Join event){
		Cause cause = event.getCause();
		
		//Getting the player that joined.
		Optional<Player> arrivingPlayer = cause.first(Player.class);
		Player player;
		
		//Checking if the cause of the event is a player.
		if(!arrivingPlayer.isPresent()){
			Main.getInstance().getLogger().warn("A client appears to have joined but EnderAuth was unable to identify them.");
			Main.getInstance().getLogger().warn("We were unable to authenticate this player because Sponge was unable to tell us who or what joined.");
			return;
		} else {
			player = arrivingPlayer.get();
		}
		
		
		
		if(isRegistered(player)){
			//TODO: Authenticate the user
		} else {
			
		}
		
	}
}
