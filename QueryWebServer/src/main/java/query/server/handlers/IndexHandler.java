package query.server.handlers;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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

// TODO handle displaying of open/close date and times

public class IndexHandler extends SIBFacingHandler {
	
	private PrefixedStudyroomQueryBuilder queryBuilder;
	
	// FIXME This is terrible and should be changed. The leading queryWebServer/ is needed because this
	// will run in a Docker container and thus the current directory for the process container is 
	// .. wrt to this project directory. But this makes the source code depend on the dir structure of
	// its execution environment, which might change in the future. It should be the opposite: the src code
	// should make an assumption on how the files will be organized in its execution environment, and the
	// build/deploy scripts should make those assumptions true. Currently the opposite is happening. 
	private final static String HTML_TEMPLATE_PATH = "../QueryWebServer/src/main/resources/indexTemplate.html";
	private final static String OPEN_ROOMS_DISPLAYER_ID = "open_rooms_table";
	private final static String CLOSED_ROOMS_DISPLAYER_ID = "closed_rooms_table";
	
	private final static String ROOM_NAME_PLACEHOLDER = "room-name";
	private final static String AVAIL_SEATS_PLACEHOLDER = "avail-seats";
	private final static String ADDRESS_PLACEHOLDER = "address";
	private final static String CAPACITY_PLACEHOLDER = "capacity";
	private final static String UNIVERSITY_PLACEHOLDER = "university";
	private final static String NEXT_TRANSITION_PLACEHOLDER = "next-trans";	
	// TODO handle displaying of open/close date and times
	private final static String TABLE_ROW_TEMPLATE = "<tr><td>" + ROOM_NAME_PLACEHOLDER 
																+ "</td><td>" 
																+ AVAIL_SEATS_PLACEHOLDER 
																+ "</td><td>" 
																+ CAPACITY_PLACEHOLDER
																+ "</td><td>" 
																+ ADDRESS_PLACEHOLDER 
																+ "</td><td>" 
																+ NEXT_TRANSITION_PLACEHOLDER
																+ "</td><td>"
																+ UNIVERSITY_PLACEHOLDER 
																+ "</td></tr>";
	
	// Keys to retrieve Session objects
	private final static String SIB_CONNECTION_KEY = "sibConn";
	private final static String QUERY_UTILS_KEY = "queryUtilsFactory";
	
