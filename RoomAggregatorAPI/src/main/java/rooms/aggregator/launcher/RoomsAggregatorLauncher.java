package rooms.aggregator.launcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import rooms.aggregator.RoomsAggregator;
import rooms.aggregator.outcome.EmptyOutcome;
import rooms.aggregator.redsib09.RedSIB09RoomsAggregator;
import rooms.aggregator.redsib09.exceptions.FailedToJoinSmartspaceException;
import rooms.aggregator.redsib09.exceptions.SIBConnectionErrorException;
import sofia_kp.KPICore;
import sofia_kp.SIBResponse;

public class RoomsAggregatorLauncher {

	private RoomsAggregator roomsAggregator;
	private KPICore connToSIB;
	
	private String sibIPorHost;
	private String smartSpaceName;
	private String ontologyPrefix;
	private int sibPort;

	private static final String ROOM_ID_VAR_NAME = "roomID";
	// Any query that returns 1 result per room works
	private static final String ALL_ROOMS_IDS_SPARQL_QUERY_TEMPLATE = "PREFIX ns:<ontology-prefix> \n"
																	  + "SELECT ?" + ROOM_ID_VAR_NAME + " \n"
																	  + "WHERE { \n"
																	  + "?" + ROOM_ID_VAR_NAME + " ns:inUniversity "
																	  + "?univ \n"
																	  + "}";
	private String allRoomsIDsSparqlQuery;
	
	public static void main(String[] args) throws FailedToJoinSmartspaceException, SIBConnectionErrorException {
		RoomsAggregatorLauncher launcher = new RoomsAggregatorLauncher(args);
		launcher.startAggregatingAllRoomsInSIB();
	}

	public RoomsAggregatorLauncher(String[] args) {
		setInstanceFieldsFromInputArgs(args);
		roomsAggregator = new RedSIB09RoomsAggregator(sibIPorHost, sibPort, smartSpaceName, ontologyPrefix);
		allRoomsIDsSparqlQuery = 
				ALL_ROOMS_IDS_SPARQL_QUERY_TEMPLATE.replace("ontology-prefix", ontologyPrefix);
	}
	
	private void setInstanceFieldsFromInputArgs(String[] args) {
		sibIPorHost 	= args[0];
		sibPort 		= Integer.parseInt(args[1]);
		smartSpaceName 	= args[2];
		ontologyPrefix 	= args[3];		
	}

	private void startAggregatingAllRoomsInSIB() throws FailedToJoinSmartspaceException, SIBConnectionErrorException {
		// Instantiate KPICore instance with same SIB IP, port and smart space name as roomsAggregator
		connToSIB = createConnectionToSIB();
		
		// Get the IDs of all the study rooms in the SIB
		List<String> roomIDs;
		try {
			roomIDs = getAllRoomsIDs();
		} catch (SIBConnectionErrorException e) {
			// Try to leave the smart space as a clean up action
			SIBResponse resp = connToSIB.leave();
			if (resp == null || !resp.isConfirmed()) {
				throw new SIBConnectionErrorException(e.getMessage() + " Clean up action: leave smart space " 
													  + smartSpaceName + " failed.");
			}
			
			throw e;
		}
		
		// At this point we no longer need to interact with the SIB so we leave the smart space. TODO check if leave was 
		// successful and log if not
		connToSIB.leave();
		
		for (String roomID : roomIDs) {
			EmptyOutcome outcome = roomsAggregator.startAggregatingRoom(roomID);
			if (!outcome.success()) {
				// TODO turn this into proper logging
				System.out.println("Failed to aggregate room with ID " 
						+ roomID 
						+ ": "
						+ outcome.getMessage()
						+ " . The aggregator will exit after attempting to stop aggregating the other rooms.");
				cleanUp(roomIDs);
				System.exit(1);
			}
		}	
	}
	
	private void cleanUp(List<String> roomIDs) {
		for (String roomID : roomIDs) {
			EmptyOutcome outcome = null;
			if (roomsAggregator.isAggregatingRoom(roomID)) {
				outcome = roomsAggregator.stopAggregatingRoom(roomID);
			}
			if (outcome != null && !outcome.success()) {
				// TODO turn this into proper logging
				System.out.println("Clean up: failed to stop aggregating room with ID " 
						+ roomID 
						+ ": "
						+ outcome.getMessage()
						+ " . The aggregator will exit now.");
			}
		}	
	}

	private KPICore createConnectionToSIB() throws FailedToJoinSmartspaceException {
		KPICore connToSIB = new KPICore(sibIPorHost, sibPort, smartSpaceName);
		SIBResponse joinResp = connToSIB.join();
		
		if (joinResp == null || !joinResp.isConfirmed()) {
			throw new FailedToJoinSmartspaceException("createConnectionToSIB - " + "could not join smart space " 
												  + smartSpaceName + " at SIB with IP/host_name:port " 
												  + sibIPorHost + ":" + sibPort);
		}
		
		return connToSIB;
	}

	private List<String> getAllRoomsIDs() throws SIBConnectionErrorException {
		// Run SPARQL query against the SIB to get the IDs of all the rooms stored in it
		SIBResponse queryResp = connToSIB.querySPARQL(allRoomsIDsSparqlQuery);
		
		if (queryResp == null || !queryResp.isConfirmed()) {
			throw new SIBConnectionErrorException("getAllRoomsIDs - " + " failed to get list of roomIDs stored in "
												  + "smart space " + smartSpaceName + " in SIB with IP/host_name:port"
												  + " equal to " + sibIPorHost + ":" + sibPort);
		}
		
		return extractRoomIDsFromSIBResponse(queryResp);
	}
	
	private List<String> extractRoomIDsFromSIBResponse(SIBResponse queryResp) {
		short roomIDNameFieldIndex = 2;
		Vector<String[]> roomIDs = queryResp.sparqlquery_results.getResultsForVar(ROOM_ID_VAR_NAME);
		List<String> roomIDsToReturn = new ArrayList<String>(roomIDs.size());
		
		for (String[] curResultEntry : roomIDs) {
			// TODO The fact that we are splitting based on # is not robust and should be done instead in such a way 
			// that edge cases are correctly handled (currently they're not). Examples of edge cases: there are more 
			// than one # in the namespace of the ontology or there's a # also in the roomID.
			roomIDsToReturn.add(curResultEntry[roomIDNameFieldIndex].split("#")[1]);
		}
		
		return roomIDsToReturn;
	}
}
