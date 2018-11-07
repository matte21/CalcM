package org.studyroom.viewmodel;

import java.beans.*;

public class ViewModel implements IViewModel {
	private final PropertyChangeSupport ps=new PropertyChangeSupport(this);
	public void addPropertyChangeListener(PropertyChangeListener l){
        ps.addPropertyChangeListener(l);
    }
	public void removePropertyChangeListener(PropertyChangeListener l){
        ps.removePropertyChangeListener(l);
    }
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue){
		ps.firePropertyChange(propertyName,oldValue,newValue);
	}
	protected void fireListInsertion(String propertyName, Object newValue, int index){
		ps.firePropertyChange(new IndexedPropertyChangeEvent(this,propertyName,null,newValue,index));
	}
	protected void fireListChange(String propertyName, Object oldValue, Object newValue, int index){
		ps.firePropertyChange(new IndexedPropertyChangeEvent(this,propertyName,oldValue,newValue,index));
	}
	protected void fireListRemoval(String propertyName, Object oldValue, int index){
		ps.firePropertyChange(new IndexedPropertyChangeEvent(this,propertyName,oldValue,null,index));
	}
	protected void fireMapChange(String propertyName, Object key, Object value){
		ps.firePropertyChange(new IndexedPropertyChangeEvent(this,propertyName,key,value,1));
	}
	protected void fireMapRemoval(String propertyName, Object key){
		ps.firePropertyChange(new IndexedPropertyChangeEvent(this,propertyName,key,null,1));
	}
}
