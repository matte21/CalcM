/**
 * 
 */
package rooms.aggregator.redsib09;

import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rooms.aggregator.RoomsAggregator;
import rooms.aggregator.outcome.EmptyFailure;
import rooms.aggregator.outcome.EmptyOutcome;
import rooms.aggregator.outcome.EmptySuccess;
import rooms.aggregator.redsib09.exceptions.BadExceptionHandlerException;
import rooms.aggregator.redsib09.exceptions.BadOntologyPrefixException;
import rooms.aggregator.redsib09.exceptions.BadRoomIDException;
import rooms.aggregator.redsib09.exceptions.BadSIBIPorHostException;
import rooms.aggregator.redsib09.exceptions.BadSmartspaceNameException;
import rooms.aggregator.redsib09.exceptions.FailedToAcquireNbrOfAvailableSeatsException;
import rooms.aggregator.redsib09.exceptions.FailedToJoinSmartspaceException;
import rooms.aggregator.redsib09.exceptions.badSIBPortNumberException;

import sofia_kp.KPICore;
import sofia_kp.SIBResponse;
import sofia_kp.SSAP_sparql_response;
import sofia_kp.iKPIC_subscribeHandler2;

/**
 * A thread-safe room aggregator implementation for cases where the rooms are stored as RDF triples in a Red SIB v0.9.
 * All communication to the SIB happens through SPARQL. For instance, the aggregation is based on SPARQL subscriptions.
 * 
 * An instance is tied to a SIB endpoint (IP and port), a smart space and to an ontology defining all the study rooms.
 */
public class RedSIB09RoomsAggregator implements RoomsAggregator {
	
	// The IP address or the host name of the SIB
	private final String sibIPorHost;
	
	// The port number of the SIB
	private final int sibPort;
	
	// The name of the smart space where the associated room is 
	private final String smartspaceName;
	
	// The prefix of the ontology defining the Study Rooms
	private final String ontologyPrefix;
	
	// Associates the roomID to the subscription ID for every aggregated room
	private Map<String, String> roomIDtoSubID;
	
	// Used to build log messages
	StringBuffer logMessage; 
	
	// Stores the length of the initial part of log messages, which is the same for all log messages
	private int sharedLogMsgPartLength;
	
	// Handler to be invoked when a subscription experiences an exception
	private final Consumer<String> subExceptionHandler;
	
