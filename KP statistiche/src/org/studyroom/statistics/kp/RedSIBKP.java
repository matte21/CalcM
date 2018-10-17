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
	private Map<String,Seat> seats;
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
		/*StudyRoom[] lsr=query(sib,sparqlPrefix("sr","rdf")+"SELECT ?sr ?n ?u ?c WHERE {"
				+ "?sr rdf:type sr:StudyRoom;"
				+ "		sr:hasName ?n;"
				+ "		sr:hasCapacity ?c;"
				+ "		sr:inUniversity ?uid."
				+ "?uid rdf:type sr:University;"
				+ "		sr:hasUniversityID ?u}").stream().map(l->new StudyRoom(getID(l,0),getInt(l,3),getString(l,1),getString(l,2))).toArray(StudyRoom[]::new);*/
		Map<String,Map<String,List<Seat>>> m=new LinkedHashMap<>();
		seats=new HashMap<>();
		Map<String,String[]> srData=new HashMap<>();
		for (List<String[]> r : query(sib,sparqlPrefix("sr","rdf")+"SELECT ?sr ?n ?u ?t ?s ?chairState ?deskState WHERE {"
				+ "?sr rdf:type sr:StudyRoom;"
				+ "		sr:table ?t;"
				+ "		sr:hasName ?n;"
				+ "		sr:inUniversity ?uid."
				+ "?uid rdf:type sr:University;"
				+ "		sr:hasUniversityID ?u."
				+ "?t rdf:type sr:Table;"
				+ "		sr:seat ?s."
				+ "?s rdf:type sr:Seat;"
				+ "		sr:hasChairSensor ?cs;"
				+ "		sr:hasTableSensor ?ts."
				+ "?cs sr:hasValue ?chairState."
				+ "?ts sr:hasValue ?deskState}")){
			boolean chairOccupied=isSomethingDetected(getString(r,5)), deskOccupied=isSomethingDetected(getString(r,6));
			String studyRoomID=getID(r,0), tableID=getID(r,3), seatID=getID(r,4), studyRoomName=getString(r,1), universityName=getString(r,2);
			Seat s=new Seat(seatID);
			s.setChairAvailable(!chairOccupied);
			s.setDeskAvailable(!deskOccupied);
			seats.put(seatID,s);
			if (!m.containsKey(studyRoomID)){
				m.put(studyRoomID,new LinkedHashMap<>());
				srData.put(studyRoomID,new String[]{studyRoomName,universityName});
			}
			Map<String,List<Seat>> srm=m.get(studyRoomID);
			srm.putIfAbsent(tableID,new ArrayList<>());
			srm.get(tableID).add(s);
		}
		/*StudyRoom[] lsr=query(sib,sparqlPrefix("sr","rdf")+"SELECT ?sr ?n ?u WHERE {"
				+ "?sr rdf:type sr:StudyRoom;"
				+ "		sr:hasName ?n;"
				+ "		sr:inUniversity ?uid."
				+ "?uid rdf:type sr:University;"
				+ "		sr:hasUniversityID ?u}").stream().map(l->new StudyRoom(getID(l,0),getInt(l,3),getString(l,1),getString(l,2))).toArray(StudyRoom[]::new);*/
		return new KPPersistence(m.keySet().stream().map(sr->new StudyRoom(sr,m.get(sr).keySet().stream().map(
				t->new Table(t,m.get(sr).get(t).toArray(new Seat[0]))
			).toArray(Table[]::new),srData.get(sr)[0],srData.get(sr)[1])).toArray(StudyRoom[]::new));
	}
	@Override
	public void start(){
		System.out.println("Avvio KP statistiche");	//XXX
		/*for (List<String[]> r : query(sib,sparqlPrefix("sr","rdf")+"SELECT ?sr ?t ?s ?chairState ?deskState WHERE {"
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
			System.out.println(seatID+" "+chairOccupied+" "+deskOccupied);
		}*/
		for (Seat s : seats.values()){
			if (!s.isChairAvailable())
				getPersistence().initState(s,SeatStateChange.CHAIR_OCCUPIED);
			if (!s.isDeskAvailable())
				getPersistence().initState(s,SeatStateChange.DESK_OCCUPIED);
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
		System.out.println("Chair changed");	//XXX
		Seat s=seats.get(getID(result,0));
		if (s==null)
			throw new IllegalStateException("Unknown seat ID");
		boolean occupied=isSomethingDetected(getString(result,1));
		s.setChairAvailable(!occupied);
		getPersistence().notifyChange(s,SeatStateChange.chair(occupied));
	}
	private void onDeskSensorChange(List<String[]> result){
		System.out.println("Table changed");	//XXX
		Seat s=seats.get(getID(result,0));
		if (s==null)
			throw new IllegalStateException("Unknown seat ID");
		boolean occupied=isSomethingDetected(getString(result,1));
		s.setDeskAvailable(!occupied);
		getPersistence().notifyChange(s,SeatStateChange.desk(occupied));
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
			if (newResults!=null)
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
