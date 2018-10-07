package query.server.utils;

import query.server.exceptions.BadSIBIPorHostException;
import query.server.exceptions.BadSIBPortNumberException;
import query.server.exceptions.BadSmartSpaceNameException;
import query.server.exceptions.SIBConnectionErrorException;
import sofia_kp.KPICore;
import sofia_kp.SIBResponse;


public class SibConnectionFactory {

	private static SibConnectionFactory instance = null; 
			
	private String sibIPorHost;
	private int sibPort;
	private String smartSpaceName;

	public static SibConnectionFactory getInstance() {
		return instance;
	}
	
	public static void init(String sibIPorHost, int sibPort, String smartSpaceName) {
		if (instance == null) {			
			instance = new SibConnectionFactory(sibIPorHost, sibPort, smartSpaceName);
		}
	}
	
	private SibConnectionFactory(String sibIPorHost, int sibPort, String smartSpaceName) {
		validateInputs(sibIPorHost, sibPort, smartSpaceName);
		
		this.sibIPorHost = sibIPorHost;
		this.sibPort = sibPort;
		this.smartSpaceName = smartSpaceName;
	}

	private void validateInputs(String sibIPorHost, int sibPort, String smartSpaceName) {
		if (sibIPorHost == null || sibIPorHost.trim().isEmpty()) {
			throw new BadSIBIPorHostException(sibIPorHost == null ? "received null for input parameter "
											  + "sibIPorHost" : "received empty input parameter sibIPorHost");
		}
		if (sibPort < 1 || sibPort > 65535) {
			throw new BadSIBPortNumberException("SIB port number is not a valid port number. Valid port nbrs are "
												+ "in range [1,65535]");
		}
		if (smartSpaceName == null || smartSpaceName.trim().isEmpty()) {
			throw new BadSmartSpaceNameException(smartSpaceName == null ? 
				"received null input parameter smartspaceName": "received empty input parameter smartspaceName");
		}
	}
	
	public KPICore getSIBConnection() throws SIBConnectionErrorException {
		KPICore sibConn = new KPICore(sibIPorHost, sibPort, smartSpaceName);
		
		SIBResponse resp = sibConn.join();
		if (resp == null || !resp.isConfirmed()) {
			throw new SIBConnectionErrorException("Failed to join the smart space " + smartSpaceName + " for SIB with "
					+ " IP " + sibIPorHost + " and port " + sibPort + ".");
		}
		
		return sibConn;
	}
	
}