	// The log4j2 logger used for logging within this class
	private static final Logger LOG = LogManager.getLogger();
	
	
	/**
	 * @param sibIPorHost - IP or host name of the SIB
	 * @param sibPort - port number of the SIB
	 * @param smartspaceName - name of the smart space where the rooms to aggregate reside
	 * @param ontologyPrefix - prefix of the study room ontology
	 *
	 * @throws BadSIBIPorHostException if sibIPorHost is null or empty
	 * @throws BadSIBPortNumberException if sibPort  is not in the range [1, 65535]
	 * @throws BadSmartspaceNameException if smartspaceName is null or empty
	 * @throws BadOntologyPrefixException if ontologyPrefix is null or empty
	 */
	public RedSIB09RoomsAggregator(String sibIPorHost, int sibPort, String smartspaceName, String ontologyPrefix) {
		
		validateInputs(sibIPorHost, sibPort, smartspaceName, ontologyPrefix);
		
		this.sibIPorHost = sibIPorHost;
		this.sibPort = sibPort;
		this.smartspaceName = smartspaceName;
		this.ontologyPrefix = ontologyPrefix;
		
		roomIDtoSubID = new ConcurrentHashMap<String, String>();
		
		// Prepare log message initial part, shared by every log message
		logMessage = new StringBuffer("Aggregator: ");
		
		// Store length of log message initial part so that we can resize it after logging a message to have it
		// ready for the next use.
		sharedLogMsgPartLength = logMessage.length();
		
		// What this handler does is cleaning the data structures associated to a subscription, i.e., remove
		// from roomIDtoSubID the entry associated to the room whose sub experienced an exception. There's
		// no need to perform an unsubscribe because the thread where the subscription handler gets executed
		// is terminated automatically when an exception is thrown.
		subExceptionHandler =  new Consumer<String>() {
			@Override
			public void accept(String roomID) {
				// Check if there's an active sub to the room with ID roomID
				if (roomID != null && roomIDtoSubID.containsKey(roomID.trim())) {
					// Clean data structures associated to the subscription to the room with ID roomID
					roomIDtoSubID.remove(roomID);
					
					// Reset log message builder so that it's ready for use
					logMessage.setLength(sharedLogMsgPartLength);
					
					// Log that the room with ID roomID is not being aggregated
					logMessage.append("subscription to room with ID " + roomID + " has been terminated because the sub" 
									  + " handler received an exception");
					LOG.error(logMessage.toString());
				} else {
					// Reset log message builder so that it's ready for use
					logMessage.setLength(sharedLogMsgPartLength);
					
					// Log that the room with ID roomID is not being aggregated
					logMessage.append("exception handler for a subscription to room with ID " + roomID + " was invoked,"
									  + " but such a room is not currently aggregated.");
					LOG.error(logMessage.toString());
				}
			}
		};
		
	}

	
	private void validateInputs(String sibIPorHost, int sibPort, String smartspaceName, String ontologyPrefix) {
		
		if (sibIPorHost == null || sibIPorHost.trim().isEmpty()) {
			throw new BadSIBIPorHostException(sibIPorHost == null ? "received null for input parameter "
											  + "sibIPorHost" : "received empty input parameter sibIPorHost");
		}
		if (sibPort < 1 || sibPort > 65535) {
			throw new badSIBPortNumberException("SIB port number is not a valid port number. Valid port nbrs are "
												+ "in range [1,65535]");
		}
		if (smartspaceName == null || smartspaceName.trim().isEmpty()) {
			throw new BadSmartspaceNameException(smartspaceName == null ? 
				"received null input parameter smartspaceName": "received empty input parameter smartspaceName");
		}
		if (ontologyPrefix == null || ontologyPrefix.trim().isEmpty()) {
			throw new BadOntologyPrefixException(ontologyPrefix == null ? "received null for input parameter "
					  						+ "ontologyPrefix" : "received empty input parameter ontologyPrefix");
		}
	}
	
	
	/**
	 * @see rooms.aggregator.RoomsAggregator#startAggregatingRoom(java.lang.String) for a high-level description
	 * of the method.
	 * 
	 * Start aggregating the room with ID roomID by performing a SPARQL subscription to its seats state changes.
	 * This method is thread-safe: the only shared and writable state it accesses is thread-safe. 
	 * 
	 * @return EmptyOutcome describing the outcome of the operation. If the subscription is successful or if the
	 * room with ID roomID is already being aggregated, an EmptySuccess is returned. An EmptyFailure is returned
	 * otherwise
	 * 
	 * @throws BadRoomIDException if roomID is null or empty
	 */
	@Override
	public EmptyOutcome startAggregatingRoom(String roomID) {
		
		validateRoomID(roomID);
		
		logRequestToAggregateRoom(roomID);
		
		// Check if roomID is already being aggregated
		if (isAlreadyAggregated(roomID)) {
			return new EmptySuccess(0);
		}
		
		// Get a connection to the SIB
		KPICore sib = new KPICore(sibIPorHost, sibPort, smartspaceName);

		// Join smart space
		EmptyOutcome joinOutcome = joinSmartspace(sib, roomID);
		if (joinOutcome.success()) {
			logSuccessfulJoin(roomID);
		} else {
			return joinOutcome;
		}
	
		// This try is part of a try-finally block. From this point we have joined the smart space, and we want to 
		// leave it eventually, whether the following operations (e.g. subscribe, etc...) fail or not. Thus, we wrap
		// everything in this try clause and leave the smart space in the finally clause
		try {
			// Check room existence
			EmptyOutcome roomExistenceOutcome = checkRoomExistence(sib, roomID);
			if (!roomExistenceOutcome.success()) {
				return roomExistenceOutcome;
			}
			
			// Instantiate handler for subscription. The operation might fail because the handler might fail to join the
			// smart space
			SeatStateHandler handler = null;
			try {
				handler = new SeatStateHandler(subExceptionHandler, sibIPorHost, sibPort, smartspaceName,
											   ontologyPrefix, roomID);
			} catch (FailedToJoinSmartspaceException | FailedToAcquireNbrOfAvailableSeatsException e) {
				return reactToUnsuccessfulHandlerInstantiation(e, roomID);
			} 
			
			return subscribe(roomID, sib, handler);
			
		} finally {
			leaveSmartspace(roomID, sib);
		}
	}

	
	/* 
	 * @param roomID the ID of the room to validate
	 *
	 * @throws BadRoomIDException if roomID is null or empty
	 */
	private void validateRoomID(String roomID) {
		if (roomID == null || roomID.trim().isEmpty()) {
			throw new BadRoomIDException(roomID == null ? "received null for input parameter roomID" 
														: "received empty input parameter roomID");
		}
	}

	
	/*
	 * @param roomID - the ID of the room which will be aggregated
	 */
	private void logRequestToAggregateRoom(String roomID) {
		// Reset log message builder so that it's ready for use
		logMessage.setLength(sharedLogMsgPartLength);
		
		// Log that a request for aggregating a new room was received
		logMessage.append(" received request to aggregate room with ID " + roomID + ".");
		LOG.info(logMessage.toString());
	}
	
	
	/*
	 * A wrapper on the method {@link #isAggregatingRoom(String)} that adds logging
	 * 
	 * @param roomID the ID of the room to check
	 * 
	 * @return true if the room is alredy being aggregated, false otherwise
	 */
	private boolean isAlreadyAggregated(String roomID) {
		// Check if room is already being aggregated
		if (isAggregatingRoom(roomID)) {
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
			
			// Log that the room is already being aggregated
			logMessage.append(" room with ID " + roomID + "is already being aggregated.");
			LOG.info(logMessage.toString());
			
			return true;
		}
		
		return false;
	}

	
	/*
	 * perform a join on the smart space where the room with ID roomID resides
	 * 
	 * @param sib - a connection to the SIB
	 * @param roomID - ID of the room which will be aggregated. Used for logging.
	 *
	 * @return EmptySuccess if the join is successful, EmptyFailure describing what went wrong otherwise
	 */
	private EmptyOutcome joinSmartspace(KPICore sib, String roomID) {
		SIBResponse resp = sib.join();
		if (resp == null || !resp.isConfirmed()) {
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
			
			// Log that join was unsuccessful
			logMessage.append("(de)aggregation of room with ID " + roomID + ": smart space join has failed. SIB message:"
							  + resp == null ? "<no message from SIB>" : resp.Message);
			LOG.debug(logMessage.toString());
			
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
		
			// Return less detailed message to the client
			logMessage.append("(de)aggregation of room with ID " + roomID + ": smart space join has failed.");
			return new EmptyFailure(logMessage.toString(), 1);
		}
		
		return new EmptySuccess(0);
	}
	
	
	/*
	 * @param roomID - ID of the room which is being aggregated, used for logging
	 */
	private void logSuccessfulJoin(String roomID) {
		// Reset log message builder so that it's ready for use
		logMessage.setLength(sharedLogMsgPartLength);
		
		// Log that join was successfully completed
		logMessage.append("aggregation of room with ID " + roomID + ": smart space has been successfully joined.");
		LOG.info(logMessage.toString());
	}
	
	
	/*
	 * @param sib - a connection to the SIB
	 * @param roomID - the ID of the room whose existence is to be assessed
	 * 
	 * @return An EmptySuccess if the room exists, an EmptyFailure if the room does not exist or if it was not possible
	 * to receive a meaningful response from the SIB. The room exists if it is stored in the SIB.
	 */
	private EmptyOutcome checkRoomExistence(KPICore sib, String roomID) {
		// Query the SIB
		SIBResponse resp = sib.querySPARQL(roomExistenceQueryForRoom(roomID));
		
		// Check that a meaningful response was received
		if (resp == null || !resp.isConfirmed()) {
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
			
			// Log debugging details about the fact that the operation failed
			logMessage.append(" failed to contact the SIB. Message received: " 
							  + resp == null ? "<no message from SIB>" : resp.Message);
			LOG.debug(logMessage.toString());		
			
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
			
			// Return failure message with less details to the client
			logMessage.append(" could not make contact with the SIB while assessing room with ID " 
							  + roomID + " existence.");
			return new EmptyFailure(logMessage.toString(), 2);		
			
		}
		
		// Check that the query result contains some results, AKA the room with ID roomID exists
		if (!resp.sparqlquery_results.hasResults()) {
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
			
			// Return a message to the client that the room with the given ID does not exist 
			logMessage.append(" no room with ID " + roomID + " was found in the SIB.");
			return new EmptyFailure(logMessage.toString(), 3);					
		}	
		
		// If we're here the room with ID roomID exists
		return new EmptySuccess(0);
	}

	
	/*
	 * @param roomID - the ID of the room whose existence the return parameter must check
	 * 
	 * @return A String representing a SPARQL query which yields results if and only if the SIB stores a room
	 * with ID roomID
	 */
	private String roomExistenceQueryForRoom(String roomID) {
		// No specific reason to use this exact query. Any query which yields results if and only if 
		// the room with ID roomID is stored in the SIB works.
		return "PREFIX ns:<" + ontologyPrefix + "> \n"
				+ "SELECT ?predicate \n"
				+ "WHERE { \n"
				+ "ns:" + roomID + " ?predicate ?object \n"
				+ "}";
	}
	
	
	/*
	 * Return an EmptyFailure describing what caused the handler instantiation to fail
	 * 
	 * @param e - the Exception which caused the handler instantiation to fail.
	 * @param roomID - the ID of the room the handler which could not be instantiated was meant to aggregate
	 * 
	 * @return EmptyFailure describing what went wrong
	 */
	private EmptyOutcome reactToUnsuccessfulHandlerInstantiation(Exception e, String roomID) {
		// Reset log message builder so that it's ready for use
		logMessage.setLength(sharedLogMsgPartLength);

		// Return a message to the client explaining that the handler could not be instantiated
		logMessage.append("failed to instantiate handler for room with ID " + roomID + ". Received " 
						  + e.getClass().getName()+ " from handler constructor. Exception message: " 
						  + e.getLocalizedMessage());	
		return new EmptyFailure(logMessage.toString(), 4);
	}
	

