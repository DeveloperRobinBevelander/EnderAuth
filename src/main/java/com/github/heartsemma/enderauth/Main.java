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

package com.github.heartsemma.enderauth;

import com.github.heartsemma.enderauth.Commands.RegisterCommand;
import com.github.heartsemma.enderauth.Listeners.ClientJoinEvent;

import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;

import org.slf4j.Logger;
import com.google.inject.Inject;

@Plugin(id = "enderauth", name = "EnderAuth", version = "0.1 (Alpha)")
public class Main {
	
	private static Main instance;
	public static Main getInstance(){
		return instance;
	}
	
	//Global, plugin wide variables related to the Sponge API
	private final Logger logger;
	private final Game game = Sponge.getGame();
	private final PluginContainer pluginContainer;
	
	private boolean killSwitchPulled = false;
	
	@Inject
	public Main(Logger logger, Game game, PluginContainer pluginContainer){
		this.logger = logger;
		this.game = game;
		this.pluginContainer = pluginContainer;
	}
	
	@Listener
	public void onPreInit(GamePreInitializationEvent preInitEvent) {
		logger.info("EnderAuth is initializing. Thank you for taking the steps to properly secure your users.");
		logger.info("Visit the spongepowered.com forums for more information on how to use and configure EnderAuth.");
		
		//Initializing Global Variables
		logger.debug("Initializing important variables.");
    }
	
	//After initialization, if nothing went wrong, install listeners.
	@Listener
	public void onInit(GameInitializationEvent postInitEvent){
		
		//If the kill switch has been pulled the plugin has lost critical functionality and we must not register listeners.
		if(killSwitchPulled){ return; }
		
		Sponge.getEventManager().registerListeners(this, new ClientJoinEvent()); 
		
		CommandSpec register = CommandSpec.builder()
			.description(Text.of("User Registration Command"))
		    .permission(pluginContainer.getId() + ".user.command.help")
		    .executor(new RegisterCommand())
		    .build();
		
		CommandSpec ea = CommandSpec.builder()
			.description(Text.of("Base command for interacting with EnderAuth"))
			.child(register, "register")
			.build();
		
		Sponge.getCommandManager().register(this, ea);
		
	}
	
	/** @return The final Logger 'logger' from Main. <br><br>This should be the only object used to log events by policy.*/
	public Logger getLogger(){ return logger; } 
	
	/** @return The final Game 'game' from Main.*/
	public Game getGame(){ return game; }
	
	/** @return The final PluginContainer 'pluginContainer' from Main.*/
	public PluginContainer getPluginContainer(){ return pluginContainer; }
	
	/** Shuts down the program in case of major unrecoverable failure, security incident, etc. 
	 * <br><br>Specifically, this unregisters all listeners. If the plugin is configured to shut down the server when encountering major error,
	 * it does that as well.*/
	public void killPlugin(){ 
		Sponge.getEventManager().unregisterListeners(this);
		killSwitchPulled = true;
	}
	
	
}
