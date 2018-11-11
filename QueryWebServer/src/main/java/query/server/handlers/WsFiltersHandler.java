package query.server.handlers;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.studyroom.web.Session;
import org.studyroom.web.WebSocketHandler;

import fi.iki.elonen.NanoWSD.WebSocketFrame;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;
import query.server.exceptions.SIBConnectionErrorException;
import query.server.utils.SibConnectionFactory;
import query.server.utils.query.PrefixedQueryUtilsFactory;
import query.server.utils.query.PrefixedStudyroomQueryBuilder;
import query.server.utils.query.StudyroomQueryResultsParser;
import sofia_kp.KPICore;
import sofia_kp.SIBResponse;
import sofia_kp.SSAP_sparql_response;
import sofia_kp.iKPIC_subscribeHandler2;

public class WsFiltersHandler extends WebSocketHandler implements iKPIC_subscribeHandler2{

	private final static Logger LOG = LogManager.getLogger();

	private static final String SIB_CONN = "sib_conn";
	private static final String SUB_ID = "sub_id";
	private static final String ROOM_ID_KEY = "roomID";
	
	private static final String NEXT_TRANSITION_INSTANT_VAR_NAME = "nextTransitionInstant"; 
	
	private static final String XSD_DATE_TIMESTAMP_PATTERN = "uuuu-MM-dd'T'HH:mm:ssxxx";  
	private static final DateTimeFormatter XSD_DATE_TIMESTAMP_FORMATTER = 
																	DateTimeFormatter.ofPattern(XSD_DATE_TIMESTAMP_PATTERN);
	
	private Consumer<StudyroomQueryResultsParser> applyIngressFilters;
	private Consumer<StudyroomQueryResultsParser> applyEgressFilters;
	
	// TODO need to clean up additional resources. Both onClose and onMessage
	private final ScheduledExecutorService schedulerOfRemovals = Executors.newScheduledThreadPool(1);
	private Map<String, ScheduledFuture<?>> roomIDxRemovalTask;
	
	@Override
	protected void onClose(CloseCode closeCode, String reason, boolean closedByRemote) {
		String logMsg = "web socket connection closed with code " 
				+ closeCode 
				+ ". Reason: " 
				+ reason 
				+ ". Closed by remote: " 
				+ closedByRemote;
		LOG.info(logMsg);
		
		// TODO stop previous removals
		LOG.debug("Stopping all removal tasks");
		stopAllRoomsRemovalTasks();
		LOG.debug("Stopped all removal tasks");

		
		Session s = super.getSession();
		KPICore sib = (KPICore) s.get(SIB_CONN);
		
		closeConnectionToSIBAndLog(sib);
		
		LOG.debug("Cleaning up session resources.");
		s.remove(SUB_ID);
		s.remove(SIB_CONN);
		LOG.debug("Session resources cleaned up.");
	}

	private void closeConnectionToSIBAndLog(KPICore sib) {
		LOG.debug("Closing connection to SIB...");
		if ( cleanUpSIB(sib) ) {
			LOG.debug("Connection to SIB closed.");			
		}
	}
	
	private boolean cleanUpSIB(KPICore sib) {
		if (subID != null) {			
			sib.unsubscribe(subID);				
		}
		SIBResponse sibResp = sib.leave();
		if (sibResp == null || !sibResp.isConfirmed()) {
			LOG.debug("SIB: could not leave smart space.");
			return false;
		}
		return true;
	}