	/*
	 * Do a SPARQL subscription to seats state changes of room with ID roomID and fill associated data structures
	 * 
	 * @param roomID - the ID of the room whose seats state changes we're trying to subscribe to
	 * @param sib - a connection to the SIB
	 * @param handler - the handler to associate to the subscription
	 * 
	 * @return EmptySuccess if the subscription was successful, EmptyFailure otherwise.
	 */
	private EmptyOutcome subscribe(String roomID, KPICore sib, SeatStateHandler handler) {
		// perform subscription
		SIBResponse resp = sib.subscribeSPARQL(handler.getSubquery(), handler);
		
		if (resp == null || !resp.isConfirmed()) {
			// This method forces the handler to release its resources (i.e. connections, etc...)
			handler.kpic_UnsubscribeEventHandler(null);
			
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
			
			// Log details about failed sub
			logMessage.append(" failed to subscribe to SIB while attempting to aggregate room with ID " + roomID + "."
								+ " Message from SIB: " + resp == null ? "<No message from SIB>" : resp.Message);
			LOG.debug(logMessage.toString());
			
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
			
			// Return less detailed message to the client
			logMessage.append(" failed to subscribe to SIB while attempting to aggregate room with ID " + roomID + ".");
			return new EmptyFailure(logMessage.toString(), 5);
		}
		
		// fill Map which stores room IDs and subscription IDs
		roomIDtoSubID.put(roomID, resp.subscription_id);
		
		// Reset log message builder so that it's ready for use
		logMessage.setLength(sharedLogMsgPartLength);
		
		// Log successful aggregation
		logMessage.append(" room with ID " + roomID + " is now being successfully aggregated.");
		LOG.info(logMessage.toString());
		
		return new EmptySuccess(0);
	}
	
	
	/*
	 * Leave the smart space the sib input parameter is part of. It assumes that a successful join has been invoked
	 * on sib.
	 * 
	 * @param roomID - used for logging
	 * @param sib - the connection to the SIB to use to leave the smart space
	 */
	private void leaveSmartspace(String roomID, KPICore sib) {
		SIBResponse resp = sib.leave();
		
		if (resp == null || !resp.isConfirmed()) {
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
			
			// Log that the leave could not be performed
			logMessage.append("attempt to leave the smart space failed.");
			LOG.error(logMessage.toString());
			
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
			
			// Log more details for debugging
			logMessage.append("attempt to leave the smart space failed with this message from the SIB: " 
							  + resp == null ? "<no message from SIB>" : resp.Message);
			LOG.debug(logMessage.toString());
		} else {
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
			
			// Log that the leave has been performed
			logMessage.append("smart space has been successfully left.");
			LOG.info(logMessage.toString());
		}
	}


