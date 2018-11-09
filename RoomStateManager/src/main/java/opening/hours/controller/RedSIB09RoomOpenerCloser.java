package opening.hours.controller;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import opening.hours.exceptions.BadOntologyPrefixException;
import opening.hours.exceptions.BadOpeningHoursException;
import opening.hours.exceptions.BadRoomIDException;
import opening.hours.exceptions.BadSIBIPorHostException;
import opening.hours.exceptions.BadSIBPortNumberException;
import opening.hours.exceptions.BadSmartSpaceNameException;
import opening.hours.exceptions.FailedToStartManagingRoomException;
import opening.hours.exceptions.FailedToStopManagingRoomException;
import opening.hours.exceptions.FailedToUpdateRoomStateException;
import opening.hours.exceptions.RoomIsAlreadyManagedException;
import opening.hours.exceptions.RoomIsNotManagedException;
import opening.hours.exceptions.SIBConnectionErrorException;
import opening.hours.model.DayOfWeekAndTime;
import opening.hours.model.OpenClosed;
import opening.hours.model.OpeningHours;
import opening.hours.model.OpeningHoursWithStartDate;

import sofia_kp.KPICore;
import sofia_kp.SIBResponse;

public class RedSIB09RoomOpenerCloser implements RoomOpenerCloser {
	// static fields used to turn ZonedDateTime instances into strings compliant with the xsd:dateTimestamp format
	private static final String XSD_DATE_TIMESTAMP_PATTERN = "uuuu-MM-dd'T'HH:mm:ssxxx";  
	private static final DateTimeFormatter XSD_DATE_TIMESTAMP_FORMATTER = 
																DateTimeFormatter.ofPattern(XSD_DATE_TIMESTAMP_PATTERN);
			
	// SIB coordinates (IP, port, ss name)
	private String sibIPorHost;
	private int sibPort;
	private String smartSpaceName;
	
	// Prefix for the study room ontology the SIB enforces
	private String ontologyPrefix;

	// An executor to which tasks performing room state updates are submitted
	private ScheduledExecutorService backgroundRoomStateUpdater;
	
	private Map<String, UUID> roomIDtoSubscriptionID;
	
	/**
	 * Connection to the SIB used when adding a study room among the managed ones. Used to 
	 * check whether the said room is stored in the SIB
	 */
	private KPICore sibConnToTestRoomExistence;
	
	// Connection to the SIB used by the background executor to update the rooms state
	private KPICore sibConnToUpdateRoomState;
	
	/**
	 * A string representing a template for the SPARQL update query to change the room state and the next timestamp at 
	 * which the room changes state. It's a template because not all the values are hardcoded in it (e.g. room ID, room
	 * new state) as they change at runtime. The user must fill the template with the appropriate values before sending
	 * the SPARQL update.
	 */
	private String roomStateUpdateQueryTemplate;
	// TODO uncomment or remove depending on test results
	private String firstRoomStateUpdateQueryTemplate;
	
