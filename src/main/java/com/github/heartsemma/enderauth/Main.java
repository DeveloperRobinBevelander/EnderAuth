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

//In House Listeners
import com.github.heartsemma.enderauth.Listeners.ClientJoinEvent;
//Main Plugin API Class
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
//Events
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
//Logging
import org.slf4j.Logger;
import com.google.inject.Inject;

//Main Class.
@Plugin(id = "enderauth", name = "EnderAuth", version = "0.1 (Alpha)")
public class Main {
	
	private static Main instance;
	public static Main getInstance(){
		return instance;
	}
	
	//Global, plugin wide variables related to the Sponge API
	private final Logger logger;
	private final Game game;
	private final PluginContainer pluginContainer;
	
	//If there was dramatic error somewhere in the program, 
	private boolean majorFailure = false;
	
	//Database/Configuration variables
	private Database database;
	
	@Inject
	public Main(Logger logger, Game game, PluginContainer pluginContainer){
		this.logger = logger;
		this.game = game;
		this.pluginContainer = pluginContainer;
	}
	
	//Initializing the plugin.
	@Listener
	public void onPreInit(GamePreInitializationEvent preInitEvent) {
		logger.info("EnderAuth is initializing. Thank you for taking the steps to properly secure your users.");
		logger.info("Visit the spongepowered.com forums for more information on how to use and configure EnderAuth.");
		
		//Initializing Global Variables
		database = new Database();
		
		
    }
	//After initialization, if nothing went wrong, install listeners.
	@Listener
	public void onPostInit(GamePostInitializationEvent postInitEvent){
		
		//Make sure initialization went well.
		if(majorFailure){
			logger.error("Initialization didn't go well, and thus EnderAuth is not attempting to register listeners.");
			logger.error("The plugin will not load and is not enabled.");
			logger.error("If your server relies on this plugin for its security, TURN OF YOUR SERVER NOW.");
			assert(!majorFailure);
		}
		
		
		//Initializing Listeners. "We're online"
		Sponge.getEventManager().registerListeners(this, new ClientJoinEvent()); //When a client successfully finishes connecting to a server.
		
	}
	
	//Get Methods
	public Logger getLogger(){ return logger; }
	public Game getGame(){ return game; }
	public PluginContainer getPluginContainer(){ return pluginContainer; }
	public Database getDatabase(){ return database; }
	public void majorFailure(){ majorFailure = true; }
	
	
}
