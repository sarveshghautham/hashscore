package com.kheyos.service.rest;

import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.kheyos.service.analyze.Cassandra;


@Path("/rest")
public class FetchAnalysedResult {

	@GET
	public Response Result(@QueryParam("match") String matchTag) {

		Cassandra client = new Cassandra();
		client.connect("127.0.0.1");
		matchTag = "#" + matchTag;
		ArrayList<Integer> analysedValues = client.selectQuery(matchTag);
		client.close();
		String response = "Fours: "+analysedValues.get(0)+" Sixers: "+analysedValues.get(1)+" Wickets: "+analysedValues.get(2);
		return Response.status(200).entity(response).build();
		
	}
}
