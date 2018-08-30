package org.studyroom.statistics.statistics;

import java.io.*;
import java.util.*;
import org.studyroom.statistics.persistence.*;
import org.studyroom.util.*;

public abstract class Statistic implements Observer {
	protected static final String SAVE_PATH="./statistics";
	private static final Map<String,Statistic> statistics=new TreeMap<>();
	public static Statistic get(String name){
		return statistics.get(name);
	}
	public static Collection<String> getStatisticsNames(){
		return statistics.keySet();
	}
	public static void loadStatistics(){
		if (!statistics.isEmpty())
			return;
		Statistic[] st={
				new S1()
				//new ...()	TODO
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
			Persistence.getInstance().addObserver(s);
			statistics.put(s.getName(),s);
		}
	}
	public static void saveStatistics(){
		File d=new File(SAVE_PATH);
		d.mkdirs();
		for (Statistic s : statistics.values())
			IniFile.write(new File(d,s.getClass().getSimpleName()+".ini"),s.saveStatisticData());
	}
	

	public abstract String getName();
	public abstract String getValuesLabel();
	public abstract Map<String,Value> getValues(String studyRoomURI);
	//public abstract Collection<String> getCategories();
	protected abstract void loadStatisticData(Map<String,Map<String,String>> data);
	protected abstract Map<String,Map<String,String>> saveStatisticData();
	
	private List<CategoryChangedListener> catListeners=new LinkedList<>();
	private List<StatisticValueChangedListener> valListeners=new LinkedList<>();
	private boolean additive, singleValue;
	protected Statistic(boolean additive, boolean singleValue){
		this.additive=additive;
		this.singleValue=singleValue;
	}
	public boolean isAdditive(){
		return additive;
	}
	public boolean isSingleValue(){
		return singleValue;
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
	public static final class Value implements Comparable<Value> {
		private final int full,partial;
		Value(int full, int partial){
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
		public int compareTo(Value o){
			int r=getTotal()-o.getTotal();
			return r!=0?r:getFull()-o.getFull();
		}
	}
	public static interface CategoryChangedListener {
		void onCategoryChanged(Statistic source, String oldCategory, String newCategory);
	}
	public static interface StatisticValueChangedListener {
		void onValueChanged(Statistic source, String category, Value newValue);
	}
}
