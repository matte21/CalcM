/**
 * 
 */
package rooms.aggregator;

import rooms.aggregator.outcome.EmptyOutcome;

/**
 * Interface representing a room aggregator. A room aggregator is an entity in charge of aggregating one or more rooms. 
 * A room is being aggregated when the aggregator keeps the number of available seats in the room up to date. 
 * I.e. If a person enters the room and sits down, the room aggregator should update the state of the room by 
 * decreasing by one the number of available seats in that room in the storage facility keeping the state of the rooms.
 * 
 * The methods of this interface expect a String input parameter representing the unique ID of the room. No rules on
 * the format of such parameter are enforced by this interface. The classes implementing this interface
 * will talk to a storage facility (e.g. a database) keeping the state of the rooms. Typically, the roomID parameter
 * will have to be compliant with what that facility expects. For instance, the parameter roomID could be the primary 
 * key of the table storing the rooms in a relational database.
 */
public interface RoomsAggregator {

	/**
	 * Start aggregating the room with the given ID. Calling this method with an ID of a room which is already being
	 * aggregated will be successful (an instance of @see rooms.aggregator.outcome.EmptySuccess will be returned).
	 * If a null roomID is provided, the method MUST NOT return a @see rooms.aggregator.outcome.EmptySuccess. Whether
	 * to return an @see rooms.aggregator.outcome.EmptyFailure or throw an exception is an implementation choice.
	 *
	 * @param roomID the ID of the room to aggregate.
	 * @return EmptyOutcome instance describing the outcome of the operation. 
	 * 		   An @see rooms.aggregator.outcome.EmptySuccess instance is returned if the room is now being aggregated.
	 * 		   An @see rooms.aggregator.outcome.EmptyFailure instance is returned if the room could not be aggreated.
	 * 		   The @see rooms.aggregator.outcome.EmptyFailure instance contains an explanation of what went wrong.
	 */
	public EmptyOutcome startAggregatingRoom(String roomID);
	
	/**
	 * Stop aggregating the room with the given ID. Calling this method with an ID of a room which is not being
	 * aggregated will be unsuccessful. Whether this is done by throwing an exception or by returning an instance of 
	 * @see rooms.aggregator.outcome.EmptyFailure is an implementation choice.
	 *
	 * @param roomID the ID of the room to stop aggregating.
	 * @return EmptyOutcome instance describing the outcome of the operation. 
	 * 		   An @see rooms.aggregator.outcome.EmptySuccess instance is returned if the room is no longer being 
	 * 		   aggregated. An @see rooms.aggregator.outcome.EmptyFailure instance is returned if it was not possible
	 * 		   to stop the room from being aggregated. The @see rooms.aggregator.outcome.EmptyFailure instance contains
	 * 		   an explanation of what went wrong.
	 */
	public EmptyOutcome stopAggregatingRoom(String roomID);
	
	/**
	 * Check whether the room with the given ID is currently being aggregated.
	 *
	 * @param roomID the ID of the room we want to check whether it's being aggregated or not.
	 * @return true if the room is currently being aggregated, false otherwise.
	 */	
	public boolean isAggregatingRoom(String roomID);

}
