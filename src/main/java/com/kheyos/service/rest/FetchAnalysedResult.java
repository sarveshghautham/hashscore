package com.kheyos.service.rest;

//import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kheyos.service.analyze.HashScore;
import com.kheyos.service.analyze.StartReading;


@Path("/rest")
public class FetchAnalysedResult {

    public HashScore hs = StartReading.hs;

	@GET
	public Response Result(@QueryParam("match") String matchTag) {

//		Cassandra client = new Cassandra();
//		client.connect("127.0.0.1");
//		matchTag = "#" + matchTag;
//		ArrayList<Integer> analysedValues = client.selectQuery(matchTag);
//		client.close();
//		String response = "Fours: "+analysedValues.get(0)+" Sixers: "+analysedValues.get(1)+" Wickets: "+analysedValues.get(2);
//		return Response.status(200).entity(response).build();
        
        //TODO: JSON serialization

        String response = hs.getTopKWords().toString();
        return Response.status(200).entity(response).build();

		
	}
}