	private static final Logger LOG = LogManager.getLogger();
	private static final Marker SPARQL = MarkerManager.getMarker("SPARQL");
		
	
	public RedSIB09RoomOpenerCloser(String sibIPorHost, int sibPort, String smartSpaceName, String ontologyPrefix) 
			throws SIBConnectionErrorException {
		
		LOG.info("Initializing...");
		
		validateInputs(sibIPorHost, sibPort, smartSpaceName, ontologyPrefix);
		this.sibIPorHost = sibIPorHost;
		this.sibPort = sibPort;
		this.smartSpaceName = smartSpaceName;
		this.ontologyPrefix = ontologyPrefix;
		
	    /**		
 		 * A string representing a template for the SPARQL update query to change the room state and the next timestamp 
 		 * at which the room changes state. It's only a template because some values of the query change everytime the 
 		 * update is sent to the SIB. Before sending the update to the SIB, the following strings in the template must 
 		 * be replaced with the appropriate values: 
 		 * <room-ID>: the ID of the room whose state is being updated.
 		 * <new-state>: the new state of the room, i.e. "open" or "closed".
 		 * <xsd-new-date-timestamp>: the xsd:dateTimestamp at which the first future update of the room will take place. 
 		 * <old-transition-predicate>: "opensAt" if <new-state> is "open", "closesAt" if <new-state> is "closed".
 		 * <new-transition-predicate>: "opensAt" if <new-state> is "closed", "closesAt" if <new-state> is "open".
 		 */
		roomStateUpdateQueryTemplate = "PREFIX ns:<" + ontologyPrefix + "> \n"
										+ "DELETE { \n"
										+ "ns:<room-ID> ns:studyRoomState ?oldState . \n"
										+ "ns:<room-ID> ns:<old-transition-predicate> ?oldDateTimestamp . \n"
										+ "} INSERT { \n"
										+ "ns:<room-ID> ns:studyRoomState ns:<new-state> . \n"
										+ "ns:<room-ID> ns:<new-transition-predicate> <xsd-new-date-timestamp> . \n"
										+ "} WHERE { \n"
										+ "ns:<room-ID> ns:studyRoomState ?oldState . \n"
										+ "ns:<room-ID> ns:<old-transition-predicate> ?oldDateTimestamp . \n"
										+ "}";
		
// TODO uncomment or remove depending on test results
		firstRoomStateUpdateQueryTemplate = "PREFIX ns:<" + ontologyPrefix + "> \n"
										+ "INSERT { \n"
										+ "ns:<room-ID> ns:studyRoomState ns:<new-state> . \n"
										+ "ns:<room-ID> ns:<new-transition-predicate> <xsd-new-date-timestamp> . \n"
										+ "}";		
				
		sibConnToTestRoomExistence = getConnectionToSIB(sibIPorHost, sibPort, smartSpaceName);
		LOG.debug("Successfully created SIB connection to test existence of rooms");
		try {
			sibConnToUpdateRoomState = getConnectionToSIB(sibIPorHost, sibPort, smartSpaceName);
			LOG.debug("Successfully created SIB connection to update room state.");
		} catch (SIBConnectionErrorException e) {
			LOG.error("Initialization failed because it was not possible to obtain the connection to the SIB used to "
					  + "send room state update messages. Beginning clean up.");
			String cleanUpMessage = clearSIBConnToTestRoomExistence();
			throw new SIBConnectionErrorException(e.getMessage() + cleanUpMessage);
		}
		
		/**
		 * For the current version of this class, there MUST be ONLY ONE thread performing the updates, because even if
		 * each room has a separate RoomUpdateTask instance, they share state: the connection to the 
		 * SIB used to perform the state updates is shared and is not thread-safe. Sharing it across multiple threads 
		 * will cause problems. That is why a single thread scheduled executor is used.
		 */
		backgroundRoomStateUpdater = Executors.newSingleThreadScheduledExecutor();
		LOG.info("Started background thread to execute room state update tasks");
		
		roomIDtoSubscriptionID = new ConcurrentHashMap<String, UUID>();
		
		LOG.info("Initialization successful");
	}

	
	private void validateInputs(String sibIPorHost, int sibPort, String smartSpaceName, String ontologyPrefix) {
		if (sibIPorHost == null || sibIPorHost.trim().isEmpty()) {
			throw new BadSIBIPorHostException(sibIPorHost == null ? "received null for input parameter "
											  + "sibIPorHost" : "received empty input parameter sibIPorHost");
		}
		if (sibPort < 1 || sibPort > 65535) {
			throw new BadSIBPortNumberException("SIB port number is not a valid port number. Valid port nbrs are "
												+ "in range [1,65535]");
		}
		if (smartSpaceName == null || smartSpaceName.trim().isEmpty()) {
			throw new BadSmartSpaceNameException(smartSpaceName == null ? 
				"received null input parameter smartspaceName": "received empty input parameter smartspaceName");
		}
		if (ontologyPrefix == null || ontologyPrefix.trim().isEmpty()) {
			throw new BadOntologyPrefixException(ontologyPrefix == null ? "received null for input parameter "
					  						+ "ontologyPrefix" : "received empty input parameter ontologyPrefix");
		}
	}
	
	
	private KPICore getConnectionToSIB(String sibIPorHost, int sibPort, String smartSpaceName) 
			throws SIBConnectionErrorException {
		
		KPICore sibConn = new KPICore(sibIPorHost, sibPort, smartSpaceName);
		SIBResponse resp = sibConn.join();
				
		if (resp == null || !resp.isConfirmed()) {
			throw new SIBConnectionErrorException("Failed to join the smart space " + smartSpaceName + " for SIB with "
					+ " IP " + sibIPorHost + " and port " + sibPort + ".");
		}
		
		return sibConn;
	}

	
	private String clearSIBConnToTestRoomExistence() {
		SIBResponse resp = sibConnToTestRoomExistence.leave();
		
		if (resp == null || !resp.isConfirmed()) {
			String cleanUpMessage = " Clean up action failed: sibConnToTestRoomExistence.leave() failed, thus smart "
									+ "space \"" + smartSpaceName + "\" could not be left.";
			return cleanUpMessage;
		}
		
		return "";
	}
	
