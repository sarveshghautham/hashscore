package com.kheyos.service.analyze;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by sarvesh on 2/14/15.
 */
public class DbConnection {

    private static final String propertiesFile = "src/main/resources/mysql_user.properties";
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

    private static void loadProperties() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(propertiesFile));
            ipAddress = br.readLine();
            port = br.readLine();
            dbName = br.readLine();
            username = br.readLine();
            password = br.readLine();
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void makeConnection() {

        loadProperties();

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: No driver found!");
            e.printStackTrace();
            return;
        }

        String url = "jdbc:mysql://"+ipAddress+":"+port+"/"+dbName;

        try {
            connection = DriverManager
                    .getConnection(url, username, password);

        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;
        }

        if (connection == null) {
            System.out.println("Connection failed");
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}