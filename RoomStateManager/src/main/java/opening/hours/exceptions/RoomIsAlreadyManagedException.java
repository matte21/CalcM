package opening.hours.exceptions;

public class RoomIsAlreadyManagedException extends Exception {

	private static final long serialVersionUID = 1443328841230602350L;

	public RoomIsAlreadyManagedException(String message) {
		super(message);
	}
}
