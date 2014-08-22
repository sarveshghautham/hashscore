package com.kheyos.service;

import java.util.ArrayList;
import java.util.Collections;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class Cassandra {
	private Cluster cluster;
	
	public void connect(String node) {
	   cluster = Cluster.builder()
	         .addContactPoint(node).build();
	   Metadata metadata = cluster.getMetadata();
	   System.out.printf("Connected to cluster: %s\n", 
	         metadata.getClusterName());
	   for ( Host host : metadata.getAllHosts() ) {
	      System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n",
	         host.getDatacenter(), host.getAddress(), host.getRack());
	   }	   
	}
	
	public void insertData (String query) {
		Session session = cluster.connect();
		session.execute(query);      
	}
	
	public void selectQuery () {
		Session session = cluster.connect();
		String query = "SELECT * FROM ks_hashscore.hashscore";
		ResultSet result = session.execute(query);
		ArrayList<Integer> temp = new ArrayList<Integer>();
		
		for (Row row: result) {
			temp.add(row.getInt("fours_count"));
			temp.add(row.getInt("sixers_count"));
			temp.add(row.getInt("wickets_count"));
			System.out.println(Collections.max(temp));
		}
	}
	
	public void close () {
		cluster.close();
	}
	
	public static void main(String[] args) {
	   Cassandra client = new Cassandra();
	   client.connect("127.0.0.1");
	   //client.insertData();
	   client.selectQuery();
	   client.close();
	   
	}
	
	
}
