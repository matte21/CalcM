package org.studyroom.view;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import javafx.application.*;
import javafx.beans.property.*;
//import javafx.beans.property.adapter.*;
import javafx.collections.*;

/**This class contains utility methods to use JavaFX binding over simple Java beans, which may not implement any view-model interface.
 * The binding works if the beans follow the convention of the JavaBeans<sup>TM</sup> atchitecture: they should declare a method 
 * {@code public void addPropertyChangeListener(PropertyChangeListener l)} and send a {@link PropertyChangeEvent}
 * @see Property
 * @see PropertyChangeEvent
 * @see PropertyChangeListener
 * @see PropertyChangeSupport
 */
public class BindingUtil {
	public static <T> void bindObject(ObjectProperty<T> fxProp, String prop, Object viewModel){
		try {
			//fxProp.bindBidirectional(JavaBeanObjectPropertyBuilder.create().bean(viewModel).name(prop).build());
			fxProp.bindBidirectional(new JavaBeanObjectProperty<T>(viewModel,prop));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static <T> void bindObject(ObjectProperty<T> fxProp, String prop, Object viewModel, String id){
		try {
			fxProp.bindBidirectional(new JavaBeanObjectProperty<T>(viewModel,prop,id,String.class));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static <T> void bindObject(ObjectProperty<T> fxProp, String prop, Object viewModel, int index){
		try {
			fxProp.bindBidirectional(new JavaBeanObjectProperty<T>(viewModel,prop,index,Integer.TYPE));
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
	public static <T> void bindObjectOneWay(ObjectProperty<T> fxProp, String prop, Object viewModel, String id){
		try {
			fxProp.bind(new ReadOnlyJavaBeanObjectProperty<T>(viewModel,prop,id,String.class));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static <T> void bindObjectOneWay(ObjectProperty<T> fxProp, String prop, Object viewModel, int index){
		try {
			fxProp.bind(new ReadOnlyJavaBeanObjectProperty<T>(viewModel,prop,index,Integer.TYPE));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	public static void bindString(StringProperty fxProp, String prop, Object viewModel){
		try {
			//fxProp.bindBidirectional(JavaBeanStringPropertyBuilder.create().bean(viewModel).name(prop).build());
			fxProp.bindBidirectional(new JavaBeanObjectProperty<String>(viewModel,prop));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindString(StringProperty fxProp, String prop, Object viewModel, String id){
		try {
			fxProp.bindBidirectional(new JavaBeanObjectProperty<String>(viewModel,prop,id,String.class));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindString(StringProperty fxProp, String prop, Object viewModel, int index){
		try {
			fxProp.bindBidirectional(new JavaBeanObjectProperty<String>(viewModel,prop,index,Integer.TYPE));
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
	public static void bindStringOneWay(StringProperty fxProp, String prop, Object viewModel, String id){
		try {
			fxProp.bind(new ReadOnlyJavaBeanObjectProperty<String>(viewModel,prop,id,String.class));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindStringOneWay(StringProperty fxProp, String prop, Object viewModel, int index){
		try {
			fxProp.bind(new ReadOnlyJavaBeanObjectProperty<String>(viewModel,prop,index,Integer.TYPE));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	public static void bindBoolean(BooleanProperty fxProp, String prop, Object viewModel){
		try {
			//fxProp.bindBidirectional(JavaBeanStringPropertyBuilder.create().bean(viewModel).name(prop).build());
			fxProp.bindBidirectional(new JavaBeanBooleanProperty(viewModel,prop));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindBoolean(BooleanProperty fxProp, String prop, Object viewModel, String id){
		try {
			fxProp.bindBidirectional(new JavaBeanBooleanProperty(viewModel,prop,id,String.class));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindBoolean(BooleanProperty fxProp, String prop, Object viewModel, int index){
		try {
			fxProp.bindBidirectional(new JavaBeanBooleanProperty(viewModel,prop,index,Integer.TYPE));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindBooleanOneWay(BooleanProperty fxProp, String prop, Object viewModel){
		try {
			//fxProp.bind(ReadOnlyJavaBeanStringPropertyBuilder.create().bean(viewModel).name(prop).build());
			fxProp.bind(new ReadOnlyJavaBeanBooleanProperty(viewModel,prop));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindBooleanOneWay(BooleanProperty fxProp, String prop, Object viewModel, String id){
		try {
			fxProp.bind(new ReadOnlyJavaBeanBooleanProperty(viewModel,prop,id,String.class));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindBooleanOneWay(BooleanProperty fxProp, String prop, Object viewModel, int index){
		try {
			fxProp.bind(new ReadOnlyJavaBeanBooleanProperty(viewModel,prop,index,Integer.TYPE));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	public static void bindInteger(IntegerProperty fxProp, String prop, Object viewModel){
		try {
			//fxProp.bindBidirectional(JavaBeanStringPropertyBuilder.create().bean(viewModel).name(prop).build());
			fxProp.bindBidirectional(new JavaBeanIntegerProperty(viewModel,prop));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindInteger(IntegerProperty fxProp, String prop, Object viewModel, String id){
		try {
			fxProp.bindBidirectional(new JavaBeanIntegerProperty(viewModel,prop,id,String.class));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindInteger(IntegerProperty fxProp, String prop, Object viewModel, int index){
		try {
			fxProp.bindBidirectional(new JavaBeanIntegerProperty(viewModel,prop,index,Integer.TYPE));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindIntegerOneWay(IntegerProperty fxProp, String prop, Object viewModel){
		try {
			//fxProp.bind(ReadOnlyJavaBeanStringPropertyBuilder.create().bean(viewModel).name(prop).build());
			fxProp.bind(new ReadOnlyJavaBeanIntegerProperty(viewModel,prop));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindIntegerOneWay(IntegerProperty fxProp, String prop, Object viewModel, String id){
		try {
			fxProp.bind(new ReadOnlyJavaBeanIntegerProperty(viewModel,prop,id,String.class));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindIntegerOneWay(IntegerProperty fxProp, String prop, Object viewModel, int index){
		try {
			fxProp.bind(new ReadOnlyJavaBeanIntegerProperty(viewModel,prop,index,Integer.TYPE));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	/*@Deprecated
	public static void bindBoolean(BooleanProperty fxProp, String prop, Object viewModel){
		try {
			fxProp.bindBidirectional(JavaBeanBooleanPropertyBuilder.create().bean(viewModel).name(prop).build());
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	@Deprecated
	public static void bindInteger(IntegerProperty fxProp, String prop, Object viewModel){
		try {
			fxProp.bindBidirectional(JavaBeanIntegerPropertyBuilder.create().bean(viewModel).name(prop).build());
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	@Deprecated
	public static void bindLong(LongProperty fxProp, String prop, Object viewModel){
		try {
			fxProp.bindBidirectional(JavaBeanLongPropertyBuilder.create().bean(viewModel).name(prop).build());
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	@Deprecated
	public static void bindFloat(FloatProperty fxProp, String prop, Object viewModel){
		try {
			fxProp.bindBidirectional(JavaBeanFloatPropertyBuilder.create().bean(viewModel).name(prop).build());
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	@Deprecated
	public static void bindDouble(DoubleProperty fxProp, String prop, Object viewModel){
		try {
			fxProp.bindBidirectional(JavaBeanDoublePropertyBuilder.create().bean(viewModel).name(prop).build());
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	@Deprecated
	public static <T> void bindList(ListProperty<T> fxProp, String prop, Object viewModel){
		try {
			fxProp.bindBidirectional(new ReadOnlyJavaBeanListProperty<T>(viewModel,prop));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	@Deprecated
	public static <T> void bindList(ObjectProperty<ObservableList<T>> fxProp, String prop, Object viewModel){
		try {
			fxProp.bindBidirectional(new ReadOnlyJavaBeanListProperty<T>(viewModel,prop));
		} catch (NoSuchMethodException e){
			throw new IllegalArgumentException(e);
		}
	}
	public static void bindBooleanOneWay(BooleanProperty fxProp, String prop, Object viewModel){
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
	/**Create and register a {@link PropertyChangeListener} for the desired property, 
	 * which invokes {@code listener} with the new value of the property.<br>
	 * The {@code listener} is also invoked immediatly with the current value of the property.<br>
	 * Don't use with boolean properties, use instead {@code addBooleanPropertyListener}.
	 * @param prop - the property name
	 * @param viewModel - the object that owns the property
	 * @param listener - the action to do on new values
	 * @return the {@link PropertyChangeListener} registered 
	 */
	@SuppressWarnings("unchecked")
	public static <T> PropertyChangeListener addPropertyListener(String prop, Object viewModel, Consumer<T> listener){
		PropertyChangeListener l=e->{
			if (e.getPropertyName().equals(prop))
				listener.accept((T)e.getNewValue());
		};
		try {
			viewModel.getClass().getMethod("addPropertyChangeListener",PropertyChangeListener.class).invoke(viewModel,l);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
			throw new IllegalArgumentException("view-model object does not accept property change listeners",e);
		}
		try {
			listener.accept((T)viewModel.getClass().getMethod(getter(prop)).invoke(viewModel));
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
			System.err.println("Property initialization failed for property listener of \""+prop+"\": property getter not found");
		}
		return l;
	}
	/**Create and register a {@link PropertyChangeListener} for the desired property, 
	 * which invokes {@code listener} with the new value of the property.<br>
	 * The {@code listener} is also invoked immediatly with the current value of the property.
	 * @param prop - the property name
	 * @param viewModel - the object that owns the property
	 * @param listener - the action to do on new values
	 * @return the {@link PropertyChangeListener} registered 
	 */
	public static PropertyChangeListener addBooleanPropertyListener(String prop, Object viewModel, Consumer<Boolean> listener){
		PropertyChangeListener l=e->{
			if (e.getPropertyName().equals(prop))
				listener.accept((Boolean)e.getNewValue());
		};
		try {
			viewModel.getClass().getMethod("addPropertyChangeListener",PropertyChangeListener.class).invoke(viewModel,l);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
			throw new IllegalArgumentException("view-model object does not accept property change listeners",e);
		}
		try {
			listener.accept((Boolean)viewModel.getClass().getMethod(booleanGetter(prop)).invoke(viewModel));
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
			new IllegalArgumentException("Property initialization failed: property getter not found",e).printStackTrace();
		}
		return l;
	}
	/**Create and register a {@link PropertyChangeListener} for the desired property, 
	 * which invokes {@code listener} with the new value of the property.<br>
	 * The {@code listener} is also invoked immediatly with the current value of the property.
	 * @param prop - the property name
	 * @param viewModel - the object that owns the property
	 * @param listener - the actions to do on changes
	 * @return the {@link PropertyChangeListener} registered
	 */
	public static <T> PropertyChangeListener addListPropertyListener(String prop, Object viewModel, ListPropertyListener<T> listener){
		return addListPropertyListener(prop,viewModel,listener::onAdd,listener::onUpdate,listener::onRemove);
	}
	/**Create and register a {@link PropertyChangeListener} for the desired property, 
	 * which invokes {@code listener} with the new value of the property.<br>
	 * The {@code listener} is also invoked immediatly with the current value of the property.
	 * @param prop - the property name
	 * @param viewModel - the object that owns the property
	 * @param onAdd - the action to do on inserted values (parameters: index and new value)
	 * @param onUpdate - the action to do on updated values (parameters: index and new value)
	 * @param onRemove - the action to do on removed values (parameters: index and old value)
	 * @return the {@link PropertyChangeListener} registered
	 */
	public static <T> PropertyChangeListener addListPropertyListener(String prop, Object viewModel, BiConsumer<Integer,T> onAdd, BiConsumer<Integer,T> onUpdate, BiConsumer<Integer,T> onRemove){
		@SuppressWarnings("unchecked")
		PropertyChangeListener l=e->{
			if (e.getPropertyName().equals(prop))
				if (e instanceof IndexedPropertyChangeEvent){
					IndexedPropertyChangeEvent ie=(IndexedPropertyChangeEvent)e;
					T oldV=(T)ie.getOldValue(), newV=(T)ie.getNewValue();
					if (newV!=null && oldV!=null)
						onUpdate.accept(ie.getIndex(),newV);
					else if (newV!=null)
						onAdd.accept(ie.getIndex(),newV);
					else if (oldV!=null)
						onRemove.accept(ie.getIndex(),oldV);
				} else {
					List<T> oldL=(List<T>)e.getOldValue(), newL=(List<T>)e.getNewValue();
					if (newL!=null && oldL!=null){
						int oldS=oldL.size(),newS=newL.size(),minS=Math.min(oldS,newS),i;
						for (i=0;i<minS;i++)
							if (oldL.get(i)!=newL.get(i))
								onUpdate.accept(i,newL.get(i));
						if (newS>oldS)
							for (;i<newS;i++)
								onAdd.accept(i,newL.get(i));
						else
							for (;i<oldS;i++)
								onRemove.accept(i,oldL.get(i));
				} else if (newL!=null)
						for (int i=0;i<newL.size();i++)
							onAdd.accept(i,newL.get(i));
					else if (oldL!=null)
						for (int i=0;i<oldL.size();i++)
							onRemove.accept(i,oldL.get(i));
				}
		};
		try {
			viewModel.getClass().getMethod("addPropertyChangeListener",PropertyChangeListener.class).invoke(viewModel,l);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
			throw new IllegalArgumentException("view-model object does not accept property change listeners",e);
		}
		try {
			l.propertyChange(new PropertyChangeEvent(viewModel,prop,null,viewModel.getClass().getMethod(getter(prop)).invoke(viewModel)));
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
			new IllegalArgumentException("Property initialization failed: property getter not found",e).printStackTrace();
		}
		return l;
	}
	private static void onFXPlatform(Runnable r){
		if (Platform.isFxApplicationThread())
			r.run();
		else
			Platform.runLater(r);
	}
	private static String getter(String prop){
		return "get"+prop.substring(0,1).toUpperCase()+prop.substring(1);
	}
	private static String booleanGetter(String prop){
		return "is"+prop.substring(0,1).toUpperCase()+prop.substring(1);
	}
	private static String setter(String prop){
		return "set"+prop.substring(0,1).toUpperCase()+prop.substring(1);
	}
	public static class ReadOnlyJavaBeanObjectProperty<T> extends ObjectPropertyBase<T> {
		private Object bean;
		private String name;
		private Method getter;
		public ReadOnlyJavaBeanObjectProperty(Object bean, String name) throws NoSuchMethodException {
			this(bean,name,null,null);
		}
		@SuppressWarnings("unchecked")
		public ReadOnlyJavaBeanObjectProperty(Object bean, String name, Object param, Class<?> paramType) throws NoSuchMethodException {
			this.bean=bean;
			this.name=name;
			getter=param==null?bean.getClass().getMethod(getter(name)):bean.getClass().getMethod(getter(name),paramType);
			Object o;
			try {
				o=param==null?getter.invoke(bean):getter.invoke(bean,param);
			} catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e){
				throw new IllegalArgumentException(e);
			}
			try {
				load((T)o);
			} catch (ClassCastException e){
				throw new IllegalArgumentException("Type mismatch");
			}
			try {
				bean.getClass().getMethod("addPropertyChangeListener",PropertyChangeListener.class).invoke(bean,new PropertyChangeListener(){
					@Override
					public void propertyChange(PropertyChangeEvent e){
						if (!e.getPropertyName().equals(name))
							return;
						try {
							load((T)(param==null?e.getNewValue():getter.invoke(bean,param)));
						} catch (ClassCastException|IllegalAccessException|IllegalArgumentException|InvocationTargetException ex){
							throw new IllegalArgumentException("Incompatible value",ex);
						}
					}
				});
			} catch (NoSuchMethodException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|SecurityException e){}
		}
		private void load(T o){
			onFXPlatform(()->{
				super.set(o);
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
	public static class ReadOnlyJavaBeanBooleanProperty extends BooleanPropertyBase {
		private Object bean;
		private String name;
		private Method getter;
		public ReadOnlyJavaBeanBooleanProperty(Object bean, String name) throws NoSuchMethodException {
			this(bean,name,null,null);
		}
		public ReadOnlyJavaBeanBooleanProperty(Object bean, String name, Object param, Class<?> paramType) throws NoSuchMethodException {
			this.bean=bean;
			this.name=name;
			getter=param==null?bean.getClass().getMethod(booleanGetter(name)):bean.getClass().getMethod(getter(name),paramType);
			boolean v;
			try {
				v=(boolean)(param==null?getter.invoke(bean):getter.invoke(bean,param));
			} catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException|ClassCastException e){
				throw new IllegalArgumentException(e);
			}
			try {
				load(v);
			} catch (ClassCastException e){
				throw new IllegalArgumentException("Type mismatch");
			}
			try {
				bean.getClass().getMethod("addPropertyChangeListener",PropertyChangeListener.class).invoke(bean,new PropertyChangeListener(){
					@Override
					public void propertyChange(PropertyChangeEvent e){
						if (!e.getPropertyName().equals(name))
							return;
						try {
							load((boolean)(param==null?e.getNewValue():getter.invoke(bean,param)));
						} catch (ClassCastException|IllegalAccessException|IllegalArgumentException|InvocationTargetException ex){
							throw new IllegalArgumentException("Incompatible value",ex);
						}
					}
				});
			} catch (NoSuchMethodException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|SecurityException e){}
		}
		private void load(boolean o){
			onFXPlatform(()->{
				super.set(o);
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
	public static class ReadOnlyJavaBeanIntegerProperty extends IntegerPropertyBase {
		private Object bean;
		private String name;
		private Method getter;
		public ReadOnlyJavaBeanIntegerProperty(Object bean, String name) throws NoSuchMethodException {
			
		}
		public ReadOnlyJavaBeanIntegerProperty(Object bean, String name, Object param, Class<?> paramType) throws NoSuchMethodException {
			this.bean=bean;
			this.name=name;
			getter=param==null?bean.getClass().getMethod(getter(name)):bean.getClass().getMethod(getter(name),paramType);
			int v;
			try {
				v=(int)(param==null?getter.invoke(bean):getter.invoke(bean,param));
			} catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException|ClassCastException e){
				throw new IllegalArgumentException(e);
			}
			try {
				load(v);
			} catch (ClassCastException e){
				throw new IllegalArgumentException("Type mismatch");
			}
			try {
				bean.getClass().getMethod("addPropertyChangeListener",PropertyChangeListener.class).invoke(bean,new PropertyChangeListener(){
					@Override
					public void propertyChange(PropertyChangeEvent e){
						if (!e.getPropertyName().equals(name))
							return;
						try {
							load((int)(param==null?e.getNewValue():getter.invoke(bean,param)));
						} catch (ClassCastException|IllegalAccessException|IllegalArgumentException|InvocationTargetException ex){
							throw new IllegalArgumentException("Incompatible value",ex);
						}
					}
				});
			} catch (NoSuchMethodException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|SecurityException e){}
		}
		private void load(int o){
			onFXPlatform(()->{
				super.set(o);
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
			getter=bean.getClass().getMethod(getter(name));
			Object ol;
			try{
				ol=getter.invoke(bean);
			} catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e){
				throw new IllegalArgumentException(e);
			}
			if (!(ol instanceof List))
				throw new IllegalArgumentException("Type mismatch");
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
			onFXPlatform(()->{
				super.set(FXCollections.observableArrayList(l));
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
			getter=bean.getClass().getMethod(getter(name));
			Object om;
			try {
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
			onFXPlatform(()->{
				super.set(FXCollections.observableMap(m));
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
	public static class JavaBeanObjectProperty<T> extends ReadOnlyJavaBeanObjectProperty<T> {
		private Method setter;
		private Object param;
		public JavaBeanObjectProperty(Object bean, String name) throws NoSuchMethodException {
			this(bean,name,null,null);
		}
		public JavaBeanObjectProperty(Object bean, String name, Object param, Class<?> paramType) throws NoSuchMethodException {
			super(bean,name,param,paramType);
			this.param=param;
			setter=Arrays.stream(getBean().getClass().getMethods()).filter(m->m.getName().equals(BindingUtil.setter(getName())) && m.getParameterTypes().length==(param==null?1:2)).findAny().orElseThrow(()->new NoSuchMethodException("Setter for "+getName()+" not found"));
		}
		@Override
		public void set(T v){
			super.set(v);
			try {
				if (param==null)
					setter.invoke(getBean(),v);
				else
					setter.invoke(getBean(),param,v);
			} catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e){
				throw new RuntimeException("Error on setting value for property "+getName(),e);
			}
		}
	}
	public static class JavaBeanBooleanProperty extends ReadOnlyJavaBeanBooleanProperty {
		private Method setter;
		private Object param;
		public JavaBeanBooleanProperty(Object bean, String name) throws NoSuchMethodException {
			this(bean,name,null,null);
		}
		public JavaBeanBooleanProperty(Object bean, String name, Object param, Class<?> paramType) throws NoSuchMethodException {
			super(bean,name,param,paramType);
			this.param=param;
			setter=param==null?getBean().getClass().getMethod(setter(getName()),Boolean.TYPE):getBean().getClass().getMethod(setter(getName()),paramType,Boolean.TYPE);
		}
		@Override
		public void set(boolean v){
			super.set(v);
			try{
				if (param==null)
					setter.invoke(getBean(),v);
				else
					setter.invoke(getBean(),param,v);
			} catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e){
				throw new RuntimeException("Error on setting value for property "+getName(),e);
			}
		}
	}
	public static class JavaBeanIntegerProperty extends ReadOnlyJavaBeanIntegerProperty {
		private Method setter;
		private Object param;
		public JavaBeanIntegerProperty(Object bean, String name) throws NoSuchMethodException {
			this(bean,name,null,null);
		}
		public JavaBeanIntegerProperty(Object bean, String name, Object param, Class<?> paramType) throws NoSuchMethodException {
			super(bean,name,param,paramType);
			this.param=param;
			setter=param==null?getBean().getClass().getMethod(setter(getName()),Integer.TYPE):getBean().getClass().getMethod(setter(getName()),paramType,Integer.TYPE);
		}
		@Override
		public void set(int v){
			super.set(v);
			try {
				if (param==null)
					setter.invoke(getBean(),v);
				else
					setter.invoke(getBean(),param,v);
			} catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e){
				throw new RuntimeException("Error on setting value for property "+getName(),e);
			}
		}
	}
	public static interface ListPropertyListener<T> {
		void onAdd(int i, T newValue);
		void onUpdate(int i, T newValue);
		void onRemove(int i, T oldValue);
	}

}
