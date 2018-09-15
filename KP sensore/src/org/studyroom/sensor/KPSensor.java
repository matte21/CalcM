package org.studyroom.sensor;

import static org.studyroom.kp.SIBUtils.*;
import java.util.*;
import sofia_kp.*;

public abstract class KPSensor implements Observer {
	private final KPICore sib;
	private final String id;
	
	public KPSensor(String sibHost, int sibPort, String sensorID, Sensor s){
		id=sensorID;
		sib=new KPICore(sibHost,sibPort,SMART_SPACE_NAME);
		SIBResponse r=sib.join();
		if (!r.isConfirmed())
			throw new RuntimeException("SIB connection refused: "+r.Status);
		s.addObserver(this);
		update(s,null);
	}

	public KPSensor(KPICore sib, String sensorID, Sensor s){
		this.sib=sib;
		id=sensorID;
		s.addObserver(this);
		update(s,null);
	}

	protected abstract String getPredicate();
	
	@Override
	public void update(Observable o, Object e){
		Sensor s=(Sensor)o;
		//SIBResponse r=sib.update(id,getPredicate(),getNS("sr")+(s.isOn()?"somethingDetected":"nothingDetected"),"URI","URI",id,getPredicate(),SSAP_XMLTools.ANYURI,"URI","URI");
		SIBResponse r=sib.querySPARQL(sparqlPrefix("sr")+"INSERT DATA {sr:"+id+" sr:hasValue sr:"+(s.isOn()?"somethingDetected":"nothingDetected")+"}");
		if (!r.isConfirmed()){
			System.err.println("Sensor "+id+": update refused ("+r.Status+")");
			return;
		}
		r=sib.querySPARQL(sparqlPrefix("sr")+"REMOVE DATA {sr:"+id+" sr:hasValue sr:"+(s.isOn()?"nothingDetected":"somethingDetected")+"}");
	}
	
	@Override
	public boolean equals(Object o){
		return o instanceof KPSensor && ((KPSensor)o).id.equals(id);
	}
	
	@Override
	public String toString(){
		return id+" "+getPredicate().replaceFirst(".*#has","");
	}
}
