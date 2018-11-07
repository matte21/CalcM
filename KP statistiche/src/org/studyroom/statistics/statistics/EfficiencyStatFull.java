package org.studyroom.statistics.statistics;

import java.util.*;
import org.studyroom.statistics.persistence.*;

public class EfficiencyStatFull extends RealTimeStatistic implements Statistic.IntValueChangedListener {
	private final Map<String,IntValue> val=new HashMap<>();
	protected EfficiencyStatFull(){
		super(false,true,true,false);
		for (String uri : Persistence.getInstance().getStudyRoomsIDs())
			val.put(uri,new IntValue(0,0));
	}
	@Override
	public String getName(){
		return "Efficienza istantanea uso posti (occupati)";
	}
	@Override
	public String getValuesLabel(){
		return "posti occupati / posti non liberi (%)";
	}
	@Override
	protected void onSeatStateChanged(SeatStateChangedEvent e){}
	@Override
	public void onValueChanged(String studyRoomID, IntValue newValue){
		val.put(studyRoomID,newValue);
		notifyValueChange(Persistence.getInstance().getStudyRoomName(studyRoomID),getValue(newValue));
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
