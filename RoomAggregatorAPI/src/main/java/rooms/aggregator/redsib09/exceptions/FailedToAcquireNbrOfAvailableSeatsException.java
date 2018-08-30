package rooms.aggregator.redsib09.exceptions;

public class FailedToAcquireNbrOfAvailableSeatsException extends Exception {

	private static final long serialVersionUID = 5068288416168145392L;

	public FailedToAcquireNbrOfAvailableSeatsException(String message) {
		super(message);
	}
}
