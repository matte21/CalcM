package org.studyroom.statistics.statistics;

import java.util.*;
import org.studyroom.model.*;
import org.studyroom.statistics.persistence.*;

public class RealTimeOccupation extends RealTimeStatistic implements Observer {
	//private final Map<String,Seat> seat=new HashMap<>();
	private final Map<String,IntValue> val=new TreeMap<>();
	private final List<IntValueChangedListener> valListeners=new LinkedList<>();
	public RealTimeOccupation(){
		super(false,true,true,true);
		for (String uri : Persistence.getInstance().getStudyRoomsIDs())
			val.put(uri,new IntValue(0,0));
	}
	@Override
	public String getName(){
		return "Occupazione attuale aule";
	}
	@Override
	public String getValuesLabel(){
		return "% posti occupati";
	}
	@Override
	public void onSeatStateChanged(SeatStateChangedEvent e){
		IntValue v=val.get(e.getStudyRoomID());
		int f=v.getFull(), p=v.getPartial();
		//seat.putIfAbsent(e.getSeatID(),new Seat());
		//Seat s=Persistence.getInstance().getStudyRoom(e.getStudyRoomID()).getSeat(e.getSeatID());//seat.get(e.getSeatID());
		if (!e.wasSeatPartiallyAvailable())
			f--;
		else if (!e.wasSeatAvailable() && !e.isInitEvent())
			p--;
		//s.setState(e.isSeatAvailable()?Seat.FREE:e.isSeatPartiallyAvailable()?Seat.PARTIAL:Seat.FULL);
		if (!e.isSeatPartiallyAvailable())
			f++;
		else if (!e.isSeatAvailable())
			p++;
		v=new IntValue(f,p);
		val.put(e.getStudyRoomID(),v);
		notifyChange(Persistence.getInstance().getStudyRoom(e.getStudyRoomID()),v,e.isInitEvent());
	}
	@Override
	public Map<String,Value> getValues(String srID){
		Map<String,Value> m=new LinkedHashMap<>();
		if (val.containsKey(srID)){
			StudyRoom sr=Persistence.getInstance().getStudyRoom(srID);
			m.put(sr.getName(),getPercentValue(val.get(srID),sr));
		}
		return m;
	}
	private void notifyChange(StudyRoom sr, IntValue v, boolean init){
		notifyValueChange(sr.getName(),getPercentValue(v,sr));
		if (!init)
			for (IntValueChangedListener l : valListeners)
				l.onValueChanged(sr.getID(),v);
	}
	@Override
	protected void loadStatisticData(Map<String,Map<String,String>> data){}
	@Override
	protected Map<String,Map<String,String>> saveStatisticData(){
		return Collections.emptyMap();
	}
	void addIntValueListener(IntValueChangedListener l){
		valListeners.add(l);
	}
	void removeIntValueListener(IntValueChangedListener l){
		valListeners.remove(l);
	}
}
