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
	private final CategoryChangedListener catListener=(s,o,n)->{cacheValid=false; firePropertyChange("categories",null,getCategories());};
	private final StatisticValueChangedListener valListener=(s,c,v)->{cacheValid=false; firePropertyChange("data",null,getData());};
	private final Aggregator[] aggregators={Aggregator.COMPARISON,Aggregator.SUM,Aggregator.AVERAGE};
	private final List<String> legend=Arrays.asList("posti totalmente occupati","posti parzialmente occupati");
	private final Map<String,Visualization> visualizations=new LinkedHashMap<>();
	private Aggregator aggregator;
	private Map<String,List<Double>> cacheData;
	private boolean init,cacheValid;
	
	public MainViewModel(){
		visualizations.put("tutto",Visualization.BOTH);
		visualizations.put("posti totalmente occupati",Visualization.FULL);
		visualizations.put("posti parzialmente occupati",Visualization.PARTIAL);
		setStatistic(Statistic.get(Statistic.getStatisticsNames().stream().findFirst().get()));
		selectStudyRoom(DEFAULT_SR,getStudyRooms().stream().findFirst().get().getUniversity());
		init=true;
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
		cacheValid=false;
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
		if (!init)
			return Collections.emptyMap();
		if (!cacheValid){
			cacheValid=true;
			Map<String,Value> g=selectedSR.size()==1?statistic.getValues(selectedSR.get(0).getID()):selectedSR.stream().map(StudyRoom::getID).map(statistic::getValues).collect(aggregator);
			cacheData=new LinkedHashMap<>();
			g.forEach((c,v)->cacheData.put(c,toGraphicData(v)));
		}
		return cacheData;
	}
	@Override
	public List<String> getCategories(){
		return new ArrayList<>(getData().keySet());
	}
	private List<Double> toGraphicData(Statistic.Value v){
		List<Double> d=new ArrayList<>(2);
		d.add((double)(visualization.showFull()?v.getFull():0));
		d.add((double)(visualization.showPartial()?v.getPartial():0));
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
	@Override
	public List<String> getLegend(){
		return legend;
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
		cacheValid=false;
		firePropertyChange("categories",null,getCategories());
		firePropertyChange("tilesLabel",l,getTilesLabel());
		firePropertyChange("data",null,getData());
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
		if (!visualizations.containsKey(option))
			throw new IllegalArgumentException("Unknown visualization");
		visualization=visualizations.get(option);
		cacheValid=false;
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
	public Collection<String> getVisualizations(){
		return visualizations.keySet();
	}
	
	@Override
	public void unbind(){
		statistic.removeListeners(catListener,valListener);
	}
}
