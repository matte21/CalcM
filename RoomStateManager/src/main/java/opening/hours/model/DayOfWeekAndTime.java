package opening.hours.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

import opening.hours.exceptions.NullDayOfWeekAndTimeException;
import opening.hours.exceptions.NullTimeException;

/**
 * An immutable, thread-safe class that represents a Day of the week
 * and a local time, such as "MONDAY 17:20:15:10013". The time is in 24h clock. 
 */
public final class DayOfWeekAndTime implements Comparable<DayOfWeekAndTime>{

	/**
	 * The day of the week, e.g. MONDAY
	 */
	public final DayOfWeek dayOfWeek;
	
	/**
	 * The local time, e.g. 17:20:15:10013
	 */
	public final LocalTime time;

	/**
	 * @param dayOfWeek - the day of the week
	 * @param time - the local time
	 * 
	 * @throws {@link NullDayOfWeekAndTimeException} if param dayOfTheWeek is null
	 * @throws {@link NullTimeException} if param time is null
	 */
	public DayOfWeekAndTime(DayOfWeek dayOfWeek, LocalTime time) {
		if (dayOfWeek == null) {
			throw new NullDayOfWeekAndTimeException();
		}
		if (time == null) {
			throw new NullTimeException();
		}
			
		this.dayOfWeek = dayOfWeek;
		this.time = time;
	}
	
	
	/**
	 * @return String representation of this object in the format "DAY HH:mm:ss:SSSSS..."
	 */
	public String toString() {
		return dayOfWeek.toString() + " " + time.toString();
	}


	/** (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * 
	 * A DayOfWeekAndTime d1 is less than a DayOfWeekAndTime if d1.DayOfWeek < d2.dayOfWeek ||
	 * (d1.dayOfWeek == d2.dayOfWeek && d1.time < d2.time)
	 * 
	 * @throws NullDayOfWeekAndTimeException if toCompare is null.
	 */
	@Override
	public int compareTo(DayOfWeekAndTime toCompare) {
		if (toCompare == null) {
			throw new NullDayOfWeekAndTimeException();
		}
		
		return dayOfWeek.compareTo(toCompare.dayOfWeek) != 0 ? 
				dayOfWeek.compareTo(toCompare.dayOfWeek) : time.compareTo(toCompare.time); 
	}
	
	@Override
	public boolean equals(Object toCompareObj) {
		if (toCompareObj == null) {
			throw new NullDayOfWeekAndTimeException();
		}
		DayOfWeekAndTime toCompare = (DayOfWeekAndTime) toCompareObj;
		
		return dayOfWeek.equals(toCompare.dayOfWeek) && time.equals(toCompare.time);
	}

}
