package org.studyroom.statistics.viewmodel;

import java.util.*;

public abstract class StatisticViewModel extends ViewModel {
	private static final Map<String,StatisticViewModel> statistics=new TreeMap<>();
	static {
		statistics.put("stat 1",new Statistic1());
		statistics.put("statistica lunga 2",new Statistic2());
		statistics.put("statistica 3",new Statistic3());
		//statistics.put("giorniSettimana",new );
		//statistics.put("fasceOrarie",new );
		//statistics.put("tempoMedioOccupazione",new );
	}
	public static StatisticViewModel get(String name){
		if (!statistics.containsKey(name))
			throw new IllegalArgumentException("Statistic "+name+" not found");
		return statistics.get(name);
	}
	public static Collection<String> getStatistics(){
		return statistics.keySet();
	}
	
	public abstract Map<String,Double> getData();
	public abstract String getTilesLabel();
}
