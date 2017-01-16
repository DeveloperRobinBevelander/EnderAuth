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

/*
 * Every sql query in this plugin with a variable, by policy, uses prepared statements.
 * Should this policy be followed, there should be absolutely no risk for SQL Injections.
 * If you see this policy being broken somewhere in the code, please message me or one of the developers.
 */
import java.sql.*;
import java.util.ArrayList;

import org.spongepowered.api.util.Tuple;

import com.github.heartsemma.enderauth.DataStructures.ErrorStatus;
import com.google.common.base.Optional;

public class Database {
	
	//Final variables for interacting with other parts of the plugin
	private final Main main = Main.getInstance(); 
	
	//Constants about the database. Some of these may be pulled from configuration in the future. For now they are set here and unconfigurable.
    private final String dbName = "enderAuthDB"; //Name of the database.
    private final String dbLocation = "localhost"; //Databases basic url. Ex: 127.0.0.1, localhost, 72.13.37.240
    private final String dbUsername = "root";
    private final String dbPassword = "yourPassword";
    private final int port = 3306;
    private final String dbUrl = "jdbc:mysql://"+dbLocation+":"+String.valueOf(port)+"/mysql?zeroDateTimeBehavior=convertToNull";
    private final String totpUserTableName = "ea_totp_users"; //The table where we store the TOTP data for players, so we can generate their codes and authenticate them.
    private final String totpUserTableIDColumn = "username"; //Column 1 of the TOTP user table.
    private final String totpUserTableIDColumnType = "TEXT"; //Variable type of ID Column
    private final String totpUserTablePSKColumn = "pre_shared_key"; //Column 2 of the TOTP user table.
    private final String totpUserTablePSKColumnType = "TEXT"; //Variable type of the stored PSK for TOTP authentication
    //Sorry about the long variable names, but its better to be long than obscure.
    
    
    //Class-Wide SQL Variables for entering commands
	private Connection connection;
    
	//Loads an already existing database or creates one if one does not already exist and then loads it.
	public Database(){
		//Initiate Connection
		try {
			connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
		} catch (SQLException e) {
			main.getLogger().error("EnderAuth encountered an error attempting to connect to the database at " + dbUrl);
			main.getLogger().error("This is an unrecoverable error and EnderAuth will shut down.");
			main.majorFailure();
			e.printStackTrace();
			return;
		}
		
		//Create the database if it doesn't already exist.
		try {
			main.getLogger().debug("Attempting to create a database, if one does not exist yet.");
	       
	        String makeDatabaseCommand = "CREATE DATABASE IF NOT EXISTS ?";
	        PreparedStatement statement = connection.prepareStatement(makeDatabaseCommand);
	        statement.setString(1, dbName);
	        
	        main.getLogger().debug("Running prepared statement " + statement.toString());
	        statement.executeUpdate();
	        
	        //Closing earlier connections.
	        statement.close();
	        connection.commit();
	        
	        main.getLogger().debug("Previous command ran successfully and we will begin structuring the database (if necessary).");
	        
	    } catch (SQLException e) {
	    	main.getLogger().error("EnderAuth encountered an error while attempting to make a database (if one did not already exist).");
	    	main.getLogger().error("A stack trace is printing now.");
	    	main.getLogger().error("EnderAuth may still function, depending on what error was thrown and whether or not you already have a database to hold player data.");
	        e.printStackTrace();
	    }
		
		//Structures the database with the necessary tables and columns if they don't already exist yet.
		try {
			//Creates a table with two columns, one for the username, and one for the Pre-Shared Key for that username's TOTP authentication.
			main.getLogger().debug("Attempting to structure database in case it does not have the required tables yet.");
			
			String command = "CREATE TABLE IF NOT EXISTS ? ( "
						   + "? ?," //Username of the player; Used as the ID
						   + "? ?)"; //Pre-shared key we generate at registration
			
			PreparedStatement statement = connection.prepareStatement(command);
			statement.setString(1, totpUserTableName);
			statement.setString(2, totpUserTableIDColumn);
			statement.setString(3, totpUserTableIDColumnType);
			statement.setString(4, totpUserTablePSKColumn);
			statement.setString(5, totpUserTablePSKColumnType);
			
			//Full command should look something like: CREATE TABLE IF NOT EXISTS EA_TOTP_TABLE (USERNAME TEXT, PSK TEXT)
			
			main.getLogger().debug("Running command: " + statement.toString());
			
			statement.executeUpdate();
			
		} catch (SQLException e) {
			main.getLogger().error("EnderAuth encountered an error while attemptig to structure the database (if it was not already structured).");
			main.getLogger().error("A stack trace is printing now.");
	    	main.getLogger().error("EnderAuth may still function, depending on what error was thrown and whether or not you already have a database to hold player data.");
			e.printStackTrace();
		}
		
	}
	
