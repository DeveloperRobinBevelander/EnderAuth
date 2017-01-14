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
 * Every sql query in this plugin with a variable in its execution, by policy, uses prepared statements.
 * Should this policy be followed, there should be absolutely no risk for SQL Injections.
 */
import java.sql.*;

public class Database {
	
	//Final variables for interacting with other parts of the plugin
	private final Main main = Main.getInstance(); 
	
	//Constants about the database. Some of these may be pulled from configuration in the future. For now they are set here and unconfigurable.
    private final String dbName = "enderAuthDB";
    private final String dbLocation = "localhost";
    private final String dbUsername = "root";
    private final String dbPassword = "yourPassword";
    private final int port = 3306;
    private final String dbUrl = "jdbc:mysql://"+dbLocation+":"+String.valueOf(port)+"/mysql?zeroDateTimeBehavior=convertToNull";
    private final String totpUserTableName = "eaTotpUsers";
    
    //Class-Wide SQL Variables for entering commands
	private Connection connection;
    
	//Loads an already existing database or creates one if one does not already exist and then loads it.
	public Database(){
		try {
			connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
		} catch (SQLException e) {
			main.getLogger().error("EnderAuth encountered an error attempting to connect to the database at " + dbUrl);
			main.getLogger().error("This is an unrecoverable error and EnderAuth will shut down.");
			e.printStackTrace();
			main.majorFailure();
			return;
		}
		
		makeDatabase(); //Create the database if it doesn't already exist.
		structureDatabase(); //Structures the database with the necessary tables and columns if they don't already exist yet.
		
	}

	private void makeDatabase(){
		
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
	}
	
	private void structureDatabase() {
		try {
			//Creates a table with two columns, one for the username, and one for the Pre-Shared Key for that username's TOTP authentication.
			main.getLogger().debug("Attempting to structure database in case it does not have the required tables yet.");
			
			String command = "CREATE TABLE IF NOT EXISTS ? ( "
								+ "Username TEXT," //Username of the player; Used as the ID
								+ "PSK TEXT )"; //Pre-shared key we generate at registration
			
			PreparedStatement statement = connection.prepareStatement(command);
			statement.setString(1, totpUserTableName);
			
			main.getLogger().debug("Running command: " + command);
			
			statement.executeUpdate();
			
		} catch (SQLException e) {
			main.getLogger().error("EnderAuth encountered an error while attemptig to structure the database (if it was not already structured).");
			main.getLogger().error("A stack trace is printing now.");
	    	main.getLogger().error("EnderAuth may still function, depending on what error was thrown and whether or not you already have a database to hold player data.");
			e.printStackTrace();
		}
	}
}
