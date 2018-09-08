package org.studyroom.statistics.kp;

public abstract class KPStatistics {
	private KPPersistence persistence;
	
	protected KPPersistence getPersistence(){
		return persistence;
	}
	
	/**Create an instance of {@code KPPersistence}*/
	public void initPersistence(){
		if (persistence!=null)
			throw new IllegalStateException("Persistence already initialized");
		persistence=createPersistence();
	}
	
	/**Create an instance of {@code KPPersistence}*/
	protected abstract KPPersistence createPersistence();
	
	/**Start sending events to persistence*/
	public abstract void start();
}
