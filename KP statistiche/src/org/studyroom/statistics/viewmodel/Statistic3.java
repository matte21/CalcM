package org.studyroom.statistics.viewmodel;

import java.util.*;

public class Statistic3 extends StatisticViewModel {
	Map<String,Double> data=new TreeMap<>();
	public Statistic3(){
		data.put("cat 1",100.0);
	}
	@Override
	public Map<String,Double> getData(){
		return data;
	}
	@Override
	public String getTilesLabel(){
		return "Valori statistica 3";
	}
}
