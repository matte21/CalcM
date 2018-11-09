package query.server.handlers;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.json.JSONArray;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import query.server.utils.query.PrefixedQueryUtilsFactory;
import query.server.utils.query.PrefixedStudyroomQueryBuilder;
import query.server.utils.query.StudyroomQueryResultsParser;
import sofia_kp.KPICore;
import sofia_kp.SIBResponse;
import sofia_kp.SSAP_sparql_response;

public class FiltersHandler extends SIBFacingHandler {
	
	private PrefixedStudyroomQueryBuilder queryBuilder;
	
	// Static fields used for logging
	private final static Logger LOG = LogManager.getLogger();
	private final static Marker SPARQL = MarkerManager.getMarker("SPARQL");
	private final static Marker QUERY_RESULTS = MarkerManager.getMarker("QUERY_RESULTS");

	// Keys to retrieve Session objects
	private final static String SIB_CONNECTION_KEY = "sibConn";
	private final static String QUERY_UTILS_KEY = "queryUtilsFactory";
	
	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession request) {
		LOG.info("Received " 
				+ request.getMethod() 
				+ " request from " 
				+ request.getRemoteIpAddress() 
				+ " with parameters " 
				+ (request.getQueryParameterString().trim().isEmpty() ? "<no parameters>"
						: request.getQueryParameterString()));
		
		LOG.debug("Initializing a SPARQL query builder.");
		queryBuilder = getQueryBuilder(request);
		LOG.debug("SPARQL query builder initialized.");
		
		LOG.debug("Building the SPARQL query.");
		String sparqlQuery = buildSPARQLQueryFromParams(request.getQueryParameterString());
		LOG.debug(SPARQL, "SPARQL Query:\n" + sparqlQuery);
		
		LOG.debug("Acquiring connection to the SIB.");		
		KPICore sibConnection = getSibConnection(request);
		LOG.debug("Connection to the SIB acquired.");
		
		LOG.debug("Sending the query to the SIB.");		
		SIBResponse sibResp = sibConnection.querySPARQL(sparqlQuery);		
		if (sibResp == null || !sibResp.isConfirmed()) {
			LOG.error("Failed to acquire query results from the SIB. Sending internal error response.");
			return buildFailedToQuerySIBHTTPResp();
		}
		LOG.debug(QUERY_RESULTS, "Received query results from the SIB:\n" 
				+ sibResp.sparqlquery_results.print_as_string());	
		
		LOG.debug("Building the Query results parser.");
		StudyroomQueryResultsParser resultsParser = getQueryResultsParser(request, sibResp.sparqlquery_results); 
		LOG.debug("Built the Query results parser.");
		
		LOG.debug("Building response body.");
		String responseBodyAsJSON = buildResponseBodyAsJSON(resultsParser);
		LOG.debug("Response body built.");
	
		Response httpResponse = buildSuccessfulHTTPResp(responseBodyAsJSON);
		LOG.info("Successfully built response to " + request.getMethod() + " from " + request.getRemoteIpAddress());
		
		return httpResponse;
	}

	private PrefixedStudyroomQueryBuilder getQueryBuilder(IHTTPSession request) {
		return ((PrefixedQueryUtilsFactory) SIBFacingHandler.getSession(request)
															.get(QUERY_UTILS_KEY))
															.getPrefixedQueryBuilder();
	}
	
	private String buildSPARQLQueryFromParams(String httpQueryParamsString) {
		 queryBuilder = queryBuilder.selectRoomID()
						   			.selectAvailSeats()
						   			.selectCapacity()
						   			.selectState()
						   			.selectUniversity()
						   			.selectAddress()
						   			.selectNextTransitionInstantAndPredicate();
		 
		if (httpQueryParamsString != null && !httpQueryParamsString.trim().isEmpty()) {
			String[] queryParams = httpQueryParamsString.split("&");
			for (String param : queryParams) {
				String[] paramNameAndValue = param.split("=");
				String paramName = paramNameAndValue[0];
				String paramValue = paramNameAndValue[1];
				switch (paramName) {
					case "availSeats":
						queryBuilder = queryBuilder.addWhereFilterOnAvailSeats(Integer.parseInt(paramValue));
						break;
					case "roomState":
						queryBuilder = queryBuilder.addWhereFilterOnRoomState(paramValue);				
						break;
					case "feat":
						queryBuilder = queryBuilder.addWhereFilterOnFeature(paramValue);				
						break;
				}
			}
		}
		
		return queryBuilder.getQuery();
	}
	
	private KPICore getSibConnection(IHTTPSession request) {
		return (KPICore) SIBFacingHandler.getSession(request).get(SIB_CONNECTION_KEY);
	}
	
	private Response buildFailedToQuerySIBHTTPResp() {
		String errorMsg = "failed to acquire study rooms data from the SIB.";
		return buildInternalErrorHTTPResp(errorMsg);
	}
	
	private StudyroomQueryResultsParser getQueryResultsParser(IHTTPSession request, SSAP_sparql_response results) {
		return ((PrefixedQueryUtilsFactory) SIBFacingHandler.getSession(request)
															.get(QUERY_UTILS_KEY))
															.newPrefixedQueryResultsExtractor(results);
	}
	
	private String buildResponseBodyAsJSON(StudyroomQueryResultsParser resultsParser) {
		JSONArray resultsAsJSONArray = new JSONArray(resultsParser.getResults());
		resultsParser.resetFilters();
		return resultsAsJSONArray.toString();
	}
	
	private Response buildSuccessfulHTTPResp(String responseBody) {
		Response httpResponse = NanoHTTPD.newFixedLengthResponse(responseBody);		
		httpResponse.setStatus(Status.OK);
		return httpResponse;
	}
	
	private Response buildInternalErrorHTTPResp(String message) {
		Response httpResponse;
		httpResponse = NanoHTTPD.newFixedLengthResponse("Server internal error: " + message);
		httpResponse.setStatus(Status.INTERNAL_ERROR);		
		return httpResponse;
	}
}
