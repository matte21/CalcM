package org.studyroom.sensor;

import org.studyroom.*;

public class KPChairSensor extends KPSensor {
	public KPChairSensor(String seatURI, String sibHost, int sibPort, Sensor s){
		super(seatURI,sibHost,sibPort,s);
	}

	@Override
	protected String getPredicate(){
		return SIBUtils.getNS("sr")+"hasChairSensor";
	}
}
