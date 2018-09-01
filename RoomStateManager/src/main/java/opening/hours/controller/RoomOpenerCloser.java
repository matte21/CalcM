package opening.hours.controller;

import opening.hours.exceptions.FailedToStartManagingRoomException;
import opening.hours.exceptions.FailedToStopManagingRoomException;
import opening.hours.exceptions.RoomIsAlreadyManagedException;
import opening.hours.model.OpeningHours;

public interface RoomOpenerCloser {

	public void startManagingRoom(String roomID, OpeningHours oh) 
			throws FailedToStartManagingRoomException, RoomIsAlreadyManagedException;
	
	public void stopManagingRoom(String roomID) throws FailedToStopManagingRoomException;
	
	public boolean isManagingRoom(String roomID);
}
