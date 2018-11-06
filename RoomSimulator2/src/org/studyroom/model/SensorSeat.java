package org.studyroom.model;

import org.studyroom.sensor.*;

public class SensorSeat extends Seat {
	private static final long serialVersionUID=1L;
	private final MockSensor chair,table;
	private final String chairID,tableID;
	public SensorSeat(String id, MockSensor chairSensor, String chairSensorID, MockSensor tableSensor, String tableSensorID){
		super(id);
		chair=chairSensor;
		table=tableSensor;
		chairID=chairSensorID;
		tableID=tableSensorID;
	}
	public MockSensor getChairSensor(){
		return chair;
	}
	public MockSensor getTableSensor(){
		return table;
	}
	public String getChairSensorID(){
		return chairID;
	}
	public String getTableSensorID(){
		return tableID;
	}
}
