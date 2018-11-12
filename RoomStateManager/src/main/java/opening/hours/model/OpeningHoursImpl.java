package opening.hours.model;

import java.util.Arrays;

import opening.hours.exceptions.IllegalOpeningHoursArrayException;
import opening.hours.exceptions.NullDayOfWeekAndTimeException;

/**
 * A class that internally represents Opening Hours as a sorted array of DayOfWeekAndTime.
 * The DayOfWeekAndTimes with even indices (starting from 0) are considered opening ones, those 
 * with odd indices closing ones. 
 */
public class OpeningHoursImpl implements OpeningHours {

//	private static final Logger LOG = LogManager.getLogger();
	
	private final DayOfWeekAndTime openingHours[]; 
	
	public OpeningHoursImpl(DayOfWeekAndTime[] openingHours) {
		if (openingHours == null || openingHours.length == 0) {
			throw new IllegalOpeningHoursArrayException(openingHours == null ? 
					"openingHours constructor param cannot be null" : 
						"openingHours constructor param length cannot be 0");
		}
		
		this.openingHours = Arrays.copyOf(openingHours, openingHours.length);
		Arrays.sort(this.openingHours);
	}

	
	@Override
	public DayOfWeekAndTime openCloseDayOfWeekAndTimeAfter(DayOfWeekAndTime afterDayOfWeekAndTime) {
		if (afterDayOfWeekAndTime == null) {
			throw new NullDayOfWeekAndTimeException();
		}
		
		int indexOfDayOfWeekAndTimeAfter = 
				Math.abs(Arrays.binarySearch(openingHours, afterDayOfWeekAndTime) + 1) % openingHours.length;
		
		return openingHours[indexOfDayOfWeekAndTimeAfter];
	}

	
	@Override
	public OpenClosed getOpenCloseBeforeDayOfWeekAndTime(DayOfWeekAndTime beforeDayOfWeekAndTime) {
		if (beforeDayOfWeekAndTime == null) {
			throw new NullDayOfWeekAndTimeException();
		}
		
		// TODO remove after tests
		for (int i = 0; i < openingHours.length; i++) {
			//LOG.debug("Opening Hours array entry nbr " + i + ": " + openingHours[i]);			
		}
		int indexOfBeforeDayOfWeekAndTime = Arrays.binarySearch(openingHours, beforeDayOfWeekAndTime);
		
		// TODO remove this logging after tests
		//LOG.debug("Binary search result: " + indexOfBeforeDayOfWeekAndTime);
		if (indexOfBeforeDayOfWeekAndTime >= 0) {
			//LOG.debug("Computing OpenClose before transition at " + beforeDayOfWeekAndTime.dayOfWeek + " " 
			//		   + beforeDayOfWeekAndTime.time + ", index of provided transition: " + indexOfBeforeDayOfWeekAndTime);
			return indexOfBeforeDayOfWeekAndTime % 2 == 0 ? OpenClosed.CLOSED : OpenClosed.OPEN;			
		}
		
		int indexOfNextOpenCloseTransition = 
				Math.abs(Arrays.binarySearch(openingHours, beforeDayOfWeekAndTime) + 1) % openingHours.length;
		//LOG.debug("Computing OpenClose before " + beforeDayOfWeekAndTime.dayOfWeek + " " 
				//   + beforeDayOfWeekAndTime.time + ", index of transition after provided time: " 
				  // + indexOfNextOpenCloseTransition);
		return indexOfNextOpenCloseTransition % 2 == 0 ? OpenClosed.CLOSED : OpenClosed.OPEN;		
	}

}
