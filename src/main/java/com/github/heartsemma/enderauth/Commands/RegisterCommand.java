package com.github.heartsemma.enderauth.Commands;


import org.slf4j.Logger;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

import com.github.heartsemma.enderauth.Main;

public class RegisterCommand implements CommandExecutor{

	private final Main main = Main.getInstance();
	private final Logger logger = main.getLogger();
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext context) throws CommandException {
		logger.debug("Register command was entered.");
		
		if(!(src instanceof Player)){
			logger.debug("The command was entered by a non-player; that's not what this is for."); 
		}
		
		return null;
	}
	
}