	@Override
	public void startManagingRoom(String roomID, OpeningHours oh) 
			throws FailedToStartManagingRoomException, RoomIsAlreadyManagedException {
		
		LOG.info("Received request to start managing room with ID " + roomID);
		
		validateInputsForStartManagingRoom(roomID, oh);
		LOG.debug("Request to start managing room with ID " + roomID + ": inputs are valid.");
		
		if (isManagingRoom(roomID)) {
			throw new RoomIsAlreadyManagedException("Room with ID " + roomID + " is already managed. If you want to "
					+ "update its opening hours, use method updateRoomOpeningHours(OpeningHours newOH) instead.");
		}
		LOG.debug("Request to start managing room with ID " + roomID + ": room is not already managed.");
		
		try {
			if (!roomExists(roomID)) {
				throw new FailedToStartManagingRoomException("No room with ID " + roomID + " exists.");
			}
		} catch (SIBConnectionErrorException e) {
			throw new FailedToStartManagingRoomException(e.getMessage(), e);
		}
		LOG.debug("Request to start managing room with ID " + roomID + ": the room exists in the SIB.");
		
		if (oh instanceof OpeningHoursWithStartDate) {
			LocalDate startDate = ((OpeningHoursWithStartDate) oh).getStartDate();
			LOG.info("Request to start managing room with ID " + roomID + ": the provided opening hours have a start"
					  + " date: " + startDate + ". As a consequence the room state will begin being updated only after "
					  + "beginning from the start date." );
			startManagingRoomAtStartDate(roomID, (OpeningHoursWithStartDate) oh);
		} else {
			LOG.info("Request to start managing room with ID " + roomID + ": the provided opening hours don't have a "
					 + "start date. As a consequence the room state will begin being updated immediately.");
			startManagingRoomNow(roomID, oh);
		}
		
		LOG.info("The room with ID " + roomID + " is now being successfully managed");
	}

	private void validateInputsForStartManagingRoom(String roomID, OpeningHours oh) {
		if (roomID == null || roomID.trim().isEmpty()) {
			throw new BadRoomIDException(roomID == null ? "received null for input parameter roomID" 
					 : "received empty input parameter roomID");
		}
		
		if (oh == null) {
			throw new BadOpeningHoursException("Received null for parameter \"oh\" of type OpeningHours.");
		}
	}
	
