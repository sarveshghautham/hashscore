package com.kheyos.service.analyze;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by sarvesh on 2/14/15.
 */
public class DbConnection {

    private static final String propertiesFile = "/mysql_user.properties";
    private static String username;
    private static String password;
    private static Connection connection = null;
    private static String ipAddress;
    private static String port;
    private static String dbName;

    private DbConnection() {

    }

    private static class SingletonHelper{
        private static final DbConnection INSTANCE = new DbConnection();
    }

    public static DbConnection getInstance(){

        if (connection == null) {
            makeConnection();
        }
        return SingletonHelper.INSTANCE;
    }

    private static void loadProperties() throws IOException {
    	
    	InputStream stream = HashScore.class.getResourceAsStream(propertiesFile);
    	BufferedReader br = null;
    	
        try {
        	
            br = new BufferedReader(new InputStreamReader(stream));
            
            ipAddress = br.readLine();
            port = br.readLine();
            dbName = br.readLine();
            username = br.readLine();
            password = br.readLine();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
       
        	if (br != null)
        		br.close();
        }
    }

    private static void makeConnection() {

        try {
        	loadProperties();
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://"+ipAddress+":"+port+"/"+dbName;
            connection = DriverManager
                    .getConnection(url, username, password);
            
            if (connection == null) {
                System.out.println("Connection failed");
            }
            
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: No driver found!");
            e.printStackTrace();
            return;
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public String getDbName() {
    	return dbName;
    }
    
    public Connection getConnection() {
    	return connection;
    }

    public void insertIntoDb(String query, String word, int count) {
    
    	
    }
    
    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}