	/* 
	 * @see rooms.aggregator.RoomsAggregator#isAggregatingRoom(java.lang.String)
	 */
	@Override
	public boolean isAggregatingRoom(String roomID) {
		return roomIDtoSubID.containsKey(roomID.trim());
	}
	
		
	/** 
	 * @see rooms.aggregator.RoomsAggregator#stopAggregatingRoom(java.lang.String) for a high-level description.
	 * 
	 * Stop aggregating the room with ID roomID by performing a SPARQL unsubscription to the SIB and cleaning up
	 * the appropriate data structures by deleting info about the sub. If the room with ID roomID is not being
	 * aggregated, an EmptyFailure is returned.
	 * 
	 * This method is thread-safe: all the writable shared state it accesses is thread-safe.
	 * 
	 * @throws BadRoomIDException if roomID is null or empty
	 */
	@Override
	public EmptyOutcome stopAggregatingRoom(String roomID) {
		
		validateRoomID(roomID);
		
		logRequestToStopAggregatingRoom(roomID);
		
		// Check if roomID is being aggregated
		EmptyOutcome currentlyAggregatedOutcome = isCurrentlyAggregated(roomID);
		if (!currentlyAggregatedOutcome.success()) {
			return currentlyAggregatedOutcome;
		}
		
		// Get connection to the SIB
		KPICore sib = new KPICore(sibIPorHost, sibPort, smartspaceName);
		
		// join smart space
		EmptyOutcome joinOutcome = joinSmartspace(sib, roomID);
		if (!joinOutcome.success()) {
			return joinOutcome;
		}
		
		// This try is part of a try-finally block. From this point we have joined the smart space, and we want to 
		// leave it eventually, whether the following unsubscribe fails or not. Thus, we wrap everything in this try 
		// clause and leave the smart space in the finally clause
		try {
			// Perform unsub
			return unsubscribe(sib, roomID);
		} finally {
			leaveSmartspace(roomID, sib);
		}
	}	