	private boolean roomExists(String roomID) throws SIBConnectionErrorException {
		// This query returns results if and only if the SIB stores a room with ID roomID
		String existenceQuery = "PREFIX ns:<" + ontologyPrefix + "> \n" 
								+ "SELECT ?predicate \n"
								+ "WHERE { \n"
								+ "ns:" + roomID + " ?predicate ?object . \n"
								+ "}"; 
		
		// send query to the SIB
		SIBResponse resp = sibConnToTestRoomExistence.querySPARQL(existenceQuery);
		
		if (resp == null || !resp.isConfirmed()) {
			// it was not possible to connect to the SIB or the SIB could not process the query
			throw new SIBConnectionErrorException("Failed to query the SIB with IP " + sibIPorHost + " and port " 
					+ sibPort + " to check whether the room with ID " + roomID + " exists. SPARQL query:\n" 
					+ existenceQuery);
		}
		LOG.debug(SPARQL, "Successfully queried the SIB for existence of room with ID " + roomID 
				  + " with SPARQL query:\n " + existenceQuery);
		
		if (resp.sparqlquery_results.hasResults()) {
			// the room exists AKA it's stored in the SIB
			return true;
		} else {
			// the room does not exist AKA it isn't stored in the SIB
			return false;
		}
	}

	// FIXME this has not been tested and is probably buggy. Should not be used
	/*
	 * 
	 * this has not been tested and is probably buggy. Should not be used
	 * 
	 * @param roomID
	 * @param oh
	 */
	private void startManagingRoomAtStartDate(String roomID, OpeningHoursWithStartDate oh) {
		/**
		 * Compute the number of seconds between now and the instant at which we must begin monitoring the room with
		 * ID roomID. If that instant is in the past, the computed number will be negative. Notice how the computed
		 * number only uses the seconds between the two instants. To be 100% accurate use nanoseconds instead, but
		 * for this application expected use cases using nanoseconds is an overkill: even if a room state is updated
		 * a few seconds earlier/later is not perceivable by the final user, especially considering that many hours 
		 * (e.g. 8) will occur between two successive state updates.
		 */
		Instant now = Instant.now();
		Instant startInstant = oh.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
		long delayInSeconds = Duration.between(now, startInstant).getSeconds();
		
		startManagingRoomWithDelayInSeconds(roomID, oh, delayInSeconds);
	}
	
	
	private void startManagingRoomNow(String roomID, OpeningHours oh) {		
		startManagingRoomWithDelayInSeconds(roomID, oh, 0);
	}


	private void startManagingRoomWithDelayInSeconds(String roomID, OpeningHours oh, long delayInSeconds) {
		UUID uuid = UUID.randomUUID();
		roomIDtoSubscriptionID.put(roomID, uuid);
		LOG.debug("created subscription for room with ID " + roomID + " has ID " + uuid);
		
		LOG.debug("Room with ID " + roomID + ". Next update task about to be scheduled to run in " + delayInSeconds 
				  + " seconds");
		backgroundRoomStateUpdater.schedule(new RoomUpdateTask(roomID, oh, uuid), delayInSeconds, TimeUnit.SECONDS);	
		LOG.debug("Room with ID " + roomID + ". Next update task successfully scheduled to be run in " + delayInSeconds 
				  + " seconds");
	}
	
	
	
	@Override
	public void stopManagingRoom(String roomID) throws FailedToStopManagingRoomException {
		LOG.info("Received request to stop managing room with ID " + roomID + ".");
		if (roomID == null || roomID.trim().isEmpty()) {
			throw new BadRoomIDException(roomID == null ? "received null for input parameter roomID" 
					 : "received empty input parameter roomID");
		}
		LOG.debug("Request to stop managing room with ID " + roomID + ": roomID is valid");
		
		if (!isManagingRoom(roomID)) {
			throw new RoomIsNotManagedException("No room with ID " + roomID + " is being managed."); 
		}
		LOG.debug("Request to stop managing room with ID " + roomID + ": room is among the managed ones.");

		UUID subID = roomIDtoSubscriptionID.remove(roomID);
		LOG.debug("Request to stop managing room with ID "+ roomID + ": subscription nbr " + subID + " cancelled.");
		
		LOG.info("Successfully stopped managing room with ID " + roomID + ".");
	}
	
