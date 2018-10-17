package query.server.handlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

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
import query.server.utils.SPARQLQueryBuilder;
import sofia_kp.KPICore;
import sofia_kp.SIBResponse;
import sofia_kp.SSAP_sparql_response;

public class BaseHandler extends SIBFacingHandler {
	
	private SPARQLQueryBuilder sparqlQueryBuilder;
	
	private final static char VAR_PREFIX = '?'; 
	private final String roomIDVarName = "roomID";
	private final String addressVarName = "address";
	private final String fullAddressVarName = "fullAddress";
	private final String availableSeatsVarName = "availSeats";
	private final String roomStateVarName = "roomState";

	// TODO: This is terrible and should be changed. The leading queryWebServer/ is needed because this
	// will run in a Docker container and thus the current directory for the process container is 
	// .. wrt to this project directory. But this makes the source code depend on the dir structure of
	// its execution environment, which might change in the future. It should be the opposite: the src code
	// should make an assumption on how the files will be organized in its execution environment, and the
	// build/deploy scripts should make those assumptions true. Currently the opposite is happening. 
	private final String pathToHTMLTemplate = "../QueryWebServer/src/main/resources/indexTemplate.html";
	private final String openRoomsDisplayerID = "open_rooms_table";
	private final String closedRoomsDisplayerID = "closed_rooms_table";
	
	private final static String ROOM_NAME_PLACEHOLDER = "room-name";
	private final static String AVAIL_SEATS_PLACEHOLDER = "avail-seats";
	private final static String ADDRESS_PLACEHOLDER = "address";
	private final static String TABLE_ROW_TEMPLATE = "<tr><td>" + ROOM_NAME_PLACEHOLDER 
																+ "</td><td>" 
																+ AVAIL_SEATS_PLACEHOLDER 
																+ "</td><td>" 
																+ ADDRESS_PLACEHOLDER 
																+ "</td></tr>";
	
	private static final String SIB_CONNECTION_KEY = "sibConn";
	
	private static final Logger LOG = LogManager.getLogger();
	private static final Marker SPARQL = MarkerManager.getMarker("SPARQL");
	
