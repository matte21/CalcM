package org.studyroom.statistics.statistics;

import java.time.*;
import java.util.*;
import org.studyroom.model.*;
import org.studyroom.statistics.persistence.*;

public class WeeklyStat extends PeriodicStatistic {
	private static final String[] DAYS={"","lunedì","martedì","mercoledì","giovedì","venerdì","sabato","domenica"};
	private final Map<String,IntValue> currentVal=new HashMap<>();
	private final Map<String,Map<Integer,List<IntValue>>> val=new HashMap<>();
	public WeeklyStat(){
		super(false,false,true,true,Duration.ofDays(1));
		for (String id : Persistence.getInstance().getStudyRoomsIDs()){
			currentVal.put(id,new IntValue(0,0));
			val.put(id,new TreeMap<>());
		}
	}
	@Override
	public String getName(){
		return "Utilizzo settimanale aule";
	}
	@Override
	public String getValuesLabel(){
		return "% posti occupati";
	}
	@Override
	public void onValueChanged(String studyRoomID, IntValue v){
		synchronized (currentVal){
			if (v.compareTo(currentVal.get(studyRoomID))>0)
				currentVal.put(studyRoomID,v);
		}
	}
	@Override
	protected void loadStatisticData(Map<String,Map<String,String>> data){
		data.forEach((sr,e)->e.forEach((k,v)->{
			try {
				int d=Integer.parseInt(k.split("_")[1]);
				Map<Integer,List<IntValue>> m=val.get(sr);
				if (m==null) return;//throw new IllegalStateException("Unknown study room: "+sr);
				m.putIfAbsent(d,new ArrayList<>());
				String[] vs=v.split("_");
				m.get(d).add(new IntValue(Integer.parseInt(vs[0]),Integer.parseInt(vs[1])));
			} catch (NumberFormatException | ArrayIndexOutOfBoundsException ex){
				new IllegalArgumentException(getName()+" - Malformed key: "+k).printStackTrace();
			}
		}));
	}
	/**key-value format: week_day=full_partial*/
	@Override
	protected Map<String,Map<String,String>> saveStatisticData(){
		Map<String,Map<String,String>> msr=new LinkedHashMap<>();
		val.forEach((sr,ml)->{
			Map<String,String> m=new LinkedHashMap<>();
			ml.forEach((d,l)->{
				for (int w=0;w<l.size();w++)
					m.put(w+"_"+d,l.get(w).getFull()+"_"+l.get(w).getPartial());
			});
			msr.put(sr,m);
		});
		return msr;
	}
	@Override
	protected void updateStatisticData(){
		int d=(LocalDate.now().getDayOfWeek().getValue()+6)%7;
		synchronized (currentVal){
			for (Map.Entry<String,IntValue> e : currentVal.entrySet()){
				/*if (e.getValue().getTotal()==0)
					continue;*/
				Map<Integer,List<IntValue>> m=val.get(e.getKey());
				synchronized (m){
					m.putIfAbsent(d,new ArrayList<>());
					m.get(d).add(e.getValue());
				}
				currentVal.put(e.getKey(),new IntValue(0,0));
				if (m.get(d).size()==1)
					notifyCategoryChange(null,DAYS[d]);
				notifyValueChange(DAYS[d],getPercentValue(m.get(d),Persistence.getInstance().getStudyRoom(e.getKey())));
			}
		}
	}
	@Override
	public Map<String,Value> getValues(String srID){
		Map<String,Value> m=new LinkedHashMap<>();
		if (val.containsKey(srID)){
			StudyRoom sr=Persistence.getInstance().getStudyRoom(srID);
			Map<Integer,List<IntValue>> ml=val.get(srID);
			synchronized (ml){
				for (Map.Entry<Integer,List<IntValue>> e : ml.entrySet())
					m.put(DAYS[e.getKey()],getPercentValue(e.getValue(),sr));
			}
		}
		return m;
	}
	private Value getPercentValue(Collection<IntValue> l, StudyRoom sr){
		return getPercentValue(l.parallelStream().reduce((v1,v2)->new IntValue(v1.getFull()+v2.getFull(),v1.getPartial()+v2.getPartial())).map(v->new Value((float)v.getFull()/l.size(),(float)v.getPartial()/l.size())).orElse(new Value(0,0)),sr);
	}
}
