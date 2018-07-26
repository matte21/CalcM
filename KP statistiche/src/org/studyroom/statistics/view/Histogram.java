package org.studyroom.statistics.view;

import java.util.*;
import java.util.stream.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.scene.chart.*;
import org.studyroom.statistics.viewmodel.*;

public class Histogram extends BarChart<String,Number> {
	private final ObjectProperty<Object> vm=new SimpleObjectProperty<>();
	/*private ObjectProperty<ObservableList<String>> cat;
	private ObjectProperty<ObservableList<Data<String,Number>>> values;*/
	@SuppressWarnings("unused")
	private final ObservableList<String> categories;
	private final ObservableList<Data<String,Number>> values;
	
	/*public ObjectProperty<ObservableList<String>> categoriesProperty(){
		return cat;
	}*/
	/*public ObjectProperty<ObservableList<Data<String,Number>>> valuesProperty(){
		return values;
	}*/
	public ObjectProperty<Object> viewModelProperty(){
		return vm;
	}
	public Object getViewModel(){
		return vm.get();
	}
	public void setViewModel(Object vm){
		this.vm.set(vm);
	}
	
	public static Histogram create(Object viewModel, String dataProp, String tilesProp){
		return new Histogram(viewModel,dataProp,FXCollections.observableArrayList(),tilesProp,FXCollections.observableArrayList());
	}
	@SuppressWarnings("unchecked")
	private Histogram(Object viewModel, String dataProp, ObservableList<String> categories, String tilesProp, ObservableList<Data<String,Number>> data){
		super(new CategoryAxis(categories),new NumberAxis(),FXCollections.observableArrayList(new Series<String,Number>(data)));
		/*cat.set(categories);
		values.set(data);*/
		this.categories=categories;
		values=data;
		vm.addListener(new ChangeListener<Object>(){
			private ObservableMap<String,Number> data;
			MapChangeListener<String,Number> l=e->{
				if (e.wasRemoved()){
					if (!e.wasAdded())
						categories.remove(e.getKey());
					values.removeIf(d->d.getXValue().equals(e.getKey()));
				}
				if (e.wasAdded()){
					if (!e.wasRemoved())
						categories.add(e.getKey());
					values.add(new Data<>(e.getKey(),e.getValueAdded()));
				}
			};
			@Override
			public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue){
				if (data!=null)
					data.removeListener(l);
				if (!(newValue instanceof ViewModel))
					throw new IllegalArgumentException(newValue+" non è un viewModel");
				data=BindingUtil.observableMap(dataProp,newValue);
				categories.clear();
				categories.addAll(data.keySet());
				Collections.sort(categories);
				values.clear();
				values.addAll(data.entrySet().stream().map(e->new Data<>(e.getKey(),e.getValue())).collect(Collectors.toList()));
				Histogram.this.getYAxis().labelProperty().unbind();
				BindingUtil.bindObjectOneWay(Histogram.this.getYAxis().labelProperty(),tilesProp,vm.get());
				data.addListener(l);
			}
		});
		vm.set(viewModel);
		setAnimated(false);
		setLegendVisible(false);
	}
}
