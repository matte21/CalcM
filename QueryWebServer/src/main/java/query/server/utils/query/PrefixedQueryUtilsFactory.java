package query.server.utils.query;

import java.util.HashMap;
import java.util.Map;

import sofia_kp.SSAP_sparql_response;

public class PrefixedQueryUtilsFactory {

	private static PrefixedQueryUtilsFactory instance;
	
	private static Map<String, String> prefixesToFullURIs;
	private static Map<String, String> varsVocabulary;
		
	private PrefixedQueryUtilsFactory(Map<String, String> prefixToFullURI, Map<String, String> varsVocabulary) {
		PrefixedQueryUtilsFactory.prefixesToFullURIs = new HashMap<String, String>(prefixToFullURI);
		PrefixedQueryUtilsFactory.varsVocabulary = new HashMap<String, String>(varsVocabulary);
	}

	public static void init(Map<String, String> prefixToFullURI, Map<String, String> varsVocabulary) {
		if (instance == null) {
			instance = new PrefixedQueryUtilsFactory(prefixToFullURI, varsVocabulary); 
		}
	}
	
	public static PrefixedQueryUtilsFactory getInstance() {
		return instance;
	}
	
	// TODO change return type with an interface, not the actual implementing class
	public PrefixedStudyroomQueryBuilder getPrefixedQueryBuilder() {
		return new PrefixedStudyroomQueryBuilder(prefixesToFullURIs, varsVocabulary);
	}
	
	public StudyroomQueryResultsParser newPrefixedQueryResultsExtractor(SSAP_sparql_response results) {
		return new StudyroomQueryResultsParser(results, 
				prefixesToFullURIs,
				varsVocabulary);
	}
	
}
