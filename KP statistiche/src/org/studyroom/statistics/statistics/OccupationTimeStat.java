package org.studyroom.statistics.statistics;

import java.util.*;
import java.util.stream.*;
import org.studyroom.statistics.persistence.*;
import static org.studyroom.statistics.statistics.Statistic.SeatState.*;

public class OccupationTimeStat extends RealTimeStatistic {
	private final Map<String,SeatOccupation> seats=new HashMap<>();
	private final Map<String,List<SeatOccupation>> val=new TreeMap<>();
	public OccupationTimeStat(){
		super(false,true,false,true);
		for (String id : Persistence.getInstance().getStudyRoomsIDs())
			val.put(id,new LinkedList<>());
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
		SeatState s=e.isSeatAvailable()?FREE:e.isSeatPartiallyAvailable()?PARTIAL:FULL;
		if (e.isInitEvent()){
			seats.put(e.getSeatID(),new SeatOccupation.Invalid(s));
			return;
		}
		if (seats.containsKey(e.getSeatID())){
			SeatOccupation so=seats.get(e.getSeatID());
			so.setState(s);
			if (so.isSetFree()){
				seats.remove(e.getSeatID());
				if (so.toValue()!=null){
					val.get(e.getStudyRoomID()).add(so);
					notifyValueChange(Persistence.getInstance().getStudyRoomName(e.getStudyRoomID()),getValue(e.getStudyRoomID()));
				}
			}
		} else if (s!=FREE)
			seats.put(e.getSeatID(),new SeatOccupation(s));
	}
	@Override
	public Map<String,Value> getValues(String srID){
		Map<String,Value> m=new LinkedHashMap<>();
		if (val.containsKey(srID))
			m.put(Persistence.getInstance().getStudyRoomName(srID),getValue(srID));
		return m;
	}
	private Value getValue(String srID){
		List<SeatOccupation> l=val.get(srID);
		return l.parallelStream().map(SeatOccupation::toValue).filter(v->v!=null).reduce((v1,v2)->new IntValue(v1.getFull()+v2.getFull(),v1.getPartial()+v2.getPartial())).map(v->new Value(v.getFull()/60.0f/l.size(),v.getPartial()/60.0f/l.size())).orElse(new Value(0,0));
	}
	@Override
	protected void loadStatisticData(Map<String,Map<String,String>> data){
		data.forEach((sr,m)->{
			List<SeatOccupation> l=val.get(sr);
			if (l!=null)
				for (String s : m.values())
					l.add(new SeatOccupation(new TreeMap<>(Arrays.stream(s.split(" ")).map(e->e.split(":")).collect(Collectors.toMap(e->Long.parseLong(e[0]),e->SeatState.valueOf(e[1]))))));
			else
				System.err.println(getName()+": unknown room \""+sr+"\" in data file");
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
		private SortedMap<Long,SeatState> states=new TreeMap<>();
		protected boolean closed;
		public SeatOccupation(SeatState state){
			if (state==FREE)
				throw new IllegalArgumentException("Illegal state");
			closed=false;
			setState(state);
		}
		SeatOccupation(SortedMap<Long,SeatState> m){
			states=m;
			closed=m.get(m.lastKey())==FREE;
		}
		public boolean isSetFree(){
			return closed;
		}
		public SeatState getState(){
			return states.get(states.lastKey());
		}
		void setState(SeatState state){
			if (closed)
				throw new UnsupportedOperationException("Seat occupation already set free");
			if (state==FREE)
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
			for (Map.Entry<Long,SeatState> e : states.entrySet()){
				k=e.getKey();
				if (s<0 && e.getValue()!=FULL)
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
			SeatState s;
			public Invalid(SeatState state){
				super(state);
			}
			@Override
			public SeatState getState(){
				return s;
			}
			@Override
			void setState(SeatState state){
				s=state;
				if (state==FREE)
					closed=true;
			}
			@Override
			public IntValue toValue(){
				return null;
			}
		}
	}
}
