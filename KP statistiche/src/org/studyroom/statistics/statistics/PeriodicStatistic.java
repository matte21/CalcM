package org.studyroom.statistics.statistics;

import java.time.*;

public abstract class PeriodicStatistic extends Statistic implements Statistic.IntValueChangedListener {
	protected PeriodicStatistic(boolean additive, boolean singleValue, Duration periodUnit){
		super(additive,singleValue);
		Statistic.schedule(this::updateStatisticData,periodUnit);
	}
	protected abstract void updateStatisticData();
}