	@Override
	public boolean isManagingRoom(String roomID) {
		return roomIDtoSubscriptionID.containsKey(roomID);
	}
	
	private void submitNewTaskAt(RoomUpdateTask roomUpdateTask, ZonedDateTime startOfExecution, String roomID) {
		/**
		 * Notice how the delay after which the room update task is executed uses only seconds. To be 100% accurate use 
		 * nanoseconds instead, but for this application expected use cases using nanoseconds is an overkill (even if a
		 * room state is updated a few seconds earlier/later is not perceivable by the final user, especially 
		 * considering that many hours (e.g. 8) will occur between two successive state updates.
		 */
		long submissionDelaySeconds = 
				Duration.between(Instant.now(), startOfExecution.toInstant()).plusMillis(1000).getSeconds();
		LOG.debug("Room with ID " + roomID + ". Next update task about to be scheduled to run in " 
				+ submissionDelaySeconds + " seconds");
		backgroundRoomStateUpdater.schedule(roomUpdateTask, submissionDelaySeconds, TimeUnit.SECONDS);
		LOG.debug("Room with ID " + roomID + ". Next update task scheduled to run in " + submissionDelaySeconds 
				  + " seconds");
	}

// TODO remove or uncomment depending on test results
	private void sendFirstSPARQLUpdate(String roomID, OpenClosed newState,
			ZonedDateTime nextTransitionZonedDateTime) throws SIBConnectionErrorException {

		String nextTransitionDateTimestamp = XSD_DATE_TIMESTAMP_FORMATTER.format(nextTransitionZonedDateTime);
		nextTransitionDateTimestamp = "\"" + nextTransitionDateTimestamp + "\"";
		String sparqlUpdate = buildFirstSparqlUpdate(roomID, 
				newState.toString().toLowerCase(), 
				nextTransitionDateTimestamp);
		
		SIBResponse resp = sibConnToUpdateRoomState.update_sparql(sparqlUpdate);
		if (resp == null || !resp.isConfirmed()) {
			throw new SIBConnectionErrorException("Failed to send SPARQL update to SIB while attempting to update "
					+ "the state of room with ID " + roomID + " to " + newState + ". SPARQL Update:\n" + sparqlUpdate);
		}
		LOG.debug(SPARQL, "Successfully sent the following SPARQL update to the SIB:\n" + sparqlUpdate);
	}

	private void sendSPARQLUpdate(String roomID, OpenClosed newState, ZonedDateTime nextTransitionZonedDateTime)
			throws SIBConnectionErrorException {
		
		String nextTransitionDateTimestamp = XSD_DATE_TIMESTAMP_FORMATTER.format(nextTransitionZonedDateTime);
		nextTransitionDateTimestamp = "\"" + nextTransitionDateTimestamp + "\"";
		String sparqlUpdate = buildSparqlUpdate(roomID, newState.toString().toLowerCase(), nextTransitionDateTimestamp);
		
		SIBResponse resp = sibConnToUpdateRoomState.update_sparql(sparqlUpdate);
		if (resp == null || !resp.isConfirmed()) {
			throw new SIBConnectionErrorException("Failed to send SPARQL update to SIB while attempting to update "
					+ "the state of room with ID " + roomID + " to " + newState + ". SPARQL Update:\n" + sparqlUpdate);
		}
		LOG.debug(SPARQL, "Successfully sent the following SPARQL update to the SIB:\n" + sparqlUpdate);
	}

