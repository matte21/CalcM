package org.studyroom.sensor;

import java.util.*;
import sofia_kp.*;

import static org.studyroom.SIBUtils.*;

public abstract class KPSensor implements Observer {
	private final KPICore sib;
	private final String uri;
	
	public KPSensor(String seatURI, String sibHost, int sibPort, Sensor s){
		uri=seatURI;
		sib=new KPICore(sibHost,sibPort,SMART_SPACE_NAME);
		SIBResponse r=sib.join();
		if (!r.isConfirmed())
			throw new RuntimeException("SIB connection refused: "+r.Status);
		s.addObserver(this);
		update(s,null);
	}

	protected abstract String getPredicate();
	
	@Override
	public void update(Observable o, Object e){
		Sensor s=(Sensor)o;
		SIBResponse r=sib.update(uri,getPredicate(),getNS("sr")+(s.isOn()?"somethingDetected":"nothingDetected"),"URI","URI",uri,getPredicate(),SSAP_XMLTools.ANYURI,"URI","URI");
		if (!r.isConfirmed())
			System.err.println("Sensor "+uri+": update refused ("+r.Status+")");
	}
	
	@Override
	public boolean equals(Object o){
		return o instanceof KPSensor && ((KPSensor)o).uri.equals(uri);
	}
	
	@Override
	public String toString(){
		return uri+" "+getPredicate().replaceFirst(".*#has","");
	}
}