	@Override
	protected void onMessage(WebSocketFrame msg) {
		// TODO stop previous removals
		LOG.debug("Stopping all removal tasks");
		stopAllRoomsRemovalTasks();
		LOG.debug("Stopped all removal tasks");

		Session s = super.getSession();
		if (!s.containsKey(SIB_CONN)) {
			addNewSIBConnToSession(s);
		}
		KPICore sib = (KPICore) s.get(SIB_CONN);
		
		String normalizedPayload = msg.getTextPayload().trim().toLowerCase(); 
		if (normalizedPayload.equals("stop")) {
			LOG.info("Received request to stop sending notifications.");
			if (subID != null) {
				sib.unsubscribe((String) s.get(SUB_ID));
				s.remove(SUB_ID);
			}
			return;
		}
		
		LOG.info("Received request for notifications.");

		LOG.debug("Building SPARQL query for filters: " + msg.getTextPayload());
		String sparqlQuery = buildSPARQLQueryFromParams(msg.getTextPayload());
		LOG.debug("SPARQL query for filters: " + msg.getTextPayload() + " built. SPARQL query:\n" + sparqlQuery);
		
		LOG.debug("Building ingress filter function.");
		applyIngressFilters = buildIngressFilters(msg.getTextPayload());
		LOG.debug("Built ingress filter function.");
		
		LOG.debug("Building egress filter function.");
		applyEgressFilters = buildEgressFilters(msg.getTextPayload());
		LOG.debug("Built egress filter function.");
		
		LOG.debug("Starting SPARQL subscription for filters: " + msg.getTextPayload());
		SIBResponse sibResp = sib.subscribeSPARQL(sparqlQuery, this);
		if (sibResp == null || !sibResp.isConfirmed()) {
			LOG.info("Failed to start SPARQL subscription for filters " + msg.getTextPayload());
			closeConnectionToSIBAndLog(sib);
		}
		LOG.info("Started SPARQL subscription for filters: " + msg.getTextPayload());
	
		s.set(SUB_ID, sibResp.subscription_id);
	}

	private synchronized void stopAllRoomsRemovalTasks() {
		if (roomIDxRemovalTask != null) {
			for (String roomID : roomIDxRemovalTask.keySet()) {
				roomIDxRemovalTask.get(roomID).cancel(false);
				roomIDxRemovalTask.remove(roomID);
			}
		}
	}

	private void addNewSIBConnToSession(Session s) {
		LOG.debug("Acquiring connection to the SIB...");
		KPICore sib = null;
		try {
			sib = SibConnectionFactory.getInstance().getSIBConnection();
			LOG.debug("Connection to the SIB opened.");
		} catch (SIBConnectionErrorException e) {
			LOG.debug("Failed to acquire connection to the SIB.\n" + e.getLocalizedMessage());
			return;
		}
		
		LOG.debug("Injecting sib connection into session " + subID.toString());
		s.set(SIB_CONN, sib);
		LOG.debug("SIB connection injected into session");
	}

	@Override
	protected void onOpen() {
		LOG.info("Opened web socket connection.");
	}
	
