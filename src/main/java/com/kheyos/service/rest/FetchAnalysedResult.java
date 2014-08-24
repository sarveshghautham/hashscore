package com.kheyos.service.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.kheyos.service.Cassandra;


@Path("/rest")
public class FetchAnalysedResult {

	@GET
	public Response Result(@QueryParam("match") String matchTag) {

		Cassandra client = new Cassandra();
		client.connect("127.0.0.1");
		matchTag = "#" + matchTag;
		int maxValue = client.selectQuery(matchTag);
		client.close();
		String response = Integer.toString(maxValue);
		return Response.status(200).entity(response).build();
		
	}
}
