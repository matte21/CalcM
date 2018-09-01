package opening.hours.exceptions;

public class FailedToStartManagingRoomException extends Exception {

	private static final long serialVersionUID = -4664924740488409970L;

	public FailedToStartManagingRoomException() {}

	/**
	 * @param message
	 */
	public FailedToStartManagingRoomException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public FailedToStartManagingRoomException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FailedToStartManagingRoomException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public FailedToStartManagingRoomException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
