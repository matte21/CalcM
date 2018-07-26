package org.studyroom.statistics.viewmodel;

import java.util.*;

public class Statistic1 extends StatisticViewModel {
	Map<String,Double> data=new TreeMap<>();
	public Statistic1(){
		data.put("cat 1",10.0);
		data.put("cat 2",30.0);
	}
	@Override
	public Map<String,Double> getData(){
		return data;
	}
	@Override
	public String getTilesLabel(){
		return "Valori stat";
	}
}
