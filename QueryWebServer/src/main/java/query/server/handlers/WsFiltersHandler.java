package query.server.handlers;

import java.util.Map;
import java.util.Set;
import java.util.Vector;

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
	
	@Override
	protected void onClose(CloseCode closeCode, String reason, boolean closedByRemote) {
		String logMsg = "web socket connection closed with code " 
				+ closeCode 
				+ ". Reason: " 
				+ reason 
				+ ". Closed by remote: " 
				+ closedByRemote;
		LOG.info(logMsg);
		
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
		
		LOG.debug("Starting SPARQL subscription for filters: " + msg.getTextPayload());
		SIBResponse sibResp = sib.subscribeSPARQL(sparqlQuery, this);
		if (sibResp == null || !sibResp.isConfirmed()) {
			LOG.info("Failed to start SPARQL subscription for filters " + msg.getTextPayload());
			closeConnectionToSIBAndLog(sib);
		}
		LOG.info("Started SPARQL subscription for filters: " + msg.getTextPayload());
	
		s.set(SUB_ID, sibResp.subscription_id);
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
				}
			}
		}
		
		return queryBuilder.getQuery();
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
		
		if (newResults != null && newResults.hasResults()) {
			StudyroomQueryResultsParser newRoomsParser = PrefixedQueryUtilsFactory.getInstance()
					  .newPrefixedQueryResultsExtractor(newResults);
			Set<Map<String, String>> newRooms = newRoomsParser.getResults();
			notificationJSON.put("toAdd", newRooms);
		}
		
		if (oldResults != null && oldResults.hasResults()) {
			StudyroomQueryResultsParser obsoleteRoomsParser = PrefixedQueryUtilsFactory.getInstance()
					   .newPrefixedQueryResultsExtractor(oldResults);
			Set<Map<String, String>> oldRooms = obsoleteRoomsParser.getResults();			
			notificationJSON.put("toRemove", oldRooms);
		}

		String notificationJSONString = notificationJSON.toString();

		LOG.debug("About to send notification: " + notificationJSONString);
		super.send(notificationJSONString);
		LOG.debug("Sent notification: " + notificationJSONString);
	}

	@Override
	public void kpic_UnsubscribeEventHandler(String subID) {
		LOG.info("Sub with ID " + subID + ": terminated.");
	}
}
