/**
 * 
 */
package rooms.aggregator.outcome;

/**
 * @author matteo
 *
 */
public final class EmptyFailure extends EmptyOutcome {
	
	public EmptyFailure(String errorMessage, int resultCode) {
		super(false, errorMessage, resultCode);
	}
	
}
