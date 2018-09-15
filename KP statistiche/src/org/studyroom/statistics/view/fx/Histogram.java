package org.studyroom.statistics.view.fx;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.chart.*;
import javafx.util.*;
import org.studyroom.view.*;

public class Histogram extends StackedBarChart<String,Number> {
	private final ObjectProperty<Object> vm=new SimpleObjectProperty<>();
	private final ObjectProperty<ObservableList<String>> categories;
	private final ObjectProperty<ObservableMap<String,List<Number>>> values;
	private final StringProperty tiles;
	private String valProp,catProp,tilesProp;
	//@SuppressWarnings("unused")
	//private final ObservableList<String> categories;
	//private final ObservableList<Data<String,Number>> values;
	
	/*public ObjectProperty<ObservableList<String>> categoriesProperty(){
		return cat;
	}*/
	public ObjectProperty<ObservableMap<String,List<Number>>> valuesProperty(){
		return values;
	}
	/*public ObjectProperty<Object> viewModelProperty(){
		return vm;
	}*/
	public Object getViewModel(){
		return vm.get();
	}
	public void setViewModel(Object vm){
		categories.unbind();
		values.unbind();
		tiles.unbind();
		this.vm.set(vm);
		BindingUtil.bindListOneWay(categories,catProp,vm);
		BindingUtil.bindMapOneWay(values,valProp,vm);
		BindingUtil.bindStringOneWay(tiles,tilesProp,vm);
	}
	
	public static Histogram create(Object viewModel, String valProp, String catProp, String tilesProp){
		ObjectProperty<ObservableMap<String,List<Number>>> dp=new SimpleObjectProperty<>();
		ObjectProperty<ObservableList<String>> cp=new SimpleObjectProperty<>();
		StringProperty tp=new SimpleStringProperty();
		BindingUtil.bindListOneWay(cp,catProp,viewModel);
		BindingUtil.bindMapOneWay(dp,valProp,viewModel);
		BindingUtil.bindStringOneWay(tp,tilesProp,viewModel);
		//ObservableList<Series<String,Number>> data=FXCollections.observableArrayList(dp.get().entrySet().stream().map(e->new Series<String,Number>(e.getKey(),FXCollections.observableArrayList(e.getValue().stream().map(v->new Data<>(e.getKey(),v)).collect(Collectors.toList())))).collect(Collectors.toList()));
		ObservableList<String> cat=FXCollections.observableArrayList(cp.get());
		cp.addListener((l,o,n)->{
			cat.clear();
			cat.addAll(cp.get());
		});
		ObservableList<Series<String,Number>> data=toSeries(dp.get(),viewModel);
		dp.addListener((l,o,n)->{
			data.clear();
			data.addAll(toSeries(dp.get(),viewModel));
		});
		return new Histogram(viewModel,valProp,catProp,tilesProp,dp,cp,tp,cat,data);
	}
	@SuppressWarnings("unchecked")
	private static ObservableList<Series<String,Number>> toSeries(Map<String,List<Number>> data, Object viewModel){
		Collection<Map.Entry<String,List<Number>>> c=data.entrySet();
		if (c.isEmpty())
			return FXCollections.emptyObservableList();
		List<Number> l1=c.iterator().next().getValue();
		List<Series<String,Number>> l=new ArrayList<>();
		List<String> leg;
		try{
			leg=(List<String>)viewModel.getClass().getMethod("getLegend").invoke(viewModel);
		} catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchMethodException|SecurityException e){
			leg=Collections.nCopies(l1.size(),"");
		}
		for (int i=0;i<l1.size();i++){
			int ii=i;
			l.add(new Series<>(leg.get(i),FXCollections.observableArrayList(c.stream().map(e->new Data<>(e.getKey(),e.getValue().get(ii))).collect(Collectors.toList()))));
		}
		return FXCollections.observableList(l);
	}
	private Histogram(Object viewModel, String valProp, String catProp, String tilesProp, ObjectProperty<ObservableMap<String,List<Number>>> valuesProperty, ObjectProperty<ObservableList<String>> categoriesProperty, StringProperty tilesProperty, ObservableList<String> cat, ObservableList<Series<String,Number>> data){
		super(new CategoryAxis(cat),new NumberAxis(),data);
		this.valProp=valProp;
		this.catProp=catProp;
		this.tilesProp=tilesProp;
		values=valuesProperty;
		categories=categoriesProperty;
		tiles=tilesProperty;
		vm.set(viewModel);
		vm.addListener((s,o,n)->setViewModel(n));
		NumberAxis y=(NumberAxis)getYAxis();
		y.labelProperty().bind(tilesProperty);
		y.setTickLabelFormatter(new StringConverter<Number>(){
			@Override
			public String toString(Number n){
				return n.intValue()==n.doubleValue()?""+n.intValue():"";
			}
			@Override
			public Number fromString(String s){
				return Integer.parseInt(s);
			}
		});
		//y.setTickUnit(1);	//doesn't work
		y.setMinorTickVisible(false);
		setAnimated(false);
		//setLegendVisible(false);
	}
	public String getBoundValuesPropertyName(){
		return valProp;
	}
	public void setBoundValuesPropertyName(String name){
		valProp=name;
	}
	public String getBoundCategoriesPropertyName(){
		return catProp;
	}
	public void setBoundCategoriesPropertyName(String name){
		catProp=name;
	}
	public String getBoundTilesPropertyName(){
		return tilesProp;
	}
	public void setBoundTilesPropertyName(String name){
		tilesProp=name;
	}
}
