/**
 * 
 */
package rooms.aggregator.outcome;

/**
 * @author matteo
 *
 */
public abstract class EmptyOutcome {
	
	private final boolean success;
	private final String resultMessage;
	private final int resultCode;
	
	/**
	 * 
	 */
	protected EmptyOutcome(boolean success, String resultMessage, int resultCode) {
		this.success = success;
		this.resultMessage = resultMessage;
		this.resultCode = resultCode;
	}
	
	public boolean success() {
		return success;
	}
	
	public String getMessage() {
		return resultMessage;
	}
	
	public int getResultCode() {
		return resultCode;
	}

}
