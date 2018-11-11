package org.studyroom.demo.kp;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.studyroom.model.*;
import org.studyroom.sensor.*;
import rooms.aggregator.*;
import rooms.aggregator.redsib09.*;
import sofia_kp.*;
import static org.studyroom.kp.SIBUtils.*;

public class KPSensorSimulator {
	private KPICore sib;
	private final RoomsAggregator aggregator;
	private final Map<String,StudyRoom> studyRooms=new HashMap<>();
	private final List<KPSensor> kps=new LinkedList<>();	//to prevent KPSensors being garbaged
	public KPSensorSimulator(){
		this("localhost");
	}
	public KPSensorSimulator(String host){
		sib=new KPICore(host,10010,SMART_SPACE_NAME);
		initNamespaces(getClass().getResourceAsStream("/ontology.owl"));
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
		aggregator=new RedSIB09RoomsAggregator(sib.HOST,sib.PORT,sib.SMART_SPACE_NAME,getNS("sr"));
	}
	public StudyRoom getStudyRoom(String id){
		studyRooms.computeIfAbsent(id,this::loadStudyRoom);
		return studyRooms.get(id);
	}
	public Collection<StudyRoom> getStudyRooms(String id){
		return studyRooms.values();
	}
	private StudyRoom loadStudyRoom(String id){
		List<String[]> srq=query(sib,sparqlPrefix("sr","rdf")+"SELECT ?n ?u WHERE {"
				+ "sr:"+id+"	rdf:type sr:StudyRoom;"
				+ "		sr:hasName ?n;"
				+ "		sr:inUniversity ?uid."
				+ "?uid	sr:hasUniversityID ?u}").stream().findFirst().orElseThrow(()->new IllegalArgumentException("Unknown studyroom ID: "+id));
		List<String> tIDs=query(sib,sparqlPrefix("sr","rdf")+"SELECT ?t WHERE {"
				+ "sr:"+id+" sr:table ?t}").stream().map(r->getID(r,0)).collect(Collectors.toList());
		List<Table> t=new ArrayList<>();
		for (String tID : tIDs){
			Map<String,SensorSeat> ms=query(sib,sparqlPrefix("sr")+"SELECT ?s ?cs ?ts WHERE {"
					+ "sr:"+tID+" sr:seat ?s."
					+ "?s	sr:hasChairSensor ?cs;"
					+ "		sr:hasTableSensor ?ts}").stream().map(r->sensorSeatForSeat(getID(r,0),getID(r,1),getID(r,2))).collect(Collectors.toMap(Seat::getID,Function.identity()));
			//We assume in this simulation that seats are on a streight line, so there is a first one and a last one
			List<SensorSeat> s=new ArrayList<>(ms.size());
			if (ms.size()<3)
				s.addAll(ms.values());
			else {
				//Let's find first and last seat
				List<Vector<String[]>> sSingle=query(sib,sparqlPrefix("sr")+"SELECT ?s1 (COUNT(*) AS ?c) WHERE {"
						+ "sr:"+tID+" sr:seat ?s1, ?s2."	//XXX if there isn't any aggregate function in the SELECT clause, GROUP BY doesn't work
						+ "?s1 sr:near ?s2}"
						+ "GROUP BY ?s1 "	//leave space before '"'!!
						+ "HAVING (COUNT(*) = 1)");
				if (sSingle.size()!=2)
					throw new IllegalStateException("Simulation constraint not respected: seats are not on a straight line ("+sSingle.size()+" single seats).");
				String sfID=getID(sSingle.get(0),0), slID=getID(sSingle.get(1),0), s0ID, s1ID=sfID, s2ID;
				s.add(ms.get(sfID));
				s2ID=getID(query(sib,sparqlPrefix("sr")+"SELECT ?s WHERE {"
						+ "sr:"+tID+" sr:seat ?s."
						+ "sr:"+sfID+" sr:near ?s}").get(0),0);
				s.add(ms.get(s2ID));
				System.out.println(s);
				do {
					s0ID=s1ID;
					s1ID=s2ID;
					s2ID=getID(query(sib,sparqlPrefix("sr")+"SELECT ?s WHERE {"
							+ "sr:"+tID+" sr:seat ?s."
							+ "sr:"+s1ID+" sr:near ?s."
							+ "FILTER(?s != sr:"+s0ID+")}").get(0),0);
					s.add(ms.get(s2ID));
					System.out.println(s);
				} while (!s2ID.equals(slID));
			}
			t.add(new Table(tID,s.toArray(new Seat[s.size()])));
		}
		aggregator.startAggregatingRoom(id);
		return new StudyRoom(id,t.toArray(new Table[t.size()]),getString(srq,0),getString(srq,1));
	}
	public void addStudyRoom(StudyRoom sr){
		if (sr.getTables()==null)
			throw new IllegalArgumentException("Incomplete study room object");
		Optional<String> optUid=query(sib,sparqlPrefix("sr")+"SELECT ?uid WHERE {?uid sr:hasUniversityID \""+sr.getUniversity()+"\"}").stream().map(r->getID(r,0)).findAny();
		String uid=optUid.orElse(sr.getUniversity().replaceAll("\\W",""));
		StringBuilder query=new StringBuilder(sparqlPrefix("sr","rdf"));
		query.append("INSERT DATA {");
		query.append("sr:"+sr.getID()+" rdf:type sr:StudyRoom;");
		query.append("	sr:hasName \""+sr.getName()+"\";");
		query.append("	sr:inUniversity sr:"+uid+". ");
		if (!optUid.isPresent())
			query.append("sr:"+uid+" sr:hasUniversityID \""+sr.getUniversity()+"\". ");
		for (Table t : sr.getTables()){
			query.append("sr:"+sr.getID()+" sr:table sr:"+t.getID()+". ");
			query.append("sr:"+t.getID()+" rdf:type sr:Table. ");
			for (Seat seat : t.getSeats()){
				//let's assume seats to be created by sensorSeatForSeat function
				SensorSeat s;
				try {
					s=(SensorSeat)seat;
				} catch (ClassCastException e){
					throw new IllegalArgumentException("Incomplete study room object");
				}
				query.append("sr:"+t.getID()+" sr:seat sr:"+s.getID()+". ");
				query.append("sr:"+s.getID()+" rdf:type sr:Seat;");
				query.append("		sr:hasChairSensor sr:"+s.getChairSensorID()+";");
				query.append("		sr:hasTableSensor sr:"+s.getTableSensorID()+". ");
				query.append("sr:"+s.getChairSensorID()+" rdf:type sr:ChairSensor;");
				query.append("		sr:hasValue sr:"+(s.getChairSensor().isOn()?"somethingDetected":"nothingDetected")+". ");
				query.append("sr:"+s.getTableSensorID()+" rdf:type sr:TableSensor;");
				query.append("		sr:hasValue sr:"+(s.getTableSensor().isOn()?"somethingDetected":"nothingDetected")+". ");
			}
			for (int i=1;i<t.getSeats().length;i++){
				query.append("sr:"+t.getSeats()[i-1].getID()+" sr:near sr:"+t.getSeats()[i].getID()+". ");
				query.append("sr:"+t.getSeats()[i].getID()+" sr:near sr:"+t.getSeats()[i-1].getID()+". ");
			}
		}
		query.append("}");
		if (!sib.querySPARQL(query.toString()).isConfirmed())
			System.err.println("Error in uploading room data on the SIB");
		studyRooms.put(sr.getID(),sr);
		aggregator.startAggregatingRoom(sr.getID());
	}
	public boolean existsID(String id){
		if (studyRooms.containsKey(id))
			return true;
		return askQuery(sib,sparqlPrefix("sr")+"ASK WHERE {"
				+ "{sr:"+id+" ?p ?o} UNION "
				+ "{?s sr:"+id+" ?o} UNION "
				+ "{?s ?p sr:"+id+"}}");
	}
	public boolean existsStudyRoom(String university, String name){
		return askQuery(sib,sparqlPrefix("sr","rdf")+"ASK WHERE {"
				+ "?s	rdf:type sr:StudyRoom;"
				+ "		sr:hasName \""+name+"\";"
				+ "		sr:inUniversity ?uid."
				+ "?uid	sr:hasUniversityID \""+university+"\"}");
	}
	public SensorSeat sensorSeatForSeat(String id, String chairID, String tableID){
		SensorSeat s=new SensorSeat(id,new MockSensor(),chairID,new MockSensor(),tableID);
		kps.add(new KPChairSensor(sib,s.getChairSensorID(),s.getChairSensor()));
		kps.add(new KPTableSensor(sib,s.getTableSensorID(),s.getTableSensor()));
		return s;
	}
	public SensorSeat sensorSeatForSeat(String id){
		return sensorSeatForSeat(id,id+"c",id+"t");
	}
}
