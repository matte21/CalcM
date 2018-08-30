package org.studyroom.statistics.viewmodel;

import java.util.*;
import java.util.stream.*;
import org.studyroom.model.*;
import org.studyroom.statistics.persistence.*;
import org.studyroom.statistics.statistics.*;
import org.studyroom.statistics.statistics.Statistic.*;

public class MainViewModel extends ViewModel implements IMainViewModel {
	private Statistic statistic;
	private List<StudyRoom> selectedSR=new ArrayList<>();
	private String graphicTitle;
	private Visualization visualization=Visualization.BOTH;
	private CategoryChangedListener catListener=(s,o,n)->firePropertyChange("categories",null,getCategories());
	private StatisticValueChangedListener valListener=(s,c,v)->firePropertyChange("data",null,getData());
	private Aggregator[] aggregators={Aggregator.COMPARISON,Aggregator.SUM,Aggregator.AVERAGE};
	private Aggregator aggregator;
	
	public MainViewModel(){
		setStatistic(Statistic.get(Statistic.getStatisticsNames().stream().findFirst().get()));
		selectStudyRoom(DEFAULT_SR,getStudyRooms().stream().findFirst().get().getUniversity());
	}
	
	private Collection<StudyRoom> getStudyRooms(){
		return Persistence.getInstance().getStudyRooms();
	}
	
	public List<StudyRoom> getSelectedStudyRooms(){
		return selectedSR;
	}
	private void setSelectedStudyRooms(List<StudyRoom> selected){
		if (selected.size()==0)
			throw new IllegalArgumentException("Empty selection");
		//firePropertyChange("selectedStudyRooms",selectedSR,selected);
		selectedSR=selected;
		updateGraphicTitle();
		firePropertyChange("categories",null,getCategories());
		firePropertyChange("data",null,getData());
	}
	@Override
	public String getGraphicTitle(){
		return graphicTitle;
	}
	private void updateGraphicTitle(){
		if (statistic==null || selectedSR.size()==0)
			return;
		String title=statistic.getName()+" - "+(selectedSR.size()==1?selectedSR.get(0).getName():aggregator.getName())+" ("+selectedSR.get(0).getUniversity()+")";
		firePropertyChange("graphicTitle",graphicTitle,title);
		graphicTitle=title;
		System.out.println(title);	////XXX//////////
	}
	@Override
	public Map<String,List<Double>> getData(){
		Map<String,Value> g=selectedSR.size()==1?statistic.getValues(selectedSR.get(0).getURI()):selectedSR.stream().map(StudyRoom::getURI).map(statistic::getValues).collect(aggregator);
		Map<String,List<Double>> m=new TreeMap<>();
		g.forEach((c,v)->m.put(c,toGraphicData(v)));
		return m;
	}
	@Override
	public List<String> getCategories(){
		return new ArrayList<>(getData().keySet());
	}
	private List<Double> toGraphicData(Statistic.Value v){
		List<Double> d=new ArrayList<>(2);
		if (visualization.showFull())
			d.add((double)v.getFull());
		if (visualization.showPartial())
			d.add((double)v.getPartial());
		return d;
	}
	private Aggregator getAggregator(){
		for (Aggregator a : aggregators)
			if (statistic.accept(a))
				return a;
		throw new IllegalStateException("No aggregator found for selected statistic");
	}
	@Override
	public String getTilesLabel(){
		return statistic.getValuesLabel();
	}
	public String getVisualization(){
		return visualization.name().toLowerCase();
	}

	public String getStatistic(){
		return statistic.getName();
	}
	private void setStatistic(Statistic st){
		String /*n=null,*/ l=null;
		if (statistic!=null){
			//n=getStatistic();
			l=getTilesLabel();
			statistic.removeListeners(catListener,valListener);
		}
		statistic=st;
		statistic.addListeners(catListener,valListener);
		//firePropertyChange("statistic",n,getStatistic());
		aggregator=getAggregator();
		firePropertyChange("data",null,getData());
		firePropertyChange("tilesLabel",l,getTilesLabel());
		updateGraphicTitle();
	}
	
	@Override
	public void selectStatistic(String name){
		if (!Statistic.getStatisticsNames().contains(name))
			throw new IllegalArgumentException("Unknown statistic");
		setStatistic(Statistic.get(name));
	}
	@Override
	public void selectStudyRoom(String name, String university){
		if (name.equals(DEFAULT_SR))
			setSelectedStudyRooms(getStudyRooms().stream().filter(s->s.getUniversity().equals(university)).collect(Collectors.toList()));
		else
			setSelectedStudyRooms(getStudyRooms().stream().filter(s->s.getUniversity().equals(university)&& s.getName().equals(name)).limit(1).collect(Collectors.toList()));
	}
	@Override
	public void selectVisualization(String option){
		//String v=getVisualization();
		visualization=Visualization.valueOf(option.toUpperCase());	//can throw IllegalArgumentException
		//firePropertyChange("visualization",v,getVisualization());
		firePropertyChange("data",null,getData());
	}
	
	@Override
	public Collection<String> getStatistics(){
		return Statistic.getStatisticsNames();
	}
	@Override
	public Collection<String> getStudyRooms(String university){
		return getStudyRooms().stream().filter(s->s.getUniversity().equals(university)).map(StudyRoom::getName).collect(Collectors.toList());
	}
	@Override
	public Collection<String> getUniversities(){
		return getStudyRooms().stream().map(StudyRoom::getUniversity).distinct().collect(Collectors.toList());
	}
	
	@Override
	public void unbind(){
		statistic.removeListeners(catListener,valListener);
	}
}
