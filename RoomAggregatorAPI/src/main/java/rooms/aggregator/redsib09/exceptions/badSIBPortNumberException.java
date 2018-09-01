package rooms.aggregator.redsib09.exceptions;

public class badSIBPortNumberException extends IllegalArgumentException {

	private static final long serialVersionUID = -6833391038491091836L;

	public badSIBPortNumberException(String msg) {
		super(msg);
	}
}