	private String buildSparqlUpdate(String roomID, String newState, String newDateTimestamp) {
		String oldTransitionPredicate = newState.equals("open") ? "opensAt" : "closesAt";
		String newTransitionPredicate = newState.equals("open") ? "closesAt" : "opensAt";
		
		return roomStateUpdateQueryTemplate.replace("<room-ID>", roomID)
										   .replace("<new-state>", newState)
										   .replace("<old-transition-predicate>", oldTransitionPredicate)
										   .replace("<new-transition-predicate>", newTransitionPredicate)
										   .replace("<xsd-new-date-timestamp>", newDateTimestamp);
	}

// TODO remove or uncomment depending on tests results
	private String buildFirstSparqlUpdate(String roomID, String newState, String newDateTimestamp) {
		String newTransitionPredicate = newState.equals("open") ? "closesAt" : "opensAt";
		
		return firstRoomStateUpdateQueryTemplate.replace("<room-ID>", roomID)
										   		.replace("<new-state>", newState)
										   		.replace("<new-transition-predicate>", newTransitionPredicate)
										   		.replace("<xsd-new-date-timestamp>", newDateTimestamp);
	}
	
	/**
	 * This method is meant to be used for testing purposes only, you should never use that elsewhere.
	 * @throws InterruptedException 
	 */
	protected void stopBackgroundScheduler() throws InterruptedException {
		LOG.debug("Stopping the background thread executing room state update tasks...");
		backgroundRoomStateUpdater.shutdownNow();	
		LOG.debug("Stopping the background thread executing room state update tasks: scheduler stopped. Awaiting "
				+ "termination of already scheduled tasks...");
		backgroundRoomStateUpdater.awaitTermination(5, TimeUnit.SECONDS);
		LOG.debug("Stopping the background thread executing room state update tasks: Wait is over. Either all the "
				+ "already scheduled tasks have completed, or the 5s timeout has elapsed.");

	}
	
	/**
	 * Class representing the task of updating a single study room's state (open or closed). It's meant to be executed 
	 * in background by an executor multiple times. Besides updating the state, before termination of the run method 
	 * this task schedules itself for execution at the instant of the next room state change. For instance, if the 
	 * associated room opens on Monday at 08:00 and closes on Monday at 23:00, this task will be executed on Monday at 
	 * 08:00 to update the room state from closed to open, and will also schedule its own re-execution on Monday at 
	 * 23:00 to update the room state again, this time from open to closed. 
	 */
	private class RoomUpdateTask implements Runnable {
		// ID of the room this task is associated with
		private String roomID;
		
		// Opening Hours of the room with ID roomID.
		private OpeningHours oh;
		
		private UUID subID;

		// TODO remove or uncomment depending on tests results
		private boolean thisIsFirstUpdate;
		
