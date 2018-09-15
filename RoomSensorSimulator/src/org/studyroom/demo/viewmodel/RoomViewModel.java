package org.studyroom.demo.viewmodel;

import java.util.*;
import java.util.stream.*;
import org.studyroom.demo.kp.*;
import org.studyroom.model.*;
import org.studyroom.sensor.*;
import org.studyroom.viewmodel.*;

public class RoomViewModel extends ViewModel implements IRoomViewModel {
	private final Map<String,List<StudyRoom>> studyRooms;
	private final Map<String,MockSensor> sensors;
	private StudyRoom sr;
	public RoomViewModel(KPSensorSimulator kp, String...studyRoomIDs){
		studyRooms=Arrays.stream(studyRoomIDs).map(kp::getStudyRoom).collect(Collectors.groupingBy(StudyRoom::getUniversity));
		List<SensorSeat> seats=studyRooms.values().stream().flatMap(Collection::stream).flatMap(s->Arrays.stream(s.getTables())).flatMap(t->Arrays.stream(t.getSeats())).map(SensorSeat.class::cast).collect(Collectors.toList());
		sensors=new HashMap<>();
		sensors.putAll(seats.stream().collect(Collectors.toMap(SensorSeat::getChairSensorID,SensorSeat::getChairSensor)));
		sensors.putAll(seats.stream().collect(Collectors.toMap(SensorSeat::getTableSensorID,SensorSeat::getTableSensor)));
		sr=studyRooms.values().iterator().next().get(0); 
	}

	@Override
	public Collection<String> getUniversities(){
		return studyRooms.keySet();
	}

	@Override
	public Collection<String> getStudyRooms(String university){
		return studyRooms.getOrDefault(university,Collections.emptyList()).stream().map(StudyRoom::getName).collect(Collectors.toList());
	}

	@Override
	public void selectStudyRoom(String name, String university){
		sr=studyRooms.getOrDefault(university,Collections.emptyList()).stream().filter(s->s.getName().equals(university)).findAny().orElseThrow(()->new IllegalArgumentException("Study room \""+name+"\" not found in university "+university));
		firePropertyChange("selectedRoomName",null,getSelectedRoomName());
		firePropertyChange("tables",null,getTables());
	}
	
	@Override
	public String getSelectedRoomName(){
		return sr.getName();
	}

	@Override
	public Collection<List<String[]>> getTables(){
		return Arrays.stream(sr.getTables()).map(
				t->Arrays.stream(t.getSeats()).map(
					s->new String[]{((SensorSeat)s).getChairSensorID(),((SensorSeat)s).getTableSensorID()}
				).collect(Collectors.toList())
			).collect(Collectors.toList());
	}

	@Override
	public boolean getState(String sensorID){
		if (!sensors.containsKey(sensorID))
			throw new IllegalArgumentException("Unknown sensor "+sensorID);
		return sensors.get(sensorID).isOn();
	}

	@Override
	public void setState(String sensorID, boolean state){
		if (!sensors.containsKey(sensorID))
			throw new IllegalArgumentException("Unknown sensor "+sensorID);
		if (state==sensors.get(sensorID).isOn())
			return;
		if (state)
			sensors.get(sensorID).set();
		else
			sensors.get(sensorID).reset();
		firePropertyChange("state",!state,state);
	}
}
