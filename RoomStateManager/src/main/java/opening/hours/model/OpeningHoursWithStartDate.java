package opening.hours.model;

import java.time.LocalDate;

/**
* An {@link OpeningHours OpeningHours} which also has a local start date, which represents the date after which 
* the opening hours are valid (before that date the opening hours have no meaning). 
*/
public interface OpeningHoursWithStartDate extends OpeningHours {
	/**
	 * @return {@link LocalDate LocalDate} representing the start date after which the opening hours are valid.
	 */
	public LocalDate getStartDate();
}
