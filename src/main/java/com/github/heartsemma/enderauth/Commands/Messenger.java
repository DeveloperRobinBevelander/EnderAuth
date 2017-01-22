package com.github.heartsemma.enderauth.Commands;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import org.slf4j.Logger;

import com.github.heartsemma.enderauth.Main;
import com.google.common.base.Preconditions;

/**
 * Provides functions and methods to help with messaging the user certain details. 
 */
public class Messenger {
	
	//Colors related to the theme of the plugin and used throughout.
	private TextColor mainColor;
	private TextColor secondaryColor;
	private TextColor tertiaryColor;
	private TextColor boldColor;
	private TextColor errorColor;
	
	private Main main = Main.getInstance();
	private Logger logger = main.getLogger();
	
	//Singleton
	private static Messenger INSTANCE;
	public static Messenger getInstance(){ 
		if(INSTANCE==null){
			INSTANCE = new Messenger();
		}
		return INSTANCE; 
	}
	
	//Messages
	public Messenger(){
		mainColor = TextColors.GOLD;
		secondaryColor = TextColors.YELLOW;
		tertiaryColor = TextColors.GRAY;
		boldColor = TextColors.DARK_GRAY;
		errorColor = TextColors.DARK_RED;
	}
	
	
	
	/**
	 * @param player (The player to send the plugin introduction to).
	 * 
	 * <br><br>This function sends the player an introduction to the passed player.
	 */
	public void sendIntroduction(Player player){
		
		Preconditions.checkNotNull(player);
		
		logger.debug("Sending introduction message to player.");
		
		String messageString1 = "EnderAuth is a plugin that provides Two-Factor Authentication to minecraft players.";
		Text message1 = Text.builder(messageString1).color(mainColor).build();
		player.sendMessage(message1);
		
		String messageString2 = "TOTP (the protocol EnderAuth uses) requires pairing or registration.";
		Text message2 = Text.builder(messageString2).color(mainColor).build();
		player.sendMessage(message2);
		
		String messageString3 = "Type {INSERT_COMMAND_HERE} to learn more.";
		Text message3 = Text.builder(messageString3).color(mainColor).build();
		player.sendMessage(message3);
		
		logger.debug("Sent introduction message to player.");
	}


	/**
	 * @param player (The player to send the plugin introduction to).
	 * 
	 * <br><br>This function sends the player a regular greeting and asks them to login.
	 */
	public void welcomeBack(Player player) {
		
		Preconditions.checkNotNull(player);
		
		logger.debug("Sending welcome back message to player");
		
		String messageString = "Welcome back " + player.getName() + ". Please login";
		Text message = Text.builder(messageString).color(mainColor).build();
		player.sendMessage(message);
		
		logger.debug("Sent welcome message to player.");
	}
	
	
	
	
}
