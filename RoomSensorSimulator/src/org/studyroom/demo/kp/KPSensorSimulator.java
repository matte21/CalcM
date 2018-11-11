package org.studyroom.demo.kp;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.studyroom.model.*;
import org.studyroom.sensor.*;
import sofia_kp.*;
import static org.studyroom.kp.SIBUtils.*;

public class KPSensorSimulator {
	private KPICore sib;
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
	}
	public StudyRoom getStudyRoom(String id){
		studyRooms.computeIfAbsent(id,this::loadStudyRoom);
		return studyRooms.get(id);
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
					+ "		sr:hasTableSensor ?ts}").stream().map(r->new SensorSeat(getID(r,0),new MockSensor(),getID(r,1),new MockSensor(),getID(r,2))).peek(s->{
						kps.add(new KPChairSensor(sib,s.getChairSensorID(),s.getChairSensor()));
						kps.add(new KPTableSensor(sib,s.getTableSensorID(),s.getTableSensor()));
					}).collect(Collectors.toMap(Seat::getID,Function.identity()));
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
		return new StudyRoom(id,t.toArray(new Table[t.size()]),getString(srq,0),getString(srq,1));
	}
}
