package org.studyroom.statistics.statistics;

import java.util.*;
import org.studyroom.model.*;
import org.studyroom.statistics.persistence.*;

public class EfficiencyStatFree extends RealTimeStatistic implements Statistic.IntValueChangedListener {
	private final Map<String,IntValue> val=new HashMap<>();
	protected EfficiencyStatFree(){
		super(false,true,true,false);
		for (String uri : Persistence.getInstance().getStudyRoomsIDs())
			val.put(uri,new IntValue(0,0));
	}
	@Override
	public String getName(){
		return "Efficienza istantanea uso posti (liberi)";
	}
	@Override
	public String getValuesLabel(){
		return "posti liberi / posti non occupati (%)";
	}
	@Override
	protected void onSeatStateChanged(SeatStateChangedEvent e){}
	@Override
	public void onValueChanged(String studyRoomID, IntValue newValue){
		StudyRoom sr=Persistence.getInstance().getStudyRoom(studyRoomID);
		IntValue v=new IntValue(sr.getCapacity()-newValue.getFull(),newValue.getPartial());
		val.put(studyRoomID,v);
		notifyValueChange(sr.getName(),getValue(v));
	}
	@Override
	public Map<String,Value> getValues(String studyRoomID){
		IntValue v=val.getOrDefault(studyRoomID,new IntValue(0,0));
		return Collections.singletonMap(Persistence.getInstance().getStudyRoomName(studyRoomID),getValue(v));
	}
	private Value getValue(IntValue v){
		int t=v.getTotal();
		return new Value(t==0?0:100.0f*v.getFull()/t,0);
	}
	@Override
	protected void loadStatisticData(Map<String,Map<String,String>> data){}
	@Override
	protected Map<String,Map<String,String>> saveStatisticData(){
		return Collections.emptyMap();
	}
}
