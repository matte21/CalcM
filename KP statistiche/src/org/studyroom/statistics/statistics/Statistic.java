package org.studyroom.statistics.statistics;

import java.io.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import org.studyroom.model.*;
import org.studyroom.statistics.persistence.*;
import org.studyroom.util.*;

public abstract class Statistic {
	protected static final String SAVE_PATH="./statistics";
	private static final Map<String,Statistic> statistics=new LinkedHashMap<>();
	private static final Timer timer=new Timer("StatisticTimer",true);
	public static Statistic get(String name){
		return statistics.get(name);
	}
	public static Collection<String> getStatisticsNames(){
		return statistics.keySet();
	}
	public static void loadStatistics(){
		if (!statistics.isEmpty())
			return;
		RealTimeOccupation srt=new RealTimeOccupation();
		Statistic[] st={
				srt,
				new DailyStat(),
				new WeeklyStat(),
				new OccupationTimeStat(),
				new EfficiencyStatFull(),
				new EfficiencyStatFree(),
				new EfficiencyTimeStatFull(),
				new EfficiencyTimeStatFree()
				//new ...()
		};
		File d=new File(SAVE_PATH);
		boolean load=d.exists();
		for (Statistic s : st){
			if (load){
				File f=new File(d,s.getClass().getSimpleName()+".ini");
				if (f.exists())
					try{
						s.loadStatisticData(IniFile.read(f));
					} catch (IOException e){
						System.err.println(f.getAbsolutePath()+" danneggiato");
					}
			}
			if (s instanceof IntValueChangedListener)
				srt.addIntValueListener((IntValueChangedListener)s);
			else if (s instanceof RealTimeStatistic)
				Persistence.getInstance().addObserver((RealTimeStatistic)s);
			statistics.put(s.getName(),s);
		}
		schedule(Statistic::saveStatistics,Duration.ofDays(1));
		Runtime.getRuntime().addShutdownHook(new Thread(Statistic::saveStatistics));
	}
	public static void saveStatistics(){
		System.out.println("Saving statistic data");	//XXX
		File d=new File(SAVE_PATH);
		d.mkdirs();
		for (Statistic s : statistics.values())
			IniFile.write(new File(d,s.getClass().getSimpleName()+".ini"),s.saveStatisticData());
	}
	protected static void schedule(Runnable task, Duration interval){
		long d=interval.toMillis();
		LocalDateTime t=LocalDateTime.now();
		long i=Duration.between(t,t.plus(interval).truncatedTo(interval.toDays()>0?ChronoUnit.DAYS:interval.toHours()>0?ChronoUnit.HOURS:interval.toMinutes()>0?ChronoUnit.MINUTES:ChronoUnit.SECONDS)).toMillis();
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run(){
				task.run();
			}
		},i,d);
		System.out.println("Task scheduled in "+i/1000+" s by "+Thread.currentThread().getStackTrace()[3].getClassName());	//XXX
	}
	protected static Value toValue(IntValue v){
		return new Value(v.full,v.partial);
	}
	protected static Value getPercentValue(Value v, StudyRoom sr){
		int n=sr.getCapacity();
		return new Value(v.getFull()*100.0f/n,v.getPartial()*100.0f/n);
	}
	protected static Value getPercentValue(IntValue v, StudyRoom sr){
		int n=sr.getCapacity();
		return new Value(v.getFull()*100.0f/n,v.getPartial()*100.0f/n);
	}

	public abstract String getName();
	public abstract String getValuesLabel();
	public abstract Map<String,Value> getValues(String studyRoomID);
	//public abstract Collection<String> getCategories();
	protected abstract void loadStatisticData(Map<String,Map<String,String>> data);
	protected abstract Map<String,Map<String,String>> saveStatisticData();
	
	private final List<CategoryChangedListener> catListeners=new LinkedList<>();
	private final List<StatisticValueChangedListener> valListeners=new LinkedList<>();
	private final boolean additive, singleValue, percent, onSeats;
	protected Statistic(boolean additive, boolean singleValue, boolean percent, boolean onSeats){
		this.additive=additive;
		this.singleValue=singleValue;
		this.percent=percent;
		this.onSeats=onSeats;
	}
	public boolean isAdditive(){
		return additive;
	}
	public boolean isSingleValue(){
		return singleValue;
	}
	public boolean isPercent(){
		return percent;
	}
	public boolean isOnSeats(){
		return onSeats;
	}
	public boolean accept(Aggregator a){
		return !((a.isAdditive()&& !additive)||(a.isComparison()&& !singleValue));
	}
	public void addCategoryListener(CategoryChangedListener cat){
		catListeners.add(cat);
	}
	public void addValueListener(StatisticValueChangedListener cat){
		valListeners.add(cat);
	}
	public void addListeners(CategoryChangedListener cat, StatisticValueChangedListener val){
		addCategoryListener(cat);
		addValueListener(val);
	}
	public void removeCategoryListener(CategoryChangedListener cat){
		catListeners.remove(cat);
	}
	public void removeValueListener(StatisticValueChangedListener cat){
		valListeners.remove(cat);
	}
	public void removeListeners(CategoryChangedListener cat, StatisticValueChangedListener val){
		removeCategoryListener(cat);
		removeValueListener(val);
	}
	protected void notifyCategoryChange(String oldCategory, String newCategory){
		for (CategoryChangedListener l : catListeners)
			l.onCategoryChanged(this,oldCategory,newCategory);
	}
	protected void notifyValueChange(String category, Value newValue){
		for (StatisticValueChangedListener l : valListeners)
			l.onValueChanged(this,category,newValue);
	}
	@Override
	public String toString(){
		return getName();
	}
	public static class Value implements Comparable<Value> {
		private final float full,partial;
		Value(float full, float partial){
			this.full=full;
			this.partial=partial;
		}
		/**@return the value considering only full-occupied seats */
		public float getFull(){
			return full;
		}
		/**@return the value considering only seats with free chair but occupied desk*/
		public float getPartial(){
			return partial;
		}
		/**@return the value considering all occupied seats */
		public float getTotal(){
			return full+partial;
		}
		@Override
		public int compareTo(Value o){
			float r=getTotal()-o.getTotal();
			return (int)(r!=0?r:getFull()-o.getFull());
		}
		@Override
		public String toString(){
			return "("+full+","+partial+")";
		}
	}
	protected static class IntValue implements Comparable<IntValue> {
		private final int full,partial;
		IntValue(int full, int partial){
			this.full=full;
			this.partial=partial;
		}
		/**@return the value considering only full-occupied seats */
		public int getFull(){
			return full;
		}
		/**@return the value considering only seats with free chair but occupied desk*/
		public int getPartial(){
			return partial;
		}
		/**@return the value considering all occupied seats */
		public int getTotal(){
			return full+partial;
		}
		@Override
		public int compareTo(IntValue o){
			int r=getTotal()-o.getTotal();
			return r!=0?r:getFull()-o.getFull();
		}
		@Override
		public String toString(){
			return "("+full+","+partial+")";
		}
	}
	public static interface CategoryChangedListener {
		void onCategoryChanged(Statistic source, String oldCategory, String newCategory);
	}
	public static interface StatisticValueChangedListener {
		void onValueChanged(Statistic source, String category, Value newValue);
	}
	protected static interface IntValueChangedListener {
		void onValueChanged(String studyRoomID, IntValue newValue);
	}
	protected static enum SeatState {
		FREE, PARTIAL, FULL;
	}
}