	/*
	 * @param roomID - the ID of the room which will no longer be aggregated
	 */
	private void logRequestToStopAggregatingRoom(String roomID) {
		// Reset log message builder so that it's ready for use
		logMessage.setLength(sharedLogMsgPartLength);
		
		// Log that a request for aggregating a new room was received
		logMessage.append(" received request to stop aggregating room with ID " + roomID + ".");
		LOG.info(logMessage.toString());
	}
	
	
	/*
	 * A method that checks if the room is currently being aggregated and performs some logging
	 * 
	 * @param roomID the ID of the room to check
	 * 
	 * @return EmptySuccess if the room is currently being aggregated, an EmptyFailure otherwise
	 */
	private EmptyOutcome isCurrentlyAggregated(String roomID) {
		// Check if room is already being aggregated
		if (!isAggregatingRoom(roomID)) {
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
			
			// Log that the room is already being aggregated
			logMessage.append(" room with ID " + roomID + " is not being aggregated.");
			return new EmptyFailure(logMessage.toString(), 6);
		}
		
		return new EmptySuccess(0);
	}

	/*
	 * Do a SPARQL unsubscription to seats state changes of room with ID roomID and clean up associated data structures
	 * 
	 * @param roomID - the ID of the room whose seats state changes we're trying to unsubscribe from
	 * @param sib - a connection to the SIB
	 * 
	 * @return EmptySuccess if the unsubscription was successful, EmptyFailure otherwise.
	 */
	private EmptyOutcome unsubscribe(KPICore sib, String roomID) {
		SIBResponse resp = sib.unsubscribe(roomIDtoSubID.get(roomID));
		
		// Check if unsub was unsuccessful
		if (resp == null || !resp.isConfirmed()) {
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
			
			// Log details about failed unsub
			logMessage.append("failed to unsubscribe to SIB while attempting to stop aggregating room with ID " 
							  + roomID + ". Message from SIB: " + resp == null ? "<No message from SIB>" : resp.Message);
			LOG.debug(logMessage.toString());
			
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
			
			// Return less detailed message to the client
			logMessage.append("failed to unsubscribe to SIB while attempting to stop aggregating room with ID " 
							  + roomID + ".");
			return new EmptyFailure(logMessage.toString(), 7);
		}
		
		// Clean up data about the subscription from data structures
		roomIDtoSubID.remove(roomID);
		
		// Reset log message builder so that it's ready for use
		logMessage.setLength(sharedLogMsgPartLength);
		
		// Log successful unsub
		logMessage.append("successfully stopped aggregating room with ID " + roomID + ".");
		LOG.info(logMessage.toString());
		
		return new EmptySuccess(0);
	}
	
	
	/**
	 * A subscription handler for reacting to Seats State changes in a room. 
	 * Whenever a seat state changes in the associated room, the number of 
	 * available seats in that room is computed and the corresponding value 
	 * in the SIB is updated. If any error occurs during this process (i.e. 
	 * an exception is received from the SIB) appropriate clean up and
	 * recovery actions are taken. 
	 */
	public static class SeatStateHandler implements iKPIC_subscribeHandler2 {

		// The callback to be used to react to exceptions
		private final Consumer<String> reactToExceptionDuringSub;
		
		// The IP address or the host name of the SIB
		private final String sibIPorHost;
		
		// The port number of the SIB
		private final int sibPort;
		
		// The name of the smart space where the associated room is 
		private final String smartspaceName;
		
		// The prefix of the ontology defining the Study Rooms
		private final String ontologyPrefix;
		
		// The Unique ID of the study room this handler aggregates. The URI of this room is given by 
		// ontologyPrefix:roomID
		private final String roomID;

		// The SPARQL subscription query to use for subscriptions where this class is used as a handler
		private final String subQuery;
		
		// The connection to the SIB
		private final KPICore sib;
		
		// Used to build log messages
		StringBuffer logMessage; 
		
		// Stores the length of the initial part shared by all log messages
		private int sharedLogMsgPartLength;
		
		// The log4j2 logger used for logging within this class
		private static final Logger LOG = LogManager.getLogger(); 
		
