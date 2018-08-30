package org.studyroom.statistics.statistics;

import java.util.*;
import org.studyroom.statistics.persistence.*;

public class S1 extends Statistic {
	private Map<String,Seat> seat=new HashMap<>();
	private Map<String,Value> val=new TreeMap<>();
	public S1(){
		super(false,true);
		Persistence.getInstance().getStudyRoomsURIs().forEach(n->{
			val.put(n,new Value(0,0));
		});
	}
	@Override
	public void update(Observable o, Object arg){
		try{
			SeatStateChangedEvent e=(SeatStateChangedEvent)arg;
			//String sr=Persistence.getInstance().getStudyRoomName(e.getStudyRoomURI());
			Value v=val.get(e.getStudyRoomURI());
			int f=v.getFull(), p=v.getPartial();
			Seat s=null;
			if (seat.containsKey(e.getSeatURI()))
				s=seat.get(e.getSeatURI());
			else {
				s=new Seat();
				seat.put(e.getSeatURI(),s);
			}
			if (s.isFullyOccupied())
				f--;
			else if (s.isPartiallyOccupied())
				p--;
			s.setState(e.isSeatAvailable()?0:e.isSeatPartiallyAvailable()?1:2);
			if (s.isFullyOccupied())
				f++;
			else if (s.isPartiallyOccupied())
				p++;
			v=new Value(f,p);
			val.put(e.getStudyRoomURI(),v);
			notifyValueChange(Persistence.getInstance().getStudyRoomName(e.getStudyRoomURI()),v);
			//System.out.println((seat.values().stream().filter(Seat::isFullyOccupied).count()==val.values().stream().mapToInt(Value::getFull).sum())+","+(seat.values().stream().filter(Seat::isPartiallyOccupied).count()==val.values().stream().mapToInt(Value::getPartial).sum())+val.values().stream().mapToInt(Value::getTotal).mapToObj(n->""+n).collect(Collectors.joining(", ","(",")")));//XXX
		} catch (ClassCastException ex){
			throw new IllegalArgumentException(ex);
		}
	}
	@Override
	public String getName(){
		return "St. di prova 1";
	}
	@Override
	public String getValuesLabel(){
		return "posti occupati";
	}
	@Override
	public Map<String,Value> getValues(String srURI){
		Map<String,Value> m=new TreeMap<>();
		if (val.containsKey(srURI))
			m.put(Persistence.getInstance().getStudyRoomName(srURI),val.get(srURI));
		return m;
	}
	@Override
	protected void loadStatisticData(Map<String,Map<String,String>> data){
		// TODO Auto-generated method stub
	}
	@Override
	protected Map<String,Map<String,String>> saveStatisticData(){
		// TODO Auto-generated method stub
		return new HashMap<>();
	}
	private class Seat {
		private byte state;
		@SuppressWarnings("unused")
		public boolean isFree(){
			return state==0;
		}
		public boolean isPartiallyOccupied(){
			return state==1;
		}
		public boolean isFullyOccupied(){
			return state==2;
		}
		void setState(int state){
			this.state=(byte)state;
		}
	}
}
