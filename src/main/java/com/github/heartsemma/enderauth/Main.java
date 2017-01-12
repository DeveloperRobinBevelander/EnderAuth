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
import org.spongepowered.api.event.game.state.GameStartedServerEvent;

//Logging
import org.slf4j.Logger;
import com.google.inject.Inject;

//Main Class.
@Plugin(id = "enderauth", name = "EnderAuth", version = "0.1 (Alpha)")
public class Main {
	
	
	
	//Global, plugin wide variables
	private final Logger logger;
	private final Game game;
	private final PluginContainer pluginContainer;
	
	private static Main instance;
	public static Main getInstance(){
		return instance;
	}
	
	@Inject
	public Main(Logger logger, Game game, PluginContainer pluginContainer){
		this.logger = logger;
		this.game = game;
		this.pluginContainer = pluginContainer;
	}
	
	//Introducing ourselves.
	@Listener
    public void onServerStart(GameStartedServerEvent event) {
		logger.info("EnderAuth is initializing. Thank you for taking the steps to properly secure your users.");
		logger.info("Visit spongepowered.com for more information on how to use and configure EnderAuth.");
		
		//Initializing Listeners
		Sponge.getEventManager().registerListeners(this, new ClientJoinEvent()); //When a client successfully connects to a server.
		
		
    }
	
	
	//Get Methods
	public Logger getLogger(){ return logger; }
	public Game getGame(){ return game; }
	public PluginContainer pluginContainer(){ return pluginContainer; }
	
	
	
}