		// The nbr of currently available seats in the room with ID roomID
		private int availableSeats;
				
		
		/**
		 * The input parameters of type String must be non-null and non-empty, otherwise appropriate 
		 * IllegalArgumentExceptions are thrown. The input parameter sibPort must be a valid port 
		 * number, in the range [1, 65535], otherwise an IllegalArgumentException is thrown. The 
		 * input parameter reactToExceptionDuringSub must be non-null, otherwise an IllegalArgument
		 * exception is thrown.
		 * 
		 * @param reactToExceptionDuringSub - callback to be used to react to exceptions
		 * @param sibIPorHost - IP address or the host name of the SIB
		 * @param sibPort - port number of the SIB
		 * @param smartspaceName - name of the smart space where the associated room is 
		 * @param ontologyPrefix - prefix of the ontology defining the Study Rooms
		 * @param roomID - Unique ID of the study room this handler aggregates
		 * 
		 * @throws FailedToJoinSmartspaceException - if the smart space where the room resides
		 * cannot be joined
		 * @throws FailedToAcquireNbrOfAvailableSeatsException - if it was not possible 
		 * to query the SIB for the number of available seats 
		 */
		public SeatStateHandler(Consumer<String> reactToExceptionDuringSub, String sibIPorHost, int sibPort,
								String smartspaceName, String ontologyPrefix, String roomID) 
																				throws FailedToJoinSmartspaceException, FailedToAcquireNbrOfAvailableSeatsException {
			
			validateInputs(reactToExceptionDuringSub, sibIPorHost, sibPort, smartspaceName, ontologyPrefix, roomID);
			
			this.reactToExceptionDuringSub = reactToExceptionDuringSub;
			this.sibIPorHost = sibIPorHost;
			this.sibPort = sibPort;
			this.smartspaceName = smartspaceName;
			this.ontologyPrefix = ontologyPrefix;
			this.roomID = roomID;
			
			// Prepare log message initial part, shared by every log message.
			logMessage = new StringBuffer("Handler for room with ID " + roomID + ": ");
			
			// Store length of log message initial part so that we can resize it after logging a message to have it
			// ready for the next use.
			sharedLogMsgPartLength = logMessage.length();
			
			subQuery = buildSubQuery();
			
			// Get a connection to the SIB smart space where the room with ID roomID is
			sib = joinSmartspace();
			
			availableSeats = computeInitiallyAvailableSeats();
		}

		
		private int computeInitiallyAvailableSeats() throws FailedToAcquireNbrOfAvailableSeatsException {
			String availSeatsVarName = "availSeats"; 
			String availSeatsQuery = "PREFIX ns:<" + ontologyPrefix + "> \n" 
									+ "SELECT (COUNT(?seat) AS ?" + availSeatsVarName + ") \n "
									+ "WHERE { \n" 
									+ "ns:" + roomID + " ns:table ?table . \n" 
									+ "?table ns:seat ?seat . \n " 
									+ "?seat ns:seatState ?state \n"  
									+ "FILTER(?state = ns:available) \n" 
									+ "}";

			SIBResponse resp = sib.querySPARQL(availSeatsQuery);
			
			if (resp == null || !resp.isConfirmed()) {
				// Reset log message builder so that it's ready for use
				logMessage.setLength(sharedLogMsgPartLength);
				
				// Log message about unsuccessful acquisition of initial nbr of available seats
				logMessage.append("failed to query the sib to acquire initial number of available Seats. Message from"
								  + " SIB:" + resp == null ? "<no message from sib>" : resp.Message);
				LOG.debug(logMessage.toString());
				
				// Reset log message builder so that it's ready for use
				logMessage.setLength(sharedLogMsgPartLength);
				
				// Return less detailed message to client
				logMessage.append("failed to query the sib to acquire initial number of available Seats.");
				throw new FailedToAcquireNbrOfAvailableSeatsException(logMessage.toString());
			}
			
			return Integer.parseInt(resp.sparqlquery_results.getResultsForVar(availSeatsVarName).firstElement()[2]);
		}