	private final static Logger LOG = LogManager.getLogger();
	private final static Marker SPARQL = MarkerManager.getMarker("SPARQL");
	private final static Marker QUERY_RESULTS = MarkerManager.getMarker("QUERY_RESULTS");

	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession request) {
		LOG.info("Received " + request.getMethod() + " request from " + request.getRemoteIpAddress());

		LOG.debug("Initializing a SPARQL query builder.");
		queryBuilder = getQueryBuilder(request);
		LOG.debug("SPARQL query builder initialized.");
		
		LOG.debug("Building the SPARQL query.");
		String sparqlQuery = buildSPARQLQuery();
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
		
		LOG.debug("Loading HTML page template to use for the response.");			
		Document htmlResponseDoc = null; 
		try {
			htmlResponseDoc = loadHTMLResponseTemplate();
		} catch (IOException e) {
			LOG.error("Could not load HTML page template for response: " + e.getStackTrace());
			return buildFailedToBuildHTMLRespHTTPResp();
		}
		LOG.debug("Loaded HTML page template to use for the response.");

		LOG.debug("Populating HTML template with query results.");
		addOpenRoomsToHTMLDoc(htmlResponseDoc, resultsParser.onlyOpenRooms().getResults());
		resultsParser.resetFilters();
		addClosedRoomsToHTMLDoc(htmlResponseDoc, resultsParser.onlyClosedRooms().getResults());		
		resultsParser.resetFilters();
		LOG.debug("Finished Populating HTML template with query results.");
		
		Response httpResponse = buildSuccessfulHTTPResp(htmlResponseDoc);
		LOG.info("Successfully built response to " + request.getMethod() + " from " + request.getRemoteIpAddress());
		
		return httpResponse;
	}

	private Response buildSuccessfulHTTPResp(Document htmlResponseDoc) {
		Response httpResponse = NanoHTTPD.newFixedLengthResponse(htmlResponseDoc.outerHtml());		
		httpResponse.setStatus(Status.OK);
		httpResponse.setMimeType(NanoHTTPD.MIME_HTML);
		return httpResponse;
	}
	
	private PrefixedStudyroomQueryBuilder getQueryBuilder(IHTTPSession request) {
		return ((PrefixedQueryUtilsFactory) SIBFacingHandler.getSession(request)
															.get(QUERY_UTILS_KEY))
															.getPrefixedQueryBuilder();
	}

	private String buildSPARQLQuery() {
		return queryBuilder.selectRoomID()
						   .selectAvailSeats()
						   .selectCapacity()
						   .selectState()
						   .selectUniversity()
						   .selectAddress()
						   .selectNextTransitionInstantAndPredicate()
						   .getQuery();
	}
	
	private KPICore getSibConnection(IHTTPSession request) {
		return (KPICore) SIBFacingHandler.getSession(request).get(SIB_CONNECTION_KEY);
	}

	private StudyroomQueryResultsParser getQueryResultsParser(IHTTPSession request, SSAP_sparql_response results) {
		return ((PrefixedQueryUtilsFactory) SIBFacingHandler.getSession(request)
															.get(QUERY_UTILS_KEY))
															.newPrefixedQueryResultsExtractor(results);
	}
	
	private Response buildFailedToQuerySIBHTTPResp() {
		String errorMsg = "failed to acquire study rooms data from the SIB.";
		return buildInternalErrorHTTPResp(errorMsg);
	}
	
	private Document loadHTMLResponseTemplate() throws IOException {
		File htmlResponseFile = new File(HTML_TEMPLATE_PATH);
		return Jsoup.parse(htmlResponseFile, "UTF-8", "");
	}
	
	private Response buildFailedToBuildHTMLRespHTTPResp() {
		String errorMsg = "failed to build HTML page to use for the response.";
		return buildInternalErrorHTTPResp(errorMsg);
	}

	private Response buildInternalErrorHTTPResp(String message) {
		Response httpResponse;
		httpResponse = NanoHTTPD.newFixedLengthResponse("Server internal error: " + message);
		httpResponse.setStatus(Status.INTERNAL_ERROR);		
		return httpResponse;
	}	

	private void addOpenRoomsToHTMLDoc(Document htmlResponseDoc, Set<Map<String, String>> openRooms) {
		addResultsToHTMLDoc(htmlResponseDoc, OPEN_ROOMS_DISPLAYER_ID, openRooms);
	}

	private void addClosedRoomsToHTMLDoc(Document htmlResponseDoc, Set<Map<String, String>> closedRooms) {
		addResultsToHTMLDoc(htmlResponseDoc, CLOSED_ROOMS_DISPLAYER_ID, closedRooms);
	}	
	
	private void addResultsToHTMLDoc(Document htmlResponseDoc, 
			String displayerID, 
			Set<Map<String, String>> results) {
		
		String oldHTML = "";
		for (Map<String, String> resultEntry : results) {
			oldHTML = htmlResponseDoc.getElementById(displayerID).html();
			htmlResponseDoc.getElementById(displayerID).html(oldHTML + htmlTableRowFor(resultEntry));
		}
	}

	private String htmlTableRowFor(Map<String, String> resultEntry) {
		String nextTransitionDateTimeString = parseNextTransitionDateAndTime(resultEntry.get("nextTransitionInstant"));
		// TODO handle displaying of open/close date and times
		return TABLE_ROW_TEMPLATE.replace(ROOM_NAME_PLACEHOLDER, resultEntry.get("roomID"))
						  		 .replace(AVAIL_SEATS_PLACEHOLDER, resultEntry.get("availSeats"))
						  		 .replace(CAPACITY_PLACEHOLDER, resultEntry.get("capacity"))
						  		 .replace(ADDRESS_PLACEHOLDER, resultEntry.get("address"))
						  		 .replace(NEXT_TRANSITION_PLACEHOLDER, nextTransitionDateTimeString)
						  		 .replace(UNIVERSITY_PLACEHOLDER, resultEntry.get("university"));
	}

	// Parse a dateTimestamp with format uuuu-MM-dd'T'HH:mm:ssxxx
	private String parseNextTransitionDateAndTime(String xsdDateAndTime) {
		// Separate date and time (their divided by "'T'") into two elements of the same array:
		String[] nextTransitionDateAndTime = xsdDateAndTime.split("T"); 

		// Parse the date: 
		String[] nextTransitionDateTokens = nextTransitionDateAndTime[0].split("-");
		StringBuilder nextTransitionDate = new StringBuilder();
		for (int i = 2; i >= 0; i--) {
			// This if prevents '/' from being appended before the day part of the data: dd/mm/yyyy. Without it 
			// we would have /dd/mm/yyyy
			if (i != 2) {
				nextTransitionDate.append("/");
			}
			nextTransitionDate.append(nextTransitionDateTokens[i]);
		}
		
		// Parse the time: from 'HH:mm:ssxxx' extract only HH:mm
		String nextTransitionTime = nextTransitionDateAndTime[1].substring(0, 5);

		return nextTransitionTime + " " + nextTransitionDate.toString();
	}
	
}