	public BaseHandler() {
		sparqlQueryBuilder = new SPARQLQueryBuilder();
	}

	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession request) {
		LOG.info("Building response to " + request.getMethod() + " from " + request.getRemoteIpAddress());
		
		Response response;

		String sparqlQuery = buildSPARQLQuery();
		LOG.info(SPARQL, "Built SPARQL query to compute response:\n" + sparqlQuery);
		
		KPICore sibConnection = getSibConnection(request);
		LOG.info("Acquired connection to the SIB");
		
		SIBResponse resp = sibConnection.querySPARQL(sparqlQuery);

		if (resp == null || !resp.isConfirmed()) {
			response = NanoHTTPD.newFixedLengthResponse(
					"Server internal error: failed to acquire study rooms data from the SIB");
			response.setStatus(Status.INTERNAL_ERROR);
			LOG.error("Failed to acquire study rooms data from the SIB.");
			return response;
		}
		
		File htmlResponseFile = new File(pathToHTMLTemplate);
		Document htmlResponseDoc = null;
		try {
			htmlResponseDoc = Jsoup.parse(htmlResponseFile, "UTF-8", "");
			LOG.info("Loaded HTML page template for response.");
		} catch (IOException e) {
			LOG.error("Could not load HTML page template for response.");
			e.printStackTrace();
			response = NanoHTTPD.newFixedLengthResponse("Server internal error: Failed to build HTML page");
			response.setStatus(Status.INTERNAL_ERROR);
			return response;
		}

		addOpenRoomsToHTMLDoc(htmlResponseDoc, resp.sparqlquery_results);
		addClosedRoomsToHTMLDoc(htmlResponseDoc, resp.sparqlquery_results);
		// TODO check that this is actually the right invocation
		response = NanoHTTPD.newFixedLengthResponse(htmlResponseDoc.html());
		response.setMimeType(NanoHTTPD.MIME_HTML);
		LOG.info("Successfully built response to " + request.getMethod() + " from " + request.getRemoteIpAddress());
		
		return response;
	}
	
	private String buildSPARQLQuery() {
		//sparqlQueryBuilder.addPrefixMapping(null,null);
		
		// TODO This addPrefixes method is terrible and a more elegant solution MUST be found. What's wrong about it
		// is that it forces us to hardcode the ontology prefixes in this class methods. They should be configurable
		// externally instead.
		addPrefixes();
		
		addVariablesToSelect();
		addWhereClauseFilters();
		
		return sparqlQueryBuilder.getQuery();
	}
	
	// TODO this method MUST disapper and a more elegant solution MUST be found. "More elegant" means that the ontology
	// prefixes are not hardcoded
	private void addPrefixes() {
		sparqlQueryBuilder.addPrefixMapping("sr", "http://www.semanticweb.org/matteo/ontologies/2016/11/OperazioneStudyRoom#");
		sparqlQueryBuilder.addPrefixMapping("locn", "http://www.w3.org/ns/locn#");
	}

	private void addVariablesToSelect() {
		sparqlQueryBuilder.addVarToSelect(VAR_PREFIX + roomIDVarName);
		sparqlQueryBuilder.addVarToSelect(VAR_PREFIX + availableSeatsVarName);
		sparqlQueryBuilder.addVarToSelect(VAR_PREFIX + fullAddressVarName);
		sparqlQueryBuilder.addVarToSelect(VAR_PREFIX + roomStateVarName);
	}
	
	private void addWhereClauseFilters() {
		sparqlQueryBuilder.addWhereFilter(VAR_PREFIX + roomIDVarName + " sr:studyRoomState " + VAR_PREFIX 
										  + roomStateVarName);
		sparqlQueryBuilder.addWhereFilter(VAR_PREFIX + roomIDVarName + " sr:availableSeats " + VAR_PREFIX 
										  + availableSeatsVarName);
		sparqlQueryBuilder.addWhereFilter(VAR_PREFIX + roomIDVarName + " locn:address " + VAR_PREFIX 
										  + addressVarName);
		sparqlQueryBuilder.addWhereFilter(VAR_PREFIX + addressVarName + " locn:fullAddress " + VAR_PREFIX 
										  + fullAddressVarName);
	}
	
	private KPICore getSibConnection(IHTTPSession request) {
		return (KPICore) SIBFacingHandler.getSession(request).get(SIB_CONNECTION_KEY);
	}
	
	private void addOpenRoomsToHTMLDoc(Document htmlResponseDoc, SSAP_sparql_response sparqlQueryResults) {
		Vector<Vector<String[]>> resultsVector = sparqlQueryResults.getResults();
		// TODO .get(3) might be wrong, if there are bugs this might be a possible root cause 
		Set<Vector<String[]>> openRoomsResults = resultsVector
												 .parallelStream()
												 .filter(resultEntry -> resultEntry.get(3)[2].endsWith("open"))
												 .collect(Collectors.<Vector<String[]>>toSet());
		for (Vector<String[]> openRoomEntry : openRoomsResults) {
			htmlResponseDoc.getElementById(openRoomsDisplayerID).appendElement(htmlTableRowFor(openRoomEntry));
		}
	}

	private void addClosedRoomsToHTMLDoc(Document htmlResponseDoc, SSAP_sparql_response sparqlQueryResults) {
		Vector<Vector<String[]>> resultsVector = sparqlQueryResults.getResults();
		// TODO .get(3) might be wrong, if there are bugs this might be a possible root cause 
		Set<Vector<String[]>> closedRoomsResults = resultsVector
				 .parallelStream()
				 .filter(resultEntry -> resultEntry.get(3)[2].endsWith("closed"))
				 .collect(Collectors.<Vector<String[]>>toSet());
		for (Vector<String[]> closedRoomEntry : closedRoomsResults) {
			htmlResponseDoc.getElementById(closedRoomsDisplayerID).appendElement(htmlTableRowFor(closedRoomEntry));
		}
	}	

	private String htmlTableRowFor(Vector<String[]> roomEntry) {
		return TABLE_ROW_TEMPLATE.replace(ROOM_NAME_PLACEHOLDER, roomEntry.get(0)[2])
						  		 .replace(AVAIL_SEATS_PLACEHOLDER, roomEntry.get(1)[2])
						  		 .replace(ADDRESS_PLACEHOLDER, roomEntry.get(2)[2]);
	}
	
}
