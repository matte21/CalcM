package org.studyroom.statistics.statistics;

import java.util.*;
import java.util.stream.*;
import org.studyroom.statistics.persistence.*;

public class OccupationTimeStat extends RealTimeStatistic {
	private final Map<String,SeatOccupation> seat=new HashMap<>();
	private final Map<String,List<SeatOccupation>> val=new TreeMap<>();
	public OccupationTimeStat(){
		super(false,true);
		for (String uri : Persistence.getInstance().getStudyRoomsURIs())
			val.put(uri,new LinkedList<>());
	}
	@Override
	public String getName(){
		return "Tempo di occupazione medio";
	}
	@Override
	public String getValuesLabel(){
		return "minuti";
	}
	@Override
	protected void onSeatStateChanged(SeatStateChangedEvent e){
		byte s=e.isSeatAvailable()?Seat.FREE:e.isSeatPartiallyAvailable()?Seat.PARTIAL:Seat.FULL;
		if (e.isInitEvent()){
			seat.put(e.getSeatURI(),new SeatOccupation.Invalid(s));
			return;
		}
		if (seat.containsKey(e.getSeatURI())){
			SeatOccupation so=seat.get(e.getSeatURI());
			so.setState(s);
			if (so.isSetFree()){
				seat.remove(e.getSeatURI());
				if (so.toValue()!=null){
					val.get(e.getStudyRoomURI()).add(so);
					notifyValueChange(Persistence.getInstance().getStudyRoomName(e.getStudyRoomURI()),getValue(e.getStudyRoomURI()));
				}
			}
		} else if (s!=Seat.FREE)
			seat.put(e.getSeatURI(),new SeatOccupation(s));
	}
	@Override
	public Map<String,Value> getValues(String srURI){
		Map<String,Value> m=new LinkedHashMap<>();
		if (val.containsKey(srURI))
			m.put(Persistence.getInstance().getStudyRoomName(srURI),getValue(srURI));
		return m;
	}
	private Value getValue(String srURI){
		List<SeatOccupation> l=val.get(srURI);
		return l.parallelStream().map(SeatOccupation::toValue).reduce((v1,v2)->new IntValue(v1.getFull()+v2.getFull(),v1.getPartial()+v2.getPartial())).map(v->new Value(v.getFull()/60.0f/l.size(),v.getPartial()/60.0f/l.size())).orElse(new Value(0,0));
	}
	@Override
	protected void loadStatisticData(Map<String,Map<String,String>> data){
		data.forEach((sr,m)->{
			List<SeatOccupation> l=val.get(sr);
			for (String s : m.values())
				l.add(new SeatOccupation(new TreeMap<>(Arrays.stream(s.split(" ")).map(e->e.split(":")).collect(Collectors.toMap(e->Long.parseLong(e[0]),e->Byte.parseByte(e[1]))))));
		});
	}
	@Override
	protected Map<String,Map<String,String>> saveStatisticData(){
		Map<String,Map<String,String>> msr=new LinkedHashMap<>();
		val.forEach((sr,l)->{
			Map<String,String> m=new LinkedHashMap<>();
			for (int i=0;i<l.size();i++)
				m.put("L"+i,l.get(i).states.entrySet().stream().map(e->e.getKey()+":"+e.getValue()).collect(Collectors.joining(" ")));
			msr.put(sr,m);
		});
		return msr;
	}
	protected static class SeatOccupation {
		private SortedMap<Long,Byte> states=new TreeMap<>();
		protected boolean closed;
		public SeatOccupation(byte state){
			if (state!=Seat.PARTIAL && state!=Seat.FULL)
				throw new IllegalArgumentException("Illegal state");
			setState(state);
		}
		SeatOccupation(SortedMap<Long,Byte> m){
			states=m;
		}
		public boolean isSetFree(){
			return closed;
		}
		public byte getState(){
			return states.get(states.lastKey());
		}
		void setState(byte state){
			if (closed)
				throw new UnsupportedOperationException("Seat occupation already set free");
			if (state>Seat.FULL || state<Seat.FREE)
				throw new IllegalArgumentException("Illegal state");
			if (state==Seat.FREE)
				closed=true;
			if (states.isEmpty() || state!=states.get(states.lastKey()))
				states.put(System.currentTimeMillis(),state);
		}
		/**Returns {@code null} if the seat is still occupied*/
		public IntValue toValue(){
			if (!closed)
				return null;
			final long PAUSE_TIME=300000;
			int f,p=(int)((states.lastKey()-states.firstKey())/1000);
			long s=-1,p1=-1,t=0,/*n=0,*/k=0;
			for (Map.Entry<Long,Byte> e : states.entrySet()){
				k=e.getKey();
				if (s<0 && k!=Seat.FULL)
					continue;
				if (s<0)
					s=k;
				else if (p1<0)
					p1=k;
				else {
					if (k-p1>PAUSE_TIME){
						t+=k-s;
						//n++;
						s=-1;
					}
					p1=-1;
				}
			}
			if (s>=0){
				t+=k-s;
				//n++;
			}
			f=(int)(t/1000);
			return new IntValue(f,p-f);
		}
		public static class Invalid extends SeatOccupation {
			byte s;
			public Invalid(byte state){
				super(state);
			}
			@Override
			public byte getState(){
				return s;
			}
			@Override
			void setState(byte state){
				s=state;
				if (state==Seat.FREE)
					closed=true;
			}
			@Override
			public IntValue toValue(){
				return null;
			}
		}
	}
}
