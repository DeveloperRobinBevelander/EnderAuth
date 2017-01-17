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

import org.spongepowered.api.util.Tuple;

import com.github.heartsemma.enderauth.DataStructures.DatabaseException;
import com.github.heartsemma.enderauth.DataStructures.DatabaseExceptions.UUIDNotFoundException;

public class Database {
	
	//Final variables for interacting with other parts of the plugin
	private final Main main = Main.getInstance(); 
	
	//Constants about the database. Some of these may be pulled from configuration in the future. For now they are set here and unconfigurable.
    private static final String dbName = "enderAuthDB"; //Name of the database.
    private static final String dbLocation = "localhost"; //Databases basic url. Ex: 127.0.0.1, localhost, 72.13.37.240
    private static final String dbUsername = "root";
    private static final String dbPassword = "yourPassword";
    private static final int port = 3306;
    private static final String dbUrl = "jdbc:mysql://"+dbLocation+":"+String.valueOf(port)+"/mysql?zeroDateTimeBehavior=convertToNull";
    
    //For the User Table.
    private static final String userTableName = "ea_users"; //The table where we store the data for players, so we can generate their codes and authenticate them, etc.
    private static final String userTableIDColumn = "uuid"; //Name of the ID column in the user table (Also what it contains)
    private static final int userTableIDColumnIndex = 1; //Index of the PSK column in the user table 
    private static final String userTableIDColumnType = "TEXT NOT NULL UNIQUE"; //Variable type/parameters of ID Column
    private static final String userTableTotpPSKColumn = "pre_shared_key"; //Name of the PSK column in the totp user table
    private static final int userTableTotpPSKColumnIndex = 2; //Index of the PSK column in the totp user table 
    private static final String userTableTotpPSKColumnType = "TEXT"; //Variable type/parameters of the stored PSK for TOTP authentication
    
    //Sorry about the long variable names, but its better to be long than obscure.
    
    
    //Class-Wide SQL Variables for entering commands
	private Connection connection;
	
	//Loads an already existing database or creates one if one does not already exist and then loads it.
	public Database() throws SQLException{
		
		connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
		
		//Create the database if it doesn't already exist.
		main.getLogger().debug("Attempting to create a database, if one does not exist yet.");
	       
	    String makeDatabaseCommand = "CREATE DATABASE IF NOT EXISTS ?";
	    PreparedStatement statement = connection.prepareStatement(makeDatabaseCommand);
	    statement.setString(1, dbName);
	        
	    main.getLogger().debug("Running prepared statement " + statement.toString());
	    statement.executeUpdate();
	        
	    
	    statement.close();
	    connection.commit();
	        
	    main.getLogger().debug("Previous command ran successfully and we will begin structuring the database (if necessary).");
	    
		main.getLogger().debug("Attempting to structure database in case it does not have the required tables yet.");
			
		String command = "CREATE TABLE IF NOT EXISTS ? ( "
					   + "? ? NOT NULL UNIQUE," //UUID of the player; Used as the ID
					   + "? ?)"; //Pre-shared key we generate at registration for TOTP Authentication
			
		PreparedStatement tableCreationStatement = connection.prepareStatement(command);
		tableCreationStatement.setString(1, userTableName);
		tableCreationStatement.setString(2, userTableIDColumn);
		tableCreationStatement.setString(3, userTableIDColumnType);
		tableCreationStatement.setString(4, userTableTotpPSKColumn);
		tableCreationStatement.setString(5, userTableTotpPSKColumnType);
		
		//Full command should look something like: CREATE TABLE IF NOT EXISTS EA_TOTP_TABLE (USERNAME TEXT, PSK TEXT)
		
		main.getLogger().debug("Running command: " + tableCreationStatement.toString());
		
		tableCreationStatement.executeUpdate();
		
		tableCreationStatement.close();
		connection.commit();
		
	}
	
	/*
	 * Public Database interaction methods
	 */
	/**
	 * @param uuid (Universally Unique Identifier)
	 * 
	 * <br><br>This function adds a user into the database with the following parameters as parts of its entry.
	 * 
	 * @throws SQLException This function accesses the database via an "INSERT INTO" query.
	 * */
	public void addUser(String uuid) throws SQLException{
		main.getLogger().debug("Attempting to create database entry for user "+uuid);
		
		String command = "INSERT INTO ? (?, NULL)";
		PreparedStatement statement = connection.prepareStatement(command);
		statement.setString(1, userTableName);
		statement.setString(2, uuid);
		
		main.getLogger().debug("Executing command " + statement.toString()+".");
		
		statement.executeQuery();
	}
	
	/**
	 * @param uuid (Universally Unique Identifier)
	 * @param PSK (Pre-Shared Key for use in generating authentication codes)
	 * 
	 * <br><br>This function adds a user into the database with the following parameters as parts of its entry.s 
	 * 
	 * @throws SQLException This function accesses the database via an "INSERT INTO" query.
	 * */
	public void addUser(String uuid, String PSK) throws SQLException{
		main.getLogger().debug("Attempting to create database entry for user "+uuid);
		
		String command = "INSERT INTO ? (?, ?)";
		PreparedStatement addUser = connection.prepareStatement(command);
		addUser.setString(1, userTableName);
		addUser.setString(2, uuid);
		addUser.setString(3, PSK);
	
		main.getLogger().debug("Executing command " + addUser.toString()+".");
		
		addUser.executeQuery();
		
		addUser.close();
	}
	