		protected RoomUpdateTask(String roomID, OpeningHours oh, UUID subID) {
			this.roomID = roomID;
			this.oh = oh;
			this.subID = subID;
			// TODO remove or uncomment depending on tests results
			thisIsFirstUpdate = true;
		}

		
		@Override
		public void run() {			
			if (!subID.equals(roomIDtoSubscriptionID.get(roomID))) {
				LOG.debug("Update task for room " + roomID + " has been cancelled because subscription nbr. " + subID 
						  + " no longer exists.");
				return;
			}
			
			ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
			LOG.debug("Updating room with ID " + roomID + ", sub nbr " + subID + ": current DateTime = " + now);
			
			// nextTransition means "next change of the room state"
			DayOfWeekAndTime nextTransitionDayOfWeekAndTime = computeNextTransitionDayOfWeekAndTimeAfter(now);
			LOG.debug("Updating room with ID " + roomID + ", sub nbr " + subID + ": next transition = " 
					  + nextTransitionDayOfWeekAndTime);
			
			OpenClosed newState = computeNewRoomState(nextTransitionDayOfWeekAndTime);
			LOG.debug("Updating room with ID " + roomID + ", sub nbr " + subID + ": new state = " + newState);
			
			ZonedDateTime nextTransitionZonedDateTime = 
					computeNextTransitionZonedDateTimeAfter(now, nextTransitionDayOfWeekAndTime);
			
			try {
				updateRoomState(newState, nextTransitionZonedDateTime);
			} catch (FailedToUpdateRoomStateException e) {
				roomIDtoSubscriptionID.remove(roomID);
				
				LOG.error("There's been an error while attempting to update the state of the room with ID " + roomID 
						 + ". The state could not be updated and the ROOM is NO LONGER MANAGED.");
				LOG.debug("Failure in updating the state of the room with ID " + roomID + ": an exception of type " 
						 + e.getCause().getClass().getName() + " was thrown, with message: " + e.getMessage());
				
				/**
				 * If the update failed we return immediately and the next update task execution is not scheduled => 
				 * the room state will stop being updated. This is correct because the current state update has failed,
				 * and this method is too low-level to implement recovery actions, which are left to the enclosing 
				 * RedSI09RoomOpenerCloser or to the client of the enclosing RedSI09RoomOpenerCloser.
				 */
				return;
			}
			
			/**
			 * Schedule the update two seconds later than the computed next transition time. This way, the case where
			 * this task is executed before the actual transition time by a few milliseconds will never occur. This 
			 * unfortunate case is rare but might happen (there are no guarantees at the millisecond precision on when 
			 * the scheduled tasks will be executed). Without the robustness margin, in the aforementioned case, this 
			 * update task would resubmit itself continuously before the actual transition time is reached. This does 
			 * not lead to an inconsistent state in the SIB, but is less efficient.  
			 */
			short robustnessMarginSeconds = 2;
			submitNewTaskAt(this, nextTransitionZonedDateTime.plus(robustnessMarginSeconds, ChronoUnit.SECONDS), roomID);
		}

		
		private DayOfWeekAndTime computeNextTransitionDayOfWeekAndTimeAfter(ZonedDateTime now) {
			DayOfWeekAndTime nowDayOfWeekAndTime = new DayOfWeekAndTime(now.getDayOfWeek(), now.toLocalTime());
			return oh.openCloseDayOfWeekAndTimeAfter(nowDayOfWeekAndTime);
		}


		private OpenClosed computeNewRoomState(DayOfWeekAndTime nextTransitionDayOfWeekAndTime) {
			return oh.getOpenCloseBeforeDayOfWeekAndTime(nextTransitionDayOfWeekAndTime);
		}
		
		
		private ZonedDateTime computeNextTransitionZonedDateTimeAfter(ZonedDateTime now, 
																	DayOfWeekAndTime nextTransitionDayOfWeekAndTime) {

			ZonedDateTime nextTransitionZonedDateTime = 
					 	 	 now.with(TemporalAdjusters.nextOrSame(nextTransitionDayOfWeekAndTime.dayOfWeek))
					 	 		.with(ChronoField.HOUR_OF_DAY, nextTransitionDayOfWeekAndTime.time.getHour())
					 	 		.with(ChronoField.MINUTE_OF_HOUR, nextTransitionDayOfWeekAndTime.time.getMinute())
					 	 		.with(ChronoField.SECOND_OF_MINUTE, nextTransitionDayOfWeekAndTime.time.getSecond());
			
			return nextTransitionZonedDateTime;
		}

		private void updateRoomState(OpenClosed newState, ZonedDateTime nextTransitionZonedDateTime) 
				throws FailedToUpdateRoomStateException {
			
			try {
// TODO uncomment or remove depending on test results
				if (thisIsFirstUpdate) {
					sendFirstSPARQLUpdate(roomID, newState, nextTransitionZonedDateTime);					
					thisIsFirstUpdate = false;
				} else {
					sendSPARQLUpdate(roomID, newState, nextTransitionZonedDateTime);
				}
				// TODO remove when code in above comment is removed
				//sendSPARQLUpdate(roomID, newState, nextTransitionZonedDateTime);
			} catch (SIBConnectionErrorException e) {
				throw new FailedToUpdateRoomStateException(e.getMessage(), e);
			}
		}

	}
}