		private void validateInputs(Consumer<String> reactToExceptionDuringSub, String sibIPorHost, int sibPort,
				String smartspaceName, String ontologyPrefix, String roomID) {
			
			if (reactToExceptionDuringSub == null) {
				throw new BadExceptionHandlerException("received null for input parameter reactToExceptionDuringSub");
			}
			if (sibIPorHost == null || sibIPorHost.trim().isEmpty()) {
				throw new BadSIBIPorHostException(sibIPorHost == null ? "received null for input parameter "
												  + "sibIPorHost" : "received empty input parameter sibIPorHost");
			}
			if (sibPort < 1 || sibPort > 65535) {
				throw new badSIBPortNumberException("SIB port number is not a valid port number. Valid port nbrs are "
													+ "in range [1,65535]");
			}
			if (smartspaceName == null || smartspaceName.trim().isEmpty()) {
				throw new BadSmartspaceNameException(smartspaceName == null ? 
					"received null input parameter smartspaceName": "received empty input parameter smartspaceName");
			}
			if (ontologyPrefix == null || ontologyPrefix.trim().isEmpty()) {
				throw new BadOntologyPrefixException(ontologyPrefix == null ? "received null for input parameter "
						  						+ "ontologyPrefix" : "received empty input parameter ontologyPrefix");
			}
			if (roomID == null || roomID.trim().isEmpty()) {
				throw new BadRoomIDException(roomID == null ? "received null for input parameter roomID" 
											 : "received empty input parameter roomID");
			}
		}

		
		private String buildSubQuery() {
			return "PREFIX ns:<" + ontologyPrefix + "> \n"
				 + "SELECT ?seat \n"
				 + "WHERE { \n"
				 + "ns:" + roomID + " ns:table " + "?table . \n"
				 + "?table ns:seat ?seat . \n"
				 + "?seat ns:seatState ?state \n" 
				 + "FILTER(?state = ns:available) \n"
				 + "}";
		}

		
		/*
		 * Instantiate a KPICore and join the smart space associated to this handler
		 * 
		 * @return a {@link sofia_kp.KPICore KPICore} instance connected to the smart
		 * space associated with the room aggregated by this handler
		 */
		private KPICore joinSmartspace() throws FailedToJoinSmartspaceException {
			// Instantiate KPICore
			KPICore sib = new KPICore(sibIPorHost, sibPort, smartspaceName);
			
			// Join smart space
			SIBResponse resp = sib.join();
			
			// Check result
			if (resp != null && resp.isConfirmed()) {
				// Reset log message builder so that it's ready for use
				logMessage.setLength(sharedLogMsgPartLength);
				
				// Log message about successful join
				logMessage.append("successfully joined smart space");
				LOG.info(logMessage.toString());
			} else {
				// Reset log message builder so that it's ready for use
				logMessage.setLength(sharedLogMsgPartLength);
				
				// Log detailed message about failed join
				logMessage.append(
						" failed smart space join. Message from SIB: " + resp == null ? "<no message received from SIB>"
								: resp.Message);
				LOG.debug(logMessage.toString());

				// Reset log message builder so that it's ready for use
				logMessage.setLength(sharedLogMsgPartLength);
				
				// Prepare less detailed error message for client
				logMessage.append("failed to join smart space before performing the update.");
				
				throw new FailedToJoinSmartspaceException(logMessage.toString());				
			}

			return sib;
		}
		
		
		/**
		 * @see sofia_kp.iKPIC_subscribeHandler2#kpic_ExceptionEventHandler(java.lang.Throwable) for a high-level
		 * description.
		 * 
		 * Leave the smart space and notify the aggregator which started the subscription that an exception occurred.
		 */
		@Override
		public void kpic_ExceptionEventHandler(Throwable exception) {
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
			
			// Log that an exception was received
			logMessage.append("exception " + exception.getClass().getName() + " received. Message: " 
							  + exception.getLocalizedMessage() + ".");
			LOG.error(logMessage.toString());
			
			// Leave the smart space
			SIBResponse resp = sib.leave();
			if (resp != null && resp.isConfirmed()) {
				// Reset log message builder so that it's ready for use
				logMessage.setLength(sharedLogMsgPartLength);
				
				// Log that the leave succeeded
				logMessage.append("smart space successfully left after exception.");
				LOG.info(logMessage.toString());
			} else {
				// Reset log message builder so that it's ready for use
				logMessage.setLength(sharedLogMsgPartLength);
				
				// Log that the leave failed
				logMessage.append("failed to leave smart space after exception.");
				LOG.error(logMessage.toString());
				
				// Reset log message builder so that it's ready for use
				logMessage.setLength(sharedLogMsgPartLength);
					
				// Log more details about failure
				logMessage.append(" failed leave. Message from SIB: " + resp == null ? "<no message received from SIB>"
									: resp.Message);
				LOG.debug(logMessage.toString());				
			}
			
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
			
			// React to the exception by invoking the function passed by the aggregator which started the subscription
			logMessage.append("a reaction to the exception will be taken by invoking the associated callback.");
			LOG.info(logMessage.toString());
			reactToExceptionDuringSub.accept(roomID);
		}

		
		/**
		 * @see sofia_kp.iKPIC_subscribeHandler2#kpic_RDFEventHandler(java.util.Vector, java.util.Vector, 
		 * java.lang.String, java.lang.String) for a high-level description.
		 * 
		 * This class only works with SPARQL, thus this method is not needed and is left unimplemented.
		 */
		@Override
		public void kpic_RDFEventHandler(Vector<Vector<String>> arg0, Vector<Vector<String>> arg1, String arg2,
				String arg3) {}

		
		/**
		 * @see sofia_kp.iKPIC_subscribeHandler2#kpic_SPARQLEventHandler(sofia_kp.SSAP_sparql_response, 
		 * sofia_kp.SSAP_sparql_response, java.lang.String, java.lang.String) for a high-level description.
		 * 
		 * Update the number of available seats stored in the SIB whenever a seat state in the associated
		 * room changes. In case the update to the SIB fails, nothing is done, even if this leaves the data 
		 * in the SIB in an inconsistent state: the number of available seats in the room is wrong. Nothing 
		 * is done because there's the hope that future seats state changes in the room will lead to successful 
		 * updates, thus data in the SIB will go back to a consistent state. THIS IS BRITTLE, AND SHOULD BE
		 * REPLACED WITH SOMETHING MORE ROBUST.
		 */
		@Override
		public void kpic_SPARQLEventHandler(SSAP_sparql_response newResults, SSAP_sparql_response oldResults, 
				String indSeq, String subID) {
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
			
			// Log that a new event was received.
			logMessage.append("received an event.");
			LOG.info(logMessage.toString());
			
			// Build SPARQL update
			updateSeatsNbr(newResults, oldResults);
			String sparqlUpdate = buildSPARQLUpdate(availableSeats);
			
			// Perform the update
			SIBResponse resp = sib.update_sparql(sparqlUpdate);
			if (resp != null && resp.isConfirmed()) {
				// Reset log message builder so that it's ready for use
				logMessage.setLength(sharedLogMsgPartLength);
				
				// Log that the update succeeded
				logMessage.append("updated new available seats number to the SIB.");
				LOG.error(logMessage.toString());
				
				// Reset log message builder so that it's ready for use
				logMessage.setLength(sharedLogMsgPartLength);
				
				// Log more details for debugging
				logMessage.append(" new number of available seats updated to the SIB: " + availableSeats);
				LOG.debug(logMessage.toString());
			} else {
				// Reset log message builder so that it's ready for use
				logMessage.setLength(sharedLogMsgPartLength);
				
				// Log that the update failed
				logMessage.append("failed to update new available seats number to the SIB.");
				LOG.error(logMessage.toString());
					
				// Reset log message builder so that it's ready for use
				logMessage.setLength(sharedLogMsgPartLength);
					
				// Log more details about the failure
				logMessage.append(" failed update. Message from SIB: " + resp == null ? "<no message received from SIB>"
									: resp.Message);
				LOG.debug(logMessage.toString());				
			}
		}
		
		
		private void updateSeatsNbr(SSAP_sparql_response newResults, SSAP_sparql_response oldResults) {
			if (newResults != null) {
				availableSeats += newResults.resultsNumber();
			}
			
			if (oldResults != null) {
				availableSeats -= oldResults.resultsNumber();
			}
		}


