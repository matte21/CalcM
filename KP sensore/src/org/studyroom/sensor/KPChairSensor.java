package org.studyroom.sensor;

import org.studyroom.kp.*;
import sofia_kp.*;

public class KPChairSensor extends KPSensor {
	public KPChairSensor(String sibHost, int sibPort, String sensorID, Sensor s){
		super(sibHost,sibPort,sensorID,s);
	}

	public KPChairSensor(KPICore sib, String sensorID, Sensor s){
		super(sib,sensorID,s);
	}
	
	@Override
	protected String getPredicate(){
		return SIBUtils.getNS("sr")+"hasChairSensor";
	}
}
