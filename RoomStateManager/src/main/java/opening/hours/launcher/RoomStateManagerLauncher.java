package opening.hours.launcher;

import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.zip.*;
import opening.hours.controller.*;
import opening.hours.exceptions.*;
import opening.hours.model.*;
import sofia_kp.*;

public class RoomStateManagerLauncher {

	private RoomOpenerCloser roomOpenerCloser;
	private KPICore connToSIB;
	
	private String openingHoursDir;
	
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
	
	private static final DateTimeFormatter HOUR_AND_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
	
	public static void main(String[] args) throws SIBConnectionErrorException {
		RoomStateManagerLauncher launcher = new RoomStateManagerLauncher(args);
		launcher.startManagingAllRoomsInSIB();
	}

	public RoomStateManagerLauncher(String[] args) throws SIBConnectionErrorException {
		setInstanceFieldsFromInputArgs(args);
		roomOpenerCloser = new RedSIB09RoomOpenerCloser(sibIPorHost, sibPort, smartSpaceName, ontologyPrefix);
		allRoomsIDsSparqlQuery = 
							ALL_ROOMS_IDS_SPARQL_QUERY_TEMPLATE.replace("<ontology-prefix>", "<" + ontologyPrefix+ ">");
	}

	private void setInstanceFieldsFromInputArgs(String args[]) {
		sibIPorHost 	= args[0];
		sibPort 		= Integer.parseInt(args[1]);
		smartSpaceName 	= args[2];
		ontologyPrefix 	= args[3];
		openingHoursDir = args[4].endsWith(File.separator) ? args[4] : args[4] + File.separator;			
	} 
	
	public void startManagingAllRoomsInSIB() throws SIBConnectionErrorException {		
		// Instantiate KPICore instance with same SIB IP, port and smart space name as roomOpenerCloser
		connToSIB = createConnectionToSIB();
		
		// Get the IDs of all the study rooms in the SIB
		List<String> roomIDs;
		try {
			roomIDs = getAllRoomsIDs();
	//		injectFakeNextTransitionInstants(roomIDs, connToSIB);
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
		
		OpeningHours oh;
		for (String roomID : roomIDs) {
			try {
				oh = getOpeningHoursForRoom(roomID);
				startManagingSingleRoomWRetries(roomID, oh);
			} catch (Exception e) {
				// TODO Turn this print to stdout into proper logging
				System.out.println(e.getMessage() + ":");
				e.printStackTrace();
			} 
		}
	}

	private KPICore createConnectionToSIB() throws SIBConnectionErrorException {
		KPICore connToSIB = new KPICore(sibIPorHost, sibPort, smartSpaceName);
		SIBResponse joinResp = connToSIB.join();
		
		if (joinResp == null || !joinResp.isConfirmed()) {
			throw new SIBConnectionErrorException("createConnectionToSIB - " + "could not join smart space " 
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
			// TODO The fact that we are splitting based on # is not robust and should be done in such a way that edge 
			// cases are handled (currently they're not). Examples of edge cases: there are more than one # in the 
			// namespace of the ontology or there's a # also in the roomID
			roomIDsToReturn.add(curResultEntry[roomIDNameFieldIndex].split("#")[1]);
		}
		
		return roomIDsToReturn;
	}

	private OpeningHours getOpeningHoursForRoom(String roomID) throws FileNotFoundException, IOException, 
																	  DataFormatException {
		String openingHoursFilePath = openingHoursDir + roomID + "-oh";
		List<DayOfWeekAndTime> dayOfWeekAndTimes = new ArrayList<DayOfWeekAndTime>();
		
		try (BufferedReader openingHoursReader = new BufferedReader(new FileReader(openingHoursFilePath))) {
			String currentLine;
			while((currentLine = openingHoursReader.readLine()) != null) {
				DayOfWeekAndTime parsedDayOfWeekAndTimes[] = parseOpeningHoursFileLineIntoDayOfWeekAndTime(currentLine);
				for (int i = 0; i < 2; i++) {
					dayOfWeekAndTimes.add(parsedDayOfWeekAndTimes[i]);
				}
			}	
		}
		
		DayOfWeekAndTime dayOfWeekAndTimesAsArray[] = 
				dayOfWeekAndTimes.toArray(new DayOfWeekAndTime[dayOfWeekAndTimes.size()]);
		return new OpeningHoursImpl(dayOfWeekAndTimesAsArray);
	}

	private DayOfWeekAndTime[] parseOpeningHoursFileLineIntoDayOfWeekAndTime(String currentLine) 
																							throws DataFormatException {
		String[] tokens = currentLine.split("\\s+");
		
		if (tokens.length != 3) {
			throw new DataFormatException("Found a line in an opening hours file in directory " + openingHoursDir 
					+ " with " + "invalid format. Valid format is: <Full-day-of-week-name> HH:mm HH:mm. Example of valid " 
					+ " format is: \"MONDAY 08:00 22:00\". Example of invalid format is \"MON 08:00 22:00\" because" 
					+ " the name of the day of the week is shortened instead of being in its full length.");
		}

		DayOfWeek dayOfWeek = DayOfWeek.valueOf(tokens[0].trim().toUpperCase());
		LocalTime openingTime = LocalTime.parse(tokens[1].trim(), HOUR_AND_MINUTE_FORMATTER);
		LocalTime closingTime = LocalTime.parse(tokens[2].trim(), HOUR_AND_MINUTE_FORMATTER);
		
		return new DayOfWeekAndTime[]{new DayOfWeekAndTime(dayOfWeek, openingTime), 
									  new DayOfWeekAndTime(dayOfWeek, closingTime)};
	}
	
	private void startManagingSingleRoomWRetries(String roomID, OpeningHours oh) throws 
													RoomIsAlreadyManagedException, FailedToStartManagingRoomException {
		boolean roomIsManaged = false;
		short nbrOfAttempts = 0;
		Exception lastExceptionThrown = null;
		
		while (!roomIsManaged && nbrOfAttempts < 3) {
			nbrOfAttempts++;
			try {
				roomOpenerCloser.startManagingRoom(roomID, oh);
				roomIsManaged = true;
			} catch (FailedToStartManagingRoomException e) {
				lastExceptionThrown = e;
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// Safely ignore this, it's not an issue if the sleep was interrupted
				}
			}
		}
		
		if (!roomIsManaged) {
			throw (FailedToStartManagingRoomException) lastExceptionThrown;
		}
	}
}












