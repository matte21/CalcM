package org.studyroom.statistics.viewmodel;

import java.util.*;
import org.studyroom.viewmodel.*;

public interface IGraphicViewModel extends IViewModel {
	//graphic (only these are properties)
	String getGraphicTitle();
	String getTilesLabel();
	List<String> getCategories();
	Map<String,List<Double>> getData();
	List<String> getLegend();
	boolean isLegendVisible();
	boolean isPercentValues();
	
	//menus
	static final String DEFAULT_SR="Tutte";
	Collection<String> getStudyRooms(String university);
	Collection<String> getUniversities();
	Collection<String> getStatistics();
	Collection<String> getVisualizations();
	
	//actions
	void selectStatistic(String name);
	void selectStudyRoom(String name, String university);
	void selectVisualization(String visualization);
	
	void unbind();
}
