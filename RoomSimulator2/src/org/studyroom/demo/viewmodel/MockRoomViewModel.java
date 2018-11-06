package org.studyroom.demo.viewmodel;

import java.util.*;
import org.studyroom.viewmodel.*;

public class MockRoomViewModel extends ViewModel /*implements IRoomViewModel*/ {
	public String getSelectedRoomName(){
		return "Aula studio";
	}
	public Collection<List<String[]>> getTables(){
		return Arrays.asList(
				Arrays.asList(new String[][]{
						{"IDs","IDt"},
						{"IDs","IDt"},
						{"IDs","IDt"},
						{"IDs","IDt"}
				}),
				Arrays.asList(new String[][]{
						{"IDs","IDt"},
						{"IDs","IDt"}
				}),
				Arrays.asList(new String[][]{
						{"IDs","IDt"}
				}),
				Arrays.asList(new String[][]{
						{"IDs","IDt"},
						{"IDs","IDt"},
						{"IDs","IDt"}
				})
		);
	}
	public Collection<String> getStudyRooms(String university){
		return Collections.singleton("room1");
	}
	public Collection<String> getUniversities(){
		return Collections.singleton("unibo");
	}
	public void selectStudyRoom(String name, String university){}
	public void toggleSensor(String id){}
	public boolean getState(String sensorID){
		System.out.println("Lettura del sensore "+sensorID);
		return false;
	}
	public void setState(String sensorID, boolean state){
		System.out.println("Scrittura del sensore "+sensorID+": "+state);
	}
}
