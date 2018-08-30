package opening.hours.model;

/**
 * An interface exposing methods to get information about opening hours of an associated place. This interface mandates
 * nothing about the association between instances of the implementing classes and places: we could have a single 
 * instance associated to multiple places (that share the same opening hours) or a separate instance for each place,
 * or enforce no association between instances and places in the implementing class and have the user maintain that
 * information. It's a choice left to the implementers.
 */
public interface OpeningHours {
	/**
	 * Method to get the {@link DayOfWeekAndTime DayOfWeekAndTime} that represents the first transition time (from open
	 * to closed or vice versa) after the time represented by param "afterDayOfWeekAndTime" (excluded).
	 * 
	 * @param afterDayOfWeekAndTime - The {@link DayOfWeekAndTime DayOfWeekAndTime} (excluded) for which we want to 
	 * know the following opening state transition {@link DayOfWeekAndTime DayOfWeekAndTime}. 
	 * 
	 * @return The {@link DayOfWeekAndTime DayOfWeekAndTime} at which the first opening transition following 
	 * afterDayOfWeekAndTime (excluded) takes place. For instance, if two successive opening state transition times are 
	 * Monday at 08:00 as opening time and Monday at 20:00 as closing time, invoking this method with 
	 * afterDayOfWeekAndTime representing "Monday 09:00" will return a DayOfWeekAndTime representing "Monday 20:00". 
	 * Notice how afterDayOfWeekAndTime is excluded: invoking this method with afterDayOfWeekAndTime equal to 
	 * "Monday 07:59" will return "Monday 08:00" while invoking it with afterDayOfWeekAndTime equal to "Monday 08:00" 
	 * will return "Monday 20:00". 
	 * 
	 * @throws NullDayOfWeekAndTimeException if param afterDayOfWeekAndTime is null.
	 */
	public DayOfWeekAndTime openCloseDayOfWeekAndTimeAfter(final DayOfWeekAndTime afterDayOfWeekAndTime);
	
	/**
	 * @param dayOfWeekAndTime - The {@link DayOfWeekAndTime DayOfWeekAndTime} before which we want to know the opening
	 * state (open or closed).
	 * 
	 * @return The opening state (open or closed) before the param dayOfWeekAndTime. For instance,
	 * if the opening hours instance has Monday at 08:00 as the opening instant and Monday at 20:00 as the closing one,
	 * invoking this method with a param dayOfWeekAndTime value that represents "MONDAY 20:00" will return 
	 * {@link OpenClosed.OPEN OpenClosed.OPEN}. 
	 * 
	 * @throws NullDayOfWeekAndTimeException if param dayOfWeekAndTime is null.
	 */
	public OpenClosed getOpenCloseBeforeDayOfWeekAndTime(final DayOfWeekAndTime dayOfWeekAndTime);
}