	/*
	 * Public Database interaction methods
	 */
	
	//Adds an entry into the TOTP user database. Used when registering new users for the first time.
	public ErrorStatus addTotpUser(String username, String PSK){
		try {
			main.getLogger().debug("Attempting to create database entry for user "+username);
			
			String command = "INSERT INTO ? (?, ?)";
			PreparedStatement statement = connection.prepareStatement(command);
			statement.setString(1, totpUserTableName);
			statement.setString(2, username);
			statement.setString(3, PSK);
			
			main.getLogger().debug("Executing command " + statement.toString()+".");
			
			statement.executeQuery();
			return ErrorStatus.SUCCESS; //User successfully added to database with no hiccups, so ErrorStatus.SUCCESS	
			
		} catch (SQLException e) {
			
			main.getLogger().error("An error was generated while attempting to add user " + username + " to the EnderAuth database. Printing stack trace now.");
			e.printStackTrace();
			return ErrorStatus.UNKNOWN; //The user was unable to be added to the database, and we may have failed at our job.
		}
	}
	
	//Returns the PSK for the specified user in the database. If the function fails to retrieve it, it will return a blank string as that part of the TUPLE.
	public Tuple<String,ErrorStatus> getTotpKey(String username){
		try {
			String command = "SELECT ? FROM ? WHERE ? == ?";
			
			PreparedStatement statement = connection.prepareStatement(command);
			statement.setString(1, totpUserTablePSKColumn);
			statement.setString(2, totpUserTableName);
			statement.setString(3, username);
			statement.setString(4, totpUserTableIDColumn);
			
			main.getLogger().debug("Executing command "+statement.toString()+".");
			
			ResultSet selection = statement.executeQuery(); 	
			main.getLogger().debug("Database inquiry returned the ResultSet " + selection.toString());
			
			//There should be one String in this resultset, but we will check it good because EnderAuth is stronk, EnderAuth is reliable.
			
			//Will return false if the resultset has no rows, and is empty	
			if(!selection.isAfterLast()){ 	
				main.getLogger().error("EnderAuth attempted to retrieve " + username + "'s PSK from the database but only managed to find an empty box of data.");
				main.getLogger().error("Does " + username + " have an entry in the " + totpUserTableName + " table?");
			
				//Returns a blank string, and a ErrorStatus.CRITICAL_FAILURE message because we were unable to get the required data.
				return new Tuple<String,ErrorStatus>("",ErrorStatus.CRITICAL_FAILURE);
			}
			
			//Otherwise, some more checks
			main.getLogger().debug("Retrieving data...");
			selection.last();
			if(selection.getRow() > 2){ //If selection.getRow() is greater than 2 after iterating to the last row, there are multiple entries for the same username and that's bad.			
				main.getLogger().error("EnderAuth attempted to retrieve " + username + "'s PSK from the database but found multiple entries with the same username. ");
				main.getLogger().error("We're not sure what to do so we're retrieving the PSK in the last row there.");
				main.getLogger().error("You should urgently find out what caused two (or more) TOTP database entries to be assigned to the same player.");
				main.getLogger().error("This is probably the result of a glitch (in which you should contact us on the SpongeForums) but it may also be the result of malicious database tampering.");
				
				String PSK = selection.getString(totpUserTablePSKColumn);
				return new Tuple<String,ErrorStatus>(PSK, ErrorStatus.UNKNOWN); //This may have worked due to just raw probability, but don't count on it.			
			} else { 
				//If we get to this point, everything looks tight.	
				String PSK = selection.getString(totpUserTablePSKColumn);
				return new Tuple<String,ErrorStatus>(PSK, ErrorStatus.SUCCESS); 			
			}
			
			
		} catch (SQLException e) {
			main.getLogger().error("EnderAuth encountered an exception while trying to retrieve the TOTP PSK for " + username +". Printing stack trace now.");
			e.printStackTrace();
			return new Tuple<String,ErrorStatus>("",ErrorStatus.CRITICAL_FAILURE);
		}
	}
	
}
