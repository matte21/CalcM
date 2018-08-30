package opening.hours.exceptions;

public class BadRoomIDException extends IllegalArgumentException {

	private static final long serialVersionUID = 2957189127938706336L;

	public BadRoomIDException(String message) {
		super(message);
	}

}