		private String buildSPARQLUpdate(int availableSeats) {
			return "PREFIX ns:<" + ontologyPrefix + "> \n"
				 + "DELETE { \n"
				 + "ns:" + roomID + " ns:availableSeats " + "?staleAvailableSeats \n"
				 + "} \n"
				 + "INSERT { \n"
				 + "ns:" + roomID + " ns:availableSeats " + availableSeats + "\n"
				 + "}"
				 + "WHERE { \n"
				 + "ns:" + roomID + " ns:availableSeats " + "?staleAvailableSeats \n"
				 + "}";
		}

		
		/**
		 * @see sofia_kp.iKPIC_subscribeHandler2#kpic_UnsubscribeEventHandler(java.lang.String) for a high-level
		 * description.
		 * 
		 * Log that the unsub occurred and leave the smart space
		 */
		@Override
		public void kpic_UnsubscribeEventHandler(String subID) {
			// Reset log message builder so that it's ready for use
			logMessage.setLength(sharedLogMsgPartLength);
			
			// Log that the unsub message was received
			LOG.info(logMessage.append("unsub message received. About to leave smart space").toString());
			
			// Leave the smart space
			SIBResponse resp = sib.leave();
			if (resp != null && resp.isConfirmed()) {
				// Reset log message builder so that it's ready for use
				logMessage.setLength(sharedLogMsgPartLength);
				
				// Log that the leave succeeded
				logMessage.append("smart space successfully left after unsub.");
				LOG.info(logMessage.toString());
			} else {
				// Reset log message builder so that it's ready for use
				logMessage.setLength(sharedLogMsgPartLength);
				
				// Log that the leave failed
				logMessage.append("failed to leave smart space after unsub.");
				LOG.error(logMessage.toString());
					
				// Reset log message builder so that it's ready for use
				logMessage.setLength(sharedLogMsgPartLength);
					
				// Log more details about failure
				logMessage.append(" failed leave. Message from SIB: " + resp == null ? "<no message received from SIB>"
									: resp.Message);
				LOG.debug(logMessage.toString());				
			}
		}
		
		
		/**
		 * @return A String representing the SPARQL subscription query to be used with this handler.
		 */
		public String getSubquery() {
			return subQuery;
		}
	
	}
	
}
