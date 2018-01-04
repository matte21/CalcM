package org.studyroom.sensor;

import org.studyroom.*;

public class KPTableSensor extends KPSensor {
	public KPTableSensor(String seatURI, String sibHost, int sibPort, Sensor s){
		super(seatURI,sibHost,sibPort,s);
	}

	@Override
	protected String getPredicate(){
		return SIBUtils.getNS("sr")+"hasTableSensor";
	}
}
