package opening.hours.model;

import java.time.LocalDate;

import opening.hours.exceptions.NullStartDateException;

public class OpeningHoursWStartDateImpl extends OpeningHoursImpl implements OpeningHoursWithStartDate {
		
	private LocalDate startDate;

	public OpeningHoursWStartDateImpl(DayOfWeekAndTime[] openingHours, LocalDate startDate) {
		super(openingHours);
		
		if (startDate == null) {
			throw new NullStartDateException();
		}
		this.startDate = startDate;
	}

	
	@Override
	public LocalDate getStartDate() {
		return startDate;
	}

}
