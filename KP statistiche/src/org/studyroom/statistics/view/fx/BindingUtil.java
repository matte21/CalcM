package org.studyroom.statistics.view.fx;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;
import javafx.application.*;
import javafx.beans.property.*;
import javafx.beans.property.adapter.*;
import javafx.collections.*;

public class BindingUtil {
	@SuppressWarnings("unchecked")
	public static <T> void bindObject(ObjectProperty<T> fxProp, String prop, Object viewModel){
		try {
			fxProp.bindBidirectional(JavaBeanObjectPropertyBuilder.create().bean(viewModel).name(prop).build());
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindString(StringProperty fxProp, String prop, Object viewModel){
		try {
			fxProp.bindBidirectional(JavaBeanStringPropertyBuilder.create().bean(viewModel).name(prop).build());
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindBoolean(BooleanProperty fxProp, String prop, Object viewModel){
		try {
			fxProp.bindBidirectional(JavaBeanBooleanPropertyBuilder.create().bean(viewModel).name(prop).build());
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindInteger(IntegerProperty fxProp, String prop, Object viewModel){
		try {
			fxProp.bindBidirectional(JavaBeanIntegerPropertyBuilder.create().bean(viewModel).name(prop).build());
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindLong(LongProperty fxProp, String prop, Object viewModel){
		try {
			fxProp.bindBidirectional(JavaBeanLongPropertyBuilder.create().bean(viewModel).name(prop).build());
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindFloat(FloatProperty fxProp, String prop, Object viewModel){
		try {
			fxProp.bindBidirectional(JavaBeanFloatPropertyBuilder.create().bean(viewModel).name(prop).build());
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindDouble(DoubleProperty fxProp, String prop, Object viewModel){
		try {
			fxProp.bindBidirectional(JavaBeanDoublePropertyBuilder.create().bean(viewModel).name(prop).build());
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	/**not implemented*/
	public static <T> void bindList(ListProperty<T> fxProp, String prop, Object viewModel){
		try {
			fxProp.bindBidirectional(new ReadOnlyJavaBeanListProperty<T>(viewModel,prop));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	/**not implemented*/
	public static <T> void bindList(ObjectProperty<ObservableList<T>> fxProp, String prop, Object viewModel){
		try {
			fxProp.bindBidirectional(new ReadOnlyJavaBeanListProperty<T>(viewModel,prop));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static <T> void bindObjectOneWay(ObjectProperty<T> fxProp, String prop, Object viewModel){
		try {
			//fxProp.bind((ObservableValue<T>)ReadOnlyJavaBeanObjectPropertyBuilder.create().bean(viewModel).name(prop).build());
			fxProp.bind(new ReadOnlyJavaBeanObjectProperty<T>(viewModel,prop));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindStringOneWay(StringProperty fxProp, String prop, Object viewModel){
		try {
			//fxProp.bind(ReadOnlyJavaBeanStringPropertyBuilder.create().bean(viewModel).name(prop).build());
			fxProp.bind(new ReadOnlyJavaBeanObjectProperty<String>(viewModel,prop));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
		
	}
	/*public static void bindBooleanOneWay(BooleanProperty fxProp, String prop, Object viewModel){
		try {
			fxProp.bind(ReadOnlyJavaBeanBooleanPropertyBuilder.create().bean(viewModel).name(prop).build());
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindIntegerOneWay(IntegerProperty fxProp, String prop, Object viewModel){
		try {
			fxProp.bind(ReadOnlyJavaBeanIntegerPropertyBuilder.create().bean(viewModel).name(prop).build());
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindLongOneWay(LongProperty fxProp, String prop, Object viewModel){
		try {
			fxProp.bind(ReadOnlyJavaBeanLongPropertyBuilder.create().bean(viewModel).name(prop).build());
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindFloatOneWay(FloatProperty fxProp, String prop, Object viewModel){
		try {
			fxProp.bind(ReadOnlyJavaBeanFloatPropertyBuilder.create().bean(viewModel).name(prop).build());
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindDoubleOneWay(DoubleProperty fxProp, String prop, Object viewModel){
		try {
			fxProp.bind(ReadOnlyJavaBeanDoublePropertyBuilder.create().bean(viewModel).name(prop).build());
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}*/
	public static <T> void bindListOneWay(ListProperty<T> fxProp, String prop, Object viewModel){
		try {
			fxProp.bind(new ReadOnlyJavaBeanListProperty<T>(viewModel,prop));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static <T> void bindListOneWay(ObjectProperty<ObservableList<T>> fxProp, String prop, Object viewModel){
		try {
			fxProp.bind(new ReadOnlyJavaBeanListProperty<T>(viewModel,prop));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static <K,V> void bindMapOneWay(MapProperty<K,V> fxProp, String prop, Object viewModel){
		try {
			fxProp.bind(new ReadOnlyJavaBeanMapProperty<K,V>(viewModel,prop));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static <K,V> void bindMapOneWay(ObjectProperty<ObservableMap<K,V>> fxProp, String prop, Object viewModel){
		try {
			fxProp.bind(new ReadOnlyJavaBeanMapProperty<K,V>(viewModel,prop));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static <T> ObservableList<T> observableList(String prop, Object viewModel){
		try {
			return new ReadOnlyJavaBeanListProperty<T>(viewModel,prop);
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static <K,V> ObservableMap<K,V> observableMap(String prop, Object viewModel){
		try {
			return new ReadOnlyJavaBeanMapProperty<K,V>(viewModel,prop);
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	/**Invokes a method of the view-model without knowing exactly its class<br>
	 * Useful to keep a weeker interface between view and view-model
	 * @param viewModel - the object that owns the method
	 * @param command - the method name
	 */
	public static void call(Object viewModel, String command){
		try {
			viewModel.getClass().getMethod(command).invoke(viewModel);
		} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
			throw new IllegalArgumentException(e);
		}
	}
	/**Invokes a method of the view-model without knowing exactly its class<br>
	 * Useful to keep a weeker interface between view and view-model
	 * @param viewModel - the object that owns the method
	 * @param command - the method name
	 * @param args - the method arguments
	 */
	public static void call(Object viewModel, String command, Object...args){
		try {
			viewModel.getClass().getMethod(command,Arrays.stream(args).map(Object::getClass).toArray(Class[]::new)).invoke(viewModel,args);
		} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
			throw new IllegalArgumentException(e);
		}
	}
	private static void onFXPlatform(Runnable r){
		if (Platform.isFxApplicationThread())
			System.out.println("Ci siamo già");//XXX
		if (Platform.isFxApplicationThread())
			r.run();
		else
			Platform.runLater(r);
	}
	public static class ReadOnlyJavaBeanObjectProperty<T> extends ObjectPropertyBase<T> {
		private Object bean;
		private String name;
		private Method getter;
		@SuppressWarnings("unchecked")
		public ReadOnlyJavaBeanObjectProperty(Object bean, String name) throws NoSuchMethodException {
			this.bean=bean;
			this.name=name;
			getter=bean.getClass().getMethod("get"+name.substring(0,1).toUpperCase()+name.substring(1));
			Object o;
			try{
				o=getter.invoke(bean);
			} catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e){
				throw new IllegalArgumentException(e);
			}
			try {
				load((T)o);
			} catch (ClassCastException e){
				throw new IllegalArgumentException("Tipe mismatch");
			}
			try {
				bean.getClass().getMethod("addPropertyChangeListener",PropertyChangeListener.class).invoke(bean,new PropertyChangeListener(){
					@Override
					public void propertyChange(PropertyChangeEvent e){
						if (!e.getPropertyName().equals(name))
							return;
						try {
							load((T)e.getNewValue());
						} catch (ClassCastException ex){
							throw new IllegalArgumentException("Incompatible value",ex);
						}
					}
				});
			} catch (NoSuchMethodException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|SecurityException e){
				System.err.println("Impossibile creare il listener: "+e);
			}
		}
		private void load(T o){
			System.out.println(name+": To FX platform...");//XXX
			onFXPlatform(()->{
				set(o);
				fireValueChangedEvent();
			});
		}
		@Override
		public Object getBean(){
			return bean;
		}
		@Override
		public String getName(){
			return name;
		}
	}
	public static class ReadOnlyJavaBeanListProperty<T> extends ListPropertyBase<T> {
		private Object bean;
		private String name;
		private Method getter;
		public ReadOnlyJavaBeanListProperty(Object bean, String name) throws NoSuchMethodException {
			this.bean=bean;
			this.name=name;
			getter=bean.getClass().getMethod("get"+name.substring(0,1).toUpperCase()+name.substring(1));
			Object ol;
			try{
				ol=getter.invoke(bean);
			} catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e){
				throw new IllegalArgumentException(e);
			}
			if (!(ol instanceof List))
				throw new IllegalArgumentException("Tipe mismatch");
			@SuppressWarnings("unchecked")
			List<T> l=(List<T>)ol;
			load(l);
			try {
				bean.getClass().getMethod("addPropertyChangeListener",PropertyChangeListener.class).invoke(bean,new PropertyChangeListener(){
					@SuppressWarnings("unchecked")
					@Override
					public void propertyChange(PropertyChangeEvent e){
						if (!e.getPropertyName().equals(name))
							return;
						try {
							if (e instanceof IndexedPropertyChangeEvent){
								IndexedPropertyChangeEvent ie=(IndexedPropertyChangeEvent)e;
								if (ie.getOldValue()!=null)
									if (ie.getNewValue()!=null)
										set(ie.getIndex(),(T)ie.getNewValue());
									else
										remove(ie.getIndex());
								else if (ie.getNewValue()!=null)
									add(ie.getIndex(),(T)ie.getNewValue());
								fireValueChangedEvent();
							} else
								load((List<T>)e.getNewValue());
						} catch (ClassCastException ex){
							throw new IllegalArgumentException("Incompatible list value",ex);
						}
					}
				});
			} catch (NoSuchMethodException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|SecurityException e){}
		}
		private void load(List<T> l){
			System.out.println(name+": To FX platform...");//XXX
			onFXPlatform(()->{
				set(FXCollections.observableArrayList(l));
				fireValueChangedEvent();
			});
		}
		@Override
		public Object getBean(){
			return bean;
		}
		@Override
		public String getName(){
			return name;
		}
	}
	public static class ReadOnlyJavaBeanMapProperty<K,V> extends MapPropertyBase<K,V> {
		private Object bean;
		private String name;
		private Method getter;
		public ReadOnlyJavaBeanMapProperty(Object bean, String name) throws NoSuchMethodException {
			this.bean=bean;
			this.name=name;
			getter=bean.getClass().getMethod("get"+name.substring(0,1).toUpperCase()+name.substring(1));
			Object om;
			try{
				om=getter.invoke(bean);
			} catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e){
				throw new IllegalArgumentException(e);
			}
			if (!(om instanceof Map))
				throw new IllegalArgumentException("Tipe mismatch");
			@SuppressWarnings("unchecked")
			Map<K,V> m=(Map<K,V>)om;
			load(m);
			try {
				bean.getClass().getMethod("addPropertyChangeListener",PropertyChangeListener.class).invoke(bean,new PropertyChangeListener(){
					@SuppressWarnings("unchecked")
					@Override
					public void propertyChange(PropertyChangeEvent e){
						if (!e.getPropertyName().equals(name))
							return;
						try {
							if (e instanceof IndexedPropertyChangeEvent){
								//use oldValue as key and newValue as value, the index is useless in maps
								/*if (e.getOldValue()==null)
									throw new IllegalArgumentException("null key");*/
								if (e.getNewValue()!=null)
									put((K)e.getOldValue(),(V)e.getNewValue());
								else
									remove((K)e.getOldValue());
								fireValueChangedEvent();
							} else
								load((Map<K,V>)e.getNewValue());
						} catch (ClassCastException ex){
							throw new IllegalArgumentException("Incompatible entry value",ex);
						}
					}
				});
			} catch (NoSuchMethodException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|SecurityException e){}
		}
		private void load(Map<K,V> m){
			System.out.println(name+": To FX platform...");//XXX
			onFXPlatform(()->{
				set(FXCollections.observableMap(m));
				fireValueChangedEvent();
			});
		}
		@Override
		public Object getBean(){
			return bean;
		}
		@Override
		public String getName(){
			return name;
		}
	}
}
