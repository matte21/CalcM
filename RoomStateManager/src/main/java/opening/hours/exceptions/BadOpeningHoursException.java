package opening.hours.exceptions;

public class BadOpeningHoursException extends IllegalArgumentException {

	private static final long serialVersionUID = -881831723984323396L;

	public BadOpeningHoursException(String s) {
		super(s);
	}
}
