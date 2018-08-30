/**
 * 
 */
package rooms.aggregator.outcome;

/**
 * @author matteo
 *
 */
public final class EmptySuccess extends EmptyOutcome {

	/**
	 * @param success
	 */
	public EmptySuccess(int resultCode) {
		super(true, "success", resultCode);
	}

}
