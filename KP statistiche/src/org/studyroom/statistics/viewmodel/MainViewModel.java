package org.studyroom.statistics.viewmodel;

import java.util.*;
import java.util.stream.*;
import org.studyroom.model.*;
import org.studyroom.statistics.persistence.*;

public class MainViewModel extends ViewModel {
	public static final String DEFAULT_SR="Tutte";
	//private List<StudyRoom> getStudyRooms();
	
	private String statistic=StatisticViewModel.getStatistics().stream().findFirst().get();
	private List<StudyRoom> selectedSR;
	private String graphicTitle;
	
	public MainViewModel(){
		selectedSR=new ArrayList<>();
		selectStudyRoom(DEFAULT_SR,getStudyRooms().stream().findFirst().get().getUniversity());
		updateGraphicTitle();
	}
	
	private Collection<StudyRoom> getStudyRooms(){
		return Persistence.getInstance().getStudyRooms();
	}
	
	public StatisticViewModel getStatisticViewModel(){
		return StatisticViewModel.get(statistic);
	}
	private void setStatistic(String statistic){
		firePropertyChange("statisticViewModel",StatisticViewModel.get(this.statistic),StatisticViewModel.get(statistic));
		this.statistic=statistic;
	}
	public List<StudyRoom> getSelectedStudyRooms(){
		return selectedSR;
	}
	private void setSelectedStudyRooms(List<StudyRoom> selected){
		firePropertyChange("selectedStudyRooms",selectedSR,selected);
		selectedSR=selected;
	}
	public String getGraphicTitle(){
		return graphicTitle;
	}
	private void updateGraphicTitle(){
		String title=statistic+" - "+(selectedSR.size()==1?selectedSR.get(0).getName():"media")+" ("+selectedSR.get(0).getUniversity()+")";
		firePropertyChange("graphicTitle",graphicTitle,title);
		graphicTitle=title;
		System.out.println(title);
	}
	
	public void selectStatistic(String name){
		if (!StatisticViewModel.getStatistics().contains(name))
			throw new IllegalArgumentException("Inexistent presenter");
		setStatistic(name);
		updateGraphicTitle();
	}
	public void selectStudyRoom(String name, String university){
		if (name.equals(DEFAULT_SR))
			setSelectedStudyRooms(getStudyRooms().stream().filter(s->s.getUniversity().equals(university)).collect(Collectors.toList()));
		else
			setSelectedStudyRooms(getStudyRooms().stream().filter(s->s.getUniversity().equals(university)&& s.getName().equals(name)).limit(1).collect(Collectors.toList()));
		updateGraphicTitle();
	}
	
	public Collection<String> getStudyRooms(String university){
		return getStudyRooms().stream().filter(s->s.getUniversity().equals(university)).map(StudyRoom::getName).collect(Collectors.toList());
	}
	public Collection<String> getUniversities(){
		return getStudyRooms().stream().map(StudyRoom::getUniversity).distinct().collect(Collectors.toList());
	}
}