	private String buildSPARQLQueryFromParams(String queryParamsString) {
		PrefixedStudyroomQueryBuilder queryBuilder = PrefixedQueryUtilsFactory.getInstance()
																			  .getPrefixedQueryBuilder();
		queryBuilder = queryBuilder.selectRoomID()
						   		   .selectAvailSeats()
						   		   .selectCapacity()
						   		   .selectState()
						   		   .selectUniversity()
						   		   .selectAddress()
						   		   .selectNextTransitionInstantAndPredicate();
		 
		if (queryParamsString != null && !queryParamsString.trim().isEmpty()) {
			String[] queryParams = queryParamsString.split("&");
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
					case "nearSeats":
						int nbrOfNearSeats = Integer.parseInt(paramValue);
						if (nbrOfNearSeats == 2) {
							queryBuilder = queryBuilder.addWhereFilterOnTwoNearSeats();							
						}
						if (nbrOfNearSeats == 3) {
							queryBuilder = queryBuilder.addWhereFilterOnThreeNearSeats();
						}
				}
			}
		}
		
		return queryBuilder.getQuery();
	}

	private Consumer<StudyroomQueryResultsParser> buildIngressFilters(String queryParamsString) {
		List<Consumer<StudyroomQueryResultsParser>> allFilters = new ArrayList<Consumer<StudyroomQueryResultsParser>>();
		
		if (queryParamsString != null && !queryParamsString.trim().isEmpty()) {
			String[] queryParams = queryParamsString.split("&");
			for (String param : queryParams) {
				String[] paramNameAndValue = param.split("=");
				String paramName = paramNameAndValue[0];
				String paramValue = paramNameAndValue[1];
				
				if (paramName.equalsIgnoreCase("openForAtLeast")) {
					int minutes = Integer.parseInt(paramValue);
					roomIDxRemovalTask = new HashMap<String, ScheduledFuture<?>>();
					Consumer<StudyroomQueryResultsParser> openForAtLeastFilter = new Consumer<StudyroomQueryResultsParser>() {
						@Override
						public void accept(StudyroomQueryResultsParser resultsParser) {
							resultsParser.roomsInCurrentStateForAtLeast(minutes);
							setRemovalOfRooms(resultsParser, minutes);
						}
					};
					allFilters.add(openForAtLeastFilter);
				}
			}
		}
		
		return new Consumer<StudyroomQueryResultsParser>() {
			@Override
			public void accept(StudyroomQueryResultsParser resultsParser) {
				for (Consumer<StudyroomQueryResultsParser> filter : allFilters) {
					filter.accept(resultsParser);
				}
			}
		};
	}
	

	private Consumer<StudyroomQueryResultsParser> buildEgressFilters(String queryParamsString) {
		List<Consumer<StudyroomQueryResultsParser>> allFilters = new ArrayList<Consumer<StudyroomQueryResultsParser>>();
		
		if (queryParamsString != null && !queryParamsString.trim().isEmpty()) {
			String[] queryParams = queryParamsString.split("&");
			for (String param : queryParams) {
				String[] paramNameAndValue = param.split("=");
				String paramName = paramNameAndValue[0];
				
				if (paramName.equalsIgnoreCase("openForAtLeast")) {
					Consumer<StudyroomQueryResultsParser> onlyDisplayedRoomsFilter =
																		new Consumer<StudyroomQueryResultsParser>() {
						@Override
						public void accept(StudyroomQueryResultsParser resultsParser) {
							Set<Map<String, String>> openRooms = resultsParser.onlyOpenRooms()
																			  .getResults();
							for (Map<String, String> anOpenRoom : openRooms) {
								String roomID = anOpenRoom.get(ROOM_ID_KEY);
								synchronized (this) {
									if (isDisplayed(roomID)) {
										removeRemovalTask(roomID);
									} else {
										resultsParser.removeRoomWithID(roomID);
									}	
								}
							}
							resultsParser.resetFilters();
							Set<Map<String, String>> closedRooms = resultsParser.onlyClosedRooms().getResults();
							for (Map<String, String> aClosedRoom : closedRooms) {
								String roomID = aClosedRoom.get(ROOM_ID_KEY);
								resultsParser.removeRoomWithID(roomID);
							}
						}
					};
					allFilters.add(onlyDisplayedRoomsFilter);
				}
			}
		}
		
		return new Consumer<StudyroomQueryResultsParser>() {
			@Override
			public void accept(StudyroomQueryResultsParser resultsParser) {
				for (Consumer<StudyroomQueryResultsParser> filter : allFilters) {
					filter.accept(resultsParser);
				}
			}
		};
	}
	
	private void setRemovalOfRooms(StudyroomQueryResultsParser resultsParser, int minutes) {
		Set<Map<String, String>> roomsToBeRemoved = resultsParser.getResults();
		for (Map<String, String> aRoomToBeRemoved : roomsToBeRemoved) {
			String roomID = aRoomToBeRemoved.get(ROOM_ID_KEY);			
			String nextTransitionDateTimestampString = aRoomToBeRemoved.get(NEXT_TRANSITION_INSTANT_VAR_NAME);
			TemporalAccessor nextTransitionDateTimestamp = 
					XSD_DATE_TIMESTAMP_FORMATTER.parse(nextTransitionDateTimestampString);
			long nextTransEpochSeconds = Instant.from(nextTransitionDateTimestamp).getEpochSecond();
			long nowEpochSeconds = Instant.now().getEpochSecond();
			long delay = nextTransEpochSeconds - nowEpochSeconds - (minutes*60);
			synchronized (this) {
				ScheduledFuture<?> removalTask = schedulerOfRemovals.schedule(new Runnable() {
					@Override
					public void run() {
						JSONObject notificationJSON = new JSONObject();
						Set<Map<String, String>> singleRoomSet = new HashSet<Map<String, String>>();
						singleRoomSet.add(aRoomToBeRemoved);
						notificationJSON.put("toRemove", singleRoomSet);
						sendNotification(notificationJSON.toString());
						roomIDxRemovalTask.remove(roomID);
					}
				}, delay, TimeUnit.SECONDS);
				roomIDxRemovalTask.put(roomID, removalTask);	
			}
		}
	}

	private boolean isDisplayed(String roomID) {
		return roomIDxRemovalTask.containsKey(roomID);
	}
	
	private void removeRemovalTask(String roomID) {
		boolean doNotInterrupt = true;
		roomIDxRemovalTask.get(roomID).cancel(doNotInterrupt);
		roomIDxRemovalTask.remove(roomID);
	}
	
	@Override
	public void kpic_ExceptionEventHandler(Throwable exception) {
		Session s = super.getSession();
		KPICore sib = (KPICore) s.get(SIB_CONN);
		
		LOG.debug("Exception while processing notification from the SIB: " + exception.getMessage());
		closeConnectionToSIBAndLog(sib);
		super.close(CloseCode.InternalServerError,"exception during SPARQL notification processing", true);
	}

	@Override
	public void kpic_RDFEventHandler(Vector<Vector<String>> arg0, Vector<Vector<String>> arg1, String arg2,
			String arg3) {
		// this method is unimplemented because we never use RDF subscriptions
	}

	@Override
	public void kpic_SPARQLEventHandler(SSAP_sparql_response newResults,
			SSAP_sparql_response oldResults, String indSequence, String subID) {
		
		LOG.debug("Sub with ID " + subID + ": received notification from SIB.");

		JSONObject notificationJSON = new JSONObject();
		PrefixedQueryUtilsFactory queryUtilsFactory = PrefixedQueryUtilsFactory.getInstance();
		
		StudyroomQueryResultsParser newRoomsParser = null;
		if (newResults != null && newResults.hasResults()) {
			newRoomsParser = queryUtilsFactory.newPrefixedQueryResultsExtractor(newResults);
			applyIngressFilters.accept(newRoomsParser);
			Set<Map<String, String>> newRooms = newRoomsParser.getResults();
			if (newRooms != null && !newRooms.isEmpty()) {
				notificationJSON.put("toAdd", newRooms);				
			}
		}
		
		StudyroomQueryResultsParser obsoleteRoomsParser = null;
		if (oldResults != null && oldResults.hasResults()) {
			obsoleteRoomsParser = queryUtilsFactory.newPrefixedQueryResultsExtractor(oldResults);
			applyEgressFilters.accept(obsoleteRoomsParser);
			Set<Map<String, String>> oldRooms = obsoleteRoomsParser.getResults();	
			if (oldRooms != null && !oldRooms.isEmpty()) {
				notificationJSON.put("toRemove", oldRooms);
			}
		}

		if ((obsoleteRoomsParser != null && !obsoleteRoomsParser.getResults().isEmpty()) || 
				(newRoomsParser != null && !newRoomsParser.getResults().isEmpty())) {
			String notificationJSONString = notificationJSON.toString();
			sendNotification(notificationJSONString);	
		}
	}

	private void sendNotification(String notifMsg) {
		LOG.debug("About to send notification: " + notifMsg);
		super.send(notifMsg);
		LOG.debug("Sent notification: " + notifMsg);		
	}
	
	@Override
	public void kpic_UnsubscribeEventHandler(String subID) {
		LOG.info("Sub with ID " + subID + ": terminated.");
	}
}