	/**
	 * @param uuid (Universally Unique Identifier)
	 * @return The Pre-Shared Key of the user for their TOTP authentication.
	 * 
	 * <br><br>Returns the TOTP Pre-Shared Key connected with the specified UUID.
	 * <br>Returns null if unable to retrieve the key (if not found or  
	 * 
	 * @throws SQLException This function accesses the database via a "SELECT" query.
	 * @throws DatabaseException Thrown if the returned ResultSet contains missing or what should be erroneous data.
	 */
	public String getTotpKey(String uuid) throws SQLException, DatabaseException{
		main.getLogger().debug("Attempting to retrieve TOTP PSK for user " + uuid + ".");
		
		String command = "SELECT ? FROM ? WHERE ? == ?";
		
		PreparedStatement selectTotpKey = connection.prepareStatement(command);
		selectTotpKey.setString(1, userTableTotpPSKColumn);
		selectTotpKey.setString(2, userTableName);
		selectTotpKey.setString(3, uuid);
		selectTotpKey.setString(4, userTableIDColumn);
		
		main.getLogger().debug("Executing command "+selectTotpKey.toString()+".");
		
		ResultSet selection = selectTotpKey.executeQuery(); 	
		main.getLogger().debug("Database inquiry returned the ResultSet: " + selection.toString() + ".");
		
		//There should be one String in this resultset, but we will check it good because EnderAuth is stronk, EnderAuth is reliable.
		main.getLogger().debug("Retrieving data...");
		
		if(!selection.isBeforeFirst()){ //Triggers when there are no rows in the ResultSet.
			main.getLogger().error("EnderAuth attempted to retrieve " + uuid + "'s PSK from the database but was unable to find it.");
			main.getLogger().error("There were no rows in the returned table of data after running the MySql 'PreparedStatement' " + selectTotpKey.toString() + ".");
			
			throw new DatabaseException("getTotpKey()'s PreparedStatement returned a ResultSet that had no data.");
		}
		
		selection.last();
		
		if(selection.getRow() == 1){ //There was no entry for this user.
			main.getLogger().error("EnderAuth attempted to retrieve " + uuid + "'s PSK from the database but was unable to find it.");
			main.getLogger().error("Does " + uuid + " have an entry in the " + userTableName + " table?");
		
			//Returns null because we were unable to get the required data.
			throw new UUIDNotFoundException("getTotpKey() was unable to find the entry in the database with the specified UUID.");
			
		} else if(selection.getRow()==2){ //There was one entry for this user
			//If we get to this point, everything looks tight.	
			main.getLogger().debug("Successfully retrieved PSK from user " + uuid + ".");
			
			String PSK = selection.getString(userTableTotpPSKColumnIndex);
			return PSK; 	
			
		} else { //There was more than one entry for this user.
			main.getLogger().error("EnderAuth searched for " + uuid + "'s PSK and found multiple entries for that user in the database.");
			main.getLogger().error("This should not have happened and indicates either plugin glitches or malcious database tampering.");
			
			throw new DatabaseException("Multiple entries matching the specified UUID were found in the database.");
		}
	}
	
	/**@param uuid (Universally Unique Identifier)
	 * @return A boolean that tells
	 * 
	 * <br><Br>Returns true if there is an entry in the User Table with a uuid matching the parameter. 
	 * <br>Returns false if there is not.
	 * 
	 * @throws SQLException The function uses PreparedStatements to ask about the presence of the UUID in the User Table.
	 * @throws DatabaseException If the returned ResultSet from the SELECT command is completely empty (that is, lacking even the names of the columns in the table), the function throws a DatabaseException.*/
	public boolean isInDatabase(String uuid) throws SQLException, DatabaseException {
		main.getLogger().debug("Attempting to determine presence of user " + uuid + " in the database.");
			
		String command = "SELECT * FROM ? WHERE ? = ?";
		PreparedStatement statement = connection.prepareStatement(command);
		
		statement.setString(1, userTableName);
		statement.setString(2, userTableIDColumn);
		statement.setString(3, uuid);

		//Command should look something like: SELECT * FROM ea_users WHERE id == uuid
		main.getLogger().debug("Executing command "+statement.toString()+".");
		
		ResultSet selection = statement.executeQuery();
		main.getLogger().debug("Database inquiry returned the ResultSet: " + selection.toString() + ".");
		
		//ResultSet Analysis + Error Checking
		if(selection.isBeforeFirst()){ //Triggers when there are no rows in the ResultSet.
			main.getLogger().error("EnderAuth attempted to find if there was a uuid matching " + uuid + " in the database but was unable to run the necessary SQL queries.");
			main.getLogger().error("There were no rows in the returned table of data after running the MySql 'PreparedStatement' " + statement.toString() + ".");
			throw new DatabaseException("Returned ResultSet in isPresent(String uuid) contained no rows.");
		}
		
		selection.last();
		
		if(selection.getRow() == 1){ //There are no entries in the ResultSet.
			main.getLogger().debug("No entries for user " + uuid + " were found in the ResultSet.");
			return false;
			
		} else { //There is one or more entries with the matching uuid.
			main.getLogger().debug("User " + uuid + " was found in the database.");
			return true;
		} 
	}
}
