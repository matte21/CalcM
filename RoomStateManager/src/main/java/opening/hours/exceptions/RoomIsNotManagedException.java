package opening.hours.exceptions;

public class RoomIsNotManagedException extends RuntimeException {

	private static final long serialVersionUID = 5966584715096906288L;
 
	public RoomIsNotManagedException(String message) {
		super(message);
	}


}
