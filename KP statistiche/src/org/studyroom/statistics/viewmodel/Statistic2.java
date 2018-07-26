package org.studyroom.statistics.viewmodel;

import java.util.*;

public class Statistic2 extends StatisticViewModel {
	Map<String,Double> data=new TreeMap<>();
	public Statistic2(){
		data.put("cat 1",50.0);
		data.put("cat 2",10.0);
	}
	@Override
	public Map<String,Double> getData(){
		return data;
	}
	@Override
	public String getTilesLabel(){
		return "Valori statistica";
	}
}
