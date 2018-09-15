package org.studyroom.statistics.kp;

import java.io.*;
import java.util.*;
import java.util.function.*;
import org.studyroom.model.*;
import org.studyroom.statistics.persistence.*;
import sofia_kp.*;
import static org.studyroom.kp.SIBUtils.*;

public class RedSIBKP extends KPStatistics {
	private static final String SIB_HOST="localhost";
	private static final int SIB_PORT=10010;
	private static RedSIBKP instance;
	private final KPICore sib;
	public static RedSIBKP getInstance(){
		if (instance==null)
			instance=new RedSIBKP();
		return instance;
	}
	private RedSIBKP(){
		sib=new KPICore(SIB_HOST,SIB_PORT,SMART_SPACE_NAME);
		sib.setProtocol_version(1);
		//sib.enable_debug_message();//XXX
		sib.enable_error_message();//XXX
		SIBResponse r=sib.join();
		if (r==null)
			throw new UncheckedIOException("No response",new IOException());
		if (!r.isConfirmed()){
			printSIBResponse(r);
			System.exit(1);
		}
	}
	@Override
	protected KPPersistence createPersistence(){
		StudyRoom[] lsr=query(sib,sparqlPrefix("sr","rdf")+"SELECT ?sr ?n ?u ?c WHERE {"
				+ "?sr rdf:type sr:StudyRoom;"
				+ "		sr:hasName ?n;"
				+ "		sr:hasCapacity ?c;"
				+ "		sr:inUniversity ?uid."
				+ "?uid rdf:type sr:University;"
				+ "		sr:hasUniversityID ?u}").stream().map(l->new StudyRoom(getID(l,0),getInt(l,3),getString(l,1),getString(l,2))).toArray(StudyRoom[]::new);
		return new KPPersistence(lsr);
	}
	@Override
	public void start(){
		for (List<String[]> r : query(sib,sparqlPrefix("sr","rdf")+"SELECT ?sr ?t ?s ?chairState ?deskState WHERE {"
				+ "?sr rdf:type sr:StudyRoom;"
				+ "		sr:table ?t."
				+ "?t rdf:type sr:Table;"
				+ "		sr:seat ?s."
				+ "?s rdf:type sr:Seat;"
				+ "		sr:hasChairSensor ?cs;"
				+ "		sr:hasTableSensor ?ts."
				+ "?cs sr:hasValue ?chairState."
				+ "?ts sr:hasValue ?deskState}")){
			boolean chairOccupied=isSomethingDetected(getString(r,3)), deskOccupied=isSomethingDetected(getString(r,4));
			String studyRoomID=getID(r,0), tableID=getID(r,1), seatID=getID(r,2);
			if (chairOccupied)
				getPersistence().initState(seatID,tableID,studyRoomID,SeatStateChange.CHAIR_OCCUPIED,SeatStateChange.DESK_FREE);
			if (deskOccupied)
				getPersistence().initState(seatID,tableID,studyRoomID,SeatStateChange.DESK_OCCUPIED,SeatStateChange.chair(chairOccupied));
		}
		sib.subscribeSPARQL(sparqlPrefix("sr","rdf")+"SELECT ?seat ?state WHERE {"
				+ "?seat rdf:type sr:Seat;"
				+ "		sr:hasChairSensor ?cs."
				+ "?cs sr:hasValue ?state}",new PublishEventHandler(this::onChairSensorChange));
		sib.subscribeSPARQL(sparqlPrefix("sr","rdf")+"SELECT ?seat ?state WHERE {"
				+ "?seat rdf:type sr:Seat;"
				+ "		sr:hasTableSensor ?ts."
				+ "?ts sr:hasValue ?state}",new PublishEventHandler(this::onDeskSensorChange));
	}
	private void onChairSensorChange(List<String[]> result){
		String seatID=getID(result,0);
		List<String[]> r=query(sib,sparqlPrefix("sr","rdf")+"SELECT ?sr ?t ?state WHERE {"
				+ "?sr rdf:type sr:StudyRoom;"
				+ "		sr:table ?t."
				+ "?t rdf:type sr:Table;"
				+ "		sr:seat sr:"+seatID+"."
				+ "sr:"+seatID+" sr:hasTableSensor ?cs."
				+ "?cs sr:hasValue ?state}").get(0);
		getPersistence().notifyChange(seatID,getID(r,1),getID(r,0),SeatStateChange.chair(isSomethingDetected(getString(result,1))),SeatStateChange.desk(isSomethingDetected(getString(r,2))));
	}
	private void onDeskSensorChange(List<String[]> result){
		String seatID=getID(result,0);
		List<String[]> r=query(sib,sparqlPrefix("sr","rdf")+"SELECT ?sr ?t ?state WHERE {"
				+ "?sr rdf:type sr:StudyRoom;"
				+ "		sr:table ?t."
				+ "?t rdf:type sr:Table;"
				+ "		sr:seat sr:"+seatID+"."
				+ "sr:"+seatID+" sr:hasChairSensor ?cs."
				+ "?cs sr:hasValue ?state}").get(0);
		getPersistence().notifyChange(seatID,getID(r,1),getID(r,0),SeatStateChange.desk(isSomethingDetected(getString(result,1))),SeatStateChange.chair(isSomethingDetected(getString(r,2))));
	}
	
	/**@return whether the value is {@code sr:somethingDetected}*/
	private static boolean isSomethingDetected(String value){
		return value.equals(getNS("sr")+"somethingDetected");
	}
	
	/**Usefull class to create a subscribeHandler with a functional interface.
	 * For each new result published, the handler calls the {@code onPublish} function.*/
	private class PublishEventHandler implements iKPIC_subscribeHandler2 {
		private Consumer<List<String[]>> onPublish;
		public PublishEventHandler(Consumer<List<String[]>> onPublish){
			this.onPublish=onPublish;
		}
		@Override
		public void kpic_SPARQLEventHandler(SSAP_sparql_response newResults, SSAP_sparql_response oldResults, String indSequence, String subID){
			newResults.getResults().forEach(onPublish);
		}
		@Override
		public void kpic_RDFEventHandler(Vector<Vector<String>> newTriples, Vector<Vector<String>> oldTriples, String indSequence, String subID){}
		@Override
		public void kpic_UnsubscribeEventHandler(String sub_ID){
			RedSIBKP.this.sib.leave();
		}
		@Override
		public void kpic_ExceptionEventHandler(Throwable e){
			e.printStackTrace();
		}
	}
}
