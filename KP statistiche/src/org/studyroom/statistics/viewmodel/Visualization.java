package org.studyroom.statistics.viewmodel;

public enum Visualization {
	FULL(true,false),PARTIAL(false,true),BOTH(true,true);
	private final boolean full,partial;
	Visualization(boolean full, boolean partial){
		this.full=full;
		this.partial=partial;
	}
	public boolean showFull(){
		return full;
	}
	public boolean showPartial(){
		return partial;
	}
}
