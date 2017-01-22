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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.github.heartsemma.enderauth.DataStructures.DatabaseException;
import com.github.heartsemma.enderauth.DataStructures.DatabaseExceptions.UUIDNotFoundException;
import com.google.common.base.Preconditions;

public class Database {
	
	//Final variables for interacting with other parts of the plugin
	private final Main main = Main.getInstance(); 
	private final Logger logger = main.getLogger();
    
    
    //Class-Wide SQL Variables for entering commands
	private Connection connection;
	private boolean databaseInitialized; //
	
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
    private static final String userTableIDColumnType = "TINYBLOB NOT NULL UNIQUE"; //Variable type/parameters of ID Column
    private static final String userTableTotpPSKColumn = "pre_shared_key"; //Name of the PSK column in the totp user table
    private static final int userTableTotpPSKColumnIndex = 2; //Index of the PSK column in the totp user table 
    private static final String userTableTotpPSKColumnType = "TEXT"; //Variable type/parameters of the stored PSK for TOTP authentication
    
    //Sorry about the long variable names, but its better to be long than obscure.
	
	private static Database INSTANCE = null;
	
	public static Database getInstance(){
		if(INSTANCE==null){
			INSTANCE = new Database();
		}
		return INSTANCE;
	}
	
	private Database(){}
	
	
	//Loads an already existing database or creates and structures one if it doesnt already exist.
	public void validate() throws SQLException{
		//This should only be called once, and we want to make sure it doesn't happen again.
		if(databaseInitialized){
			logger.debug("The Database was already initialized.");
			logger.debug("This initialization process will be skipped to prevent it from happening a second time...");
		} else {
			connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
			
			logger.debug("Attempting to create a database, if one does not exist yet.");
		       
		    String makeDatabaseCommand = "CREATE DATABASE IF NOT EXISTS ?";
		    
		    ArrayList<Object> makeDatabaseVariables = new ArrayList<Object>();
		    makeDatabaseVariables.add(dbName);
		    
		    transact(makeDatabaseCommand, makeDatabaseVariables);
		    
		    logger.debug("Previous command ran successfully and we will begin structuring the database (if necessary).");
			logger.debug("Attempting to structure database in case it does not have the required tables yet.");
				
			String tableCreationCommand = "CREATE TABLE IF NOT EXISTS ? ( "
						   + "? ?," //UUID of the player; Used as the ID
						   + "? ?)"; //Pre-shared key we generate at registration for TOTP Authentication
			
			ArrayList<Object> tableCreationVariables = new ArrayList<Object>();
			tableCreationVariables.add(userTableName);
			tableCreationVariables.add(userTableIDColumn);
			tableCreationVariables.add(userTableIDColumnType);
			tableCreationVariables.add(userTableTotpPSKColumn);
			tableCreationVariables.add(userTableTotpPSKColumnType);
			
			transact(tableCreationCommand,tableCreationVariables);
			
			//Full command should look something like: CREATE TABLE IF NOT EXISTS EA_TOTP_TABLE (USERNAME TEXT, PSK TEXT)
			
			logger.debug("Running command: " + tableCreationCommand.toString());
			
			databaseInitialized=true;
		}
	}
	
	/**
	 * @param sql (SQL Statement to be executed)
	 * @param variables (Variables within the SQL statement that will be securely injected into the sql statement. May be empty.)
	 * @return A ResultSet containing the result of the MySQL query entered. Returns null if the MySQL is not a select query.
	 * 
	 * <br><br> This command runs the entered MySQL query 'sql', and substitutes the question marks with the variables in the array.
	 * It then returns an arraylist that represents the results of that MySQL query.
	 */
	private ResultSet transact(String sql, ArrayList<Object> variables) throws SQLException{
		
		if(!databaseInitialized){
			validate();
		}
		
		logger.debug("transact() sql statement execution method called.");
		logger.debug("Checking for null parameters...");
		Preconditions.checkNotNull(sql);
		
		ResultSet returnedResultSet = null;
		
		if(variables==null || variables.size()==0){
			logger.debug("Executing command " + sql + ".");
			
			try(Statement statement = connection.createStatement()){
				statement.execute(sql);
				returnedResultSet = statement.getResultSet();
				statement.close();
			}
			
		} else {
			logger.debug("Checking if amount of variables equals");
			Preconditions.checkArgument(variables.size()==StringUtils.countMatches(sql, "?"));
			
			try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
				for(int i=1; i<=variables.size(); i++){
					preparedStatement.setObject(i, variables.get(i));
				}
				logger.debug("Executing command " + preparedStatement.toString() + ".");
				preparedStatement.execute();
				returnedResultSet = preparedStatement.getResultSet();
				preparedStatement.close();
			}
		}
		
		connection.commit();
		
