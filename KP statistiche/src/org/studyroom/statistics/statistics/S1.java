package org.studyroom.statistics.statistics;

import java.util.*;
import java.util.stream.*;
import org.studyroom.statistics.persistence.*;

public class S1 extends Statistic {
	private Map<String,Seat> seat=new HashMap<>();
	private Map<String,Value> val=new TreeMap<>();
	public S1(){
		Persistence.getInstance().getStudyRoomsNames().forEach(n->{
			val.put(n,new Value(0,0));
		});
	}

	@Override
	public void update(Observable o, Object arg){
		try {
			SeatStateChangedEvent e=(SeatStateChangedEvent)arg;
			String sr=Persistence.getInstance().getStudyRoomsName(e.getStudyRoomURI());
			Value v=val.get(sr);
			int f=v.getFull(),p=v.getPartial();
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
			/*if (e.getChange().isOccupied())
				if (e.hasSeatAvailableChanged()){
					f++;
					if (e.hasSeatPartiallyAvailableChanged())
						p--;
				} else if ()
					p++;
			else
				if (e.hasSeatAvailableChanged()){
					f--;
					if (e.hasSeatPartiallyAvailableChanged())
						p++;
				} else if (e.isSeatAvailable())
					p--;*/
			v=new Value(f,p);
			val.put(sr,v);
			notifyValueChange(sr,v);
			assert seat.values().stream().filter(Seat::isFullyOccupied).count()==val.values().stream().mapToInt(Value::getFull).sum();
			assert seat.values().stream().filter(Seat::isPartiallyOccupied).count()==val.values().stream().mapToInt(Value::getPartial).sum();
			System.out.println((seat.values().stream().filter(Seat::isFullyOccupied).count()==val.values().stream().mapToInt(Value::getFull).sum())+","+
					(seat.values().stream().filter(Seat::isPartiallyOccupied).count()==val.values().stream().mapToInt(Value::getPartial).sum())+
					val.values().stream().mapToInt(Value::getTotal).mapToObj(n->""+n).collect(Collectors.joining(", ","(",")")));
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
		return "valori";
	}

	@Override
	public Map<String,Value> getValues(){
		return val;
	}

	@Override
	public Collection<String> getCategories(){
		return val.keySet();
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
