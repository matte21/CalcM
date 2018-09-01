package org.studyroom.statistics.statistics;

import java.util.*;
import org.studyroom.statistics.persistence.*;

public abstract class RealTimeStatistic extends Statistic implements Observer {
	protected RealTimeStatistic(boolean additive, boolean singleValue){
		super(additive,singleValue);
	}
	@Override
	public void update(Observable o, Object arg){
		if (arg instanceof SeatStateChangedEvent)
			onSeatStateChanged((SeatStateChangedEvent)arg);
	}
	protected abstract void onSeatStateChanged(SeatStateChangedEvent e);
	
	protected static class Seat {
		public static final byte FREE=0, PARTIAL=1, FULL=2;
		private byte state;
		public boolean isFree(){
			return state==FREE;
		}
		public boolean isPartiallyOccupied(){
			return state==PARTIAL;
		}
		public boolean isFullyOccupied(){
			return state==FULL;
		}
		byte getState(){
			return state;
		}
		void setState(byte state){
			this.state=state;
		}
	}
}