		return returnedResultSet;
		
	}
	

	/**
	 * @param uuid (Universally Unique Identifier)
	 * 
	 * <br><br>This function adds a user into the database with the following parameters as parts of its entry.
	 * 
	 * @throws SQLException This function accesses the database via an "INSERT INTO" query.
	 * */
	public void addUser(byte[] uuid) throws SQLException{
		logger.debug("Attempting to create database entry for user " + new String(uuid));
		
		String addUserCommand = "INSERT INTO ? (?, NULL)";
		ArrayList<Object> addUserVariables = new ArrayList<Object>();
		addUserVariables.add(userTableName);
		addUserVariables.add(uuid);
		
		transact(addUserCommand,addUserVariables);
	}
	
	/**
	 * @param uuid (Universally Unique Identifier)
	 * @param PSK (Pre-Shared Key for use in generating authentication codes)
	 * 
	 * <br><br>This function adds a user into the database with the following parameters as parts of its entry.s 
	 * 
	 * @throws SQLException This function accesses the database via an "INSERT INTO" query.
	 * */
	public void addUser(byte[] uuid, String PSK) throws SQLException{
		logger.debug("Attempting to create database entry for user "+uuid);
		
		String addUserCommand = "INSERT INTO ? (?, ?)";
		ArrayList<Object> addUserVariables = new ArrayList<Object>();
		addUserVariables.add(userTableName);
		addUserVariables.add(uuid);
		addUserVariables.add(PSK);
	
		transact(addUserCommand,addUserVariables);
		
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
	public String getTotpKey(byte[] uuid) throws SQLException, DatabaseException{
		logger.debug("Attempting to retrieve TOTP PSK for user " + new String(uuid) + ".");
		
		String getKeyCommand = "SELECT ? FROM ? WHERE ? == ?";
		
		ArrayList<Object> getKeyVariables = new ArrayList<Object>();
		getKeyVariables.add(userTableTotpPSKColumn);
		getKeyVariables.add(userTableName);
		getKeyVariables.add(userTableIDColumn);
		getKeyVariables.add(uuid);
		
		ResultSet selection = transact(getKeyCommand,getKeyVariables); 	
		
		logger.debug("Database inquiry returned the ResultSet: " + selection.toString() + ".");
		
		//There should be one String in this resultset, but we will check it good because EnderAuth is stronk, EnderAuth is reliable.
		logger.debug("Error checking...");
		Preconditions.checkNotNull(selection);
		
		if(!selection.isBeforeFirst()){ //Triggers when there are no rows in the ResultSet.
			logger.error("EnderAuth attempted to retrieve " + new String(uuid) + "'s PSK from the database but was unable to find it.");
			logger.error("There were no rows in the returned table of data after running the MySql 'PreparedStatement' .");
			
			throw new DatabaseException("getTotpKey()'s PreparedStatement returned a ResultSet that had no data.");
		}
		
		selection.last();
		
		if(selection.getRow() == 1){ //There was no entry for this user.
			logger.error("EnderAuth attempted to retrieve " + new String(uuid) + "'s PSK from the database but was unable to find it.");
			logger.error("Does " + new String(uuid) + " have an entry in the " + userTableName + " table?");
		
			//Returns null because we were unable to get the required data.
			throw new UUIDNotFoundException("getTotpKey() was unable to find the entry in the database with the specified UUID.");
			
		} else if(selection.getRow()==2){ //There was one entry for this user
			//If we get to this point, everything looks tight.	
			logger.debug("Successfully retrieved PSK from user " + new String(uuid) + ".");
			
			String PSK = selection.getString(userTableTotpPSKColumnIndex);
			return PSK; 	
			
		} else { //There was more than one entry for this user.
			logger.error("EnderAuth searched for " + new String(uuid) + "'s PSK and found multiple entries for that user in the database.");
			logger.error("This should not have happened and indicates either plugin glitches or malcious database tampering.");
			
			throw new DatabaseException("Multiple entries matching the specified UUID were found in the database.");
		}
	}
	
	/**@param uuid (Universally Unique Identifier)
	 * @return A boolean
	 * 
	 * <br><Br>Returns true if there is an entry in the User Table with a uuid matching the parameter. 
	 * <br>Returns false if there is not.
	 * 
	 * @throws SQLException The function uses PreparedStatements to ask about the presence of the UUID in the User Table.
	 * @throws DatabaseException If the returned ResultSet from the SELECT command is completely empty (that is, lacking even the names of the columns in the table), the function throws a DatabaseException.*/
	public boolean isInDatabase(byte[] uuid) throws SQLException, DatabaseException {
		logger.debug("Attempting to determine presence of user " + new String(uuid) + " in the database.");
			
		String isInDatabaseCommand = "SELECT * FROM ? WHERE ? = ?";
		
		ArrayList<Object> isInDatabaseVariables = new ArrayList<Object>();
		
		isInDatabaseVariables.add(1, userTableName);
		isInDatabaseVariables.add(2, userTableIDColumn);
		isInDatabaseVariables.add(3, uuid);

		//Command should look something like: SELECT * FROM ea_users WHERE id == uuid
		
		ResultSet selection = transact(isInDatabaseCommand, isInDatabaseVariables);
		
		logger.debug("Database inquiry returned the ResultSet: " + selection.toString() + ".");
		
		logger.debug("Error Checking...");
		Preconditions.checkNotNull(selection);
		
		//ResultSet Analysis + Error Checking
		if(selection.isBeforeFirst()){ //Triggers when there are no rows in the ResultSet.
			logger.error("EnderAuth attempted to find if there was a uuid matching " +
									new String(uuid) + " in the database but was unable to run the necessary SQL queries.");
			logger.error("There were no rows in the returned table of data after running the MySql 'PreparedStatement'.");
			throw new DatabaseException("Returned ResultSet in isPresent(String uuid) contained no rows.");
		}
		
		selection.last();
		
		if(selection.getRow() == 1){ //There are no entries in the ResultSet.
			logger.debug("No entries for user " + new String(uuid) + " were found in the ResultSet.");
			return false;
			
		} else { //There is one or more entries with the matching uuid.
			logger.debug("User " + new String(uuid) + " was found in the database.");
			return true;
		} 
		
	}
}
