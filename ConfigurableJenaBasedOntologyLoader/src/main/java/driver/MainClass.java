package driver;

import resources.JenaBasedOntologyLoader;

public class MainClass {

	// Port 0 is reserved and should not be used, as mandated by IANA
	private static final int MIN_PORT = 1;  
	private static final int MAX_PORT = 65535;
	
	public static void main(String[] args) {
		validateInput(args);
		
		int 	portNbr 	= Integer.parseInt(args[1]);
		String 	sibHost 	= args[0];
		String  ssName		= args[2];
		String 	ontologyURL = args[3];
		JenaBasedOntologyLoader loader = 
				new JenaBasedOntologyLoader(portNbr, sibHost, ssName, ontologyURL);
		
		loader.LoadOntologyIntoSIB();
	}
	
	private static void validateInput(final String[] inputs) {
		if (inputs.length == 4) {
			validateSibPortNumber(inputs[1]);
		} else {
			printUsage();
			System.exit(0);
		}
	}
	
	private static void validateSibPortNumber(final String portNumAsString) {
		Integer sibPort = null;
		try {
			sibPort = Integer.parseInt(portNumAsString);
		} catch (NumberFormatException e) {
			System.out.println("The SIB port argument must be an integer");
			System.exit(0);
		}
		if (sibPort.intValue() < MIN_PORT || sibPort.intValue() > MAX_PORT) {
			System.out.println("Port number must be within " + MIN_PORT + " and " + MAX_PORT);
			System.exit(0);
		}
	}
	
	private static void printUsage() {
		System.out.println("Usage:			<SIB IP/host name> <SIB port>"
					       + " <SS Name> <Ontology absolute URL>\n"
					       + "Display usage info : 	-h or --help");
	}
}
