package org.studyroom.viewmodel;

import java.beans.*;

/**Contains the methods that a view-model must declare to allow binding*/
public interface IViewModel {
	void addPropertyChangeListener(PropertyChangeListener l);
	void removePropertyChangeListener(PropertyChangeListener l);
}
