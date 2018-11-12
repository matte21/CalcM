package org.studyroom.statistics.statistics;

import java.time.*;
import java.util.*;
import java.util.stream.*;
import org.studyroom.model.*;
import org.studyroom.statistics.persistence.*;
import static org.studyroom.statistics.statistics.Statistic.SeatState.*;

public class EfficiencyTimeStatFull extends RealTimeStatistic {
	private final Map<String,SeatOccupation> seats=new HashMap<>();
	private final Map<String,Value> val=new HashMap<>();
	protected EfficiencyTimeStatFull(){
		super(false,true,true,false);
		long now=System.currentTimeMillis();
		for (Seat s : Persistence.getInstance().getSeats())
			seats.put(s.getID(),new SeatOccupation(s.isAvailable()?FREE:s.isChairAvailable()?PARTIAL:FULL,now));
		Statistic.schedule(this::refresh,Duration.ofSeconds(2));
	}
	private void refresh(){
		for (StudyRoom sr : Persistence.getInstance().getStudyRooms()){
			List<SeatOccupation> l=Arrays.stream(sr.getTables()).flatMap(t->Arrays.stream(t.getSeats())).map(s->seats.get(s.getID())).filter(s->s!=null).collect(Collectors.toList());
			long full=l.stream().mapToLong(SeatOccupation::getFullTime).sum(), total=l.stream().mapToLong(SeatOccupation::getTotalTime).sum();
			double r= total==0 ? 1 : (double)full/total;
			Value v=new Value((float)r*100,0);
			val.put(sr.getID(),v);
			notifyValueChange(sr.getName(),v);
		}
	}
	@Override
	public String getName(){
		return "Efficienza media uso posti (occupati)";
	}
	@Override
	public String getValuesLabel(){
		return "tempo di reale utilizzo / tempo di occupazione (%)";
	}
	@Override
	protected void onSeatStateChanged(SeatStateChangedEvent e){
		seats.putIfAbsent(e.getSeatID(),new SeatOccupation());
		seats.get(e.getSeatID()).setState(e.isSeatAvailable()?FREE:e.isSeatPartiallyAvailable()?PARTIAL:FULL);
	}
	@Override
	public Map<String,Value> getValues(String studyRoomID){
		return Collections.singletonMap(Persistence.getInstance().getStudyRoomName(studyRoomID),val.get(studyRoomID));
	}

	@Override
	protected void loadStatisticData(Map<String,Map<String,String>> data){
		long now=System.currentTimeMillis();
		for (Map<String,String> m : data.values())	//should be only one
			m.forEach((id,v)->{
				String[] spl=v.split("_",3);
				try {
					seats.put(id,new SeatOccupation(SeatState.valueOf(spl[2]),now,Long.parseLong(spl[0]),Long.parseLong(spl[0])));
				} catch (NumberFormatException | IndexOutOfBoundsException e){
					System.err.println("EfficiencyStat: malformed input line: "+id+"="+v);
				}
			});
	}
	@Override
	protected Map<String,Map<String,String>> saveStatisticData(){
		Map<String,String> m=new TreeMap<>();
		seats.forEach((id,s)->m.put(id,s.getFullTime()+"_"+s.getPartialTime()+"_"+s.getState()));
		return Collections.singletonMap("mixed",m);
	}
	@SuppressWarnings("unused")	//some overloaded methods can currently be not in use
	private class SeatOccupation {
		private long f,p,t;
		private OccupationTimeStat.SeatState state;
		public SeatOccupation(OccupationTimeStat.SeatState state, long now, long full, long partial){
			this.state=state;
			t=now;
			f=full;
			p=partial;
		}
		public SeatOccupation(OccupationTimeStat.SeatState state, long now){
			this(state,now,0,0);
		}
		public SeatOccupation(OccupationTimeStat.SeatState state){
			this(state,System.currentTimeMillis(),0,0);
		}
		public SeatOccupation(){
			this(FREE,System.currentTimeMillis(),0,0);
		}
		OccupationTimeStat.SeatState getState(){
			return state;
		}
		public void setState(OccupationTimeStat.SeatState state){
			long now=System.currentTimeMillis();
			if (this.state==FULL)
				f+=now-t;
			else if (this.state==PARTIAL)
				p+=now-t;
			this.state=state;
			t=now;
		}
		public long getFullTime(){
			return f+(state==FULL?System.currentTimeMillis()-t:0);
		}
		public long getPartialTime(){
			return p+(state==PARTIAL?System.currentTimeMillis()-t:0);
		}
		public long getTotalTime(){
			return f+p+(state!=FREE?System.currentTimeMillis()-t:0);
		}
	}
}
