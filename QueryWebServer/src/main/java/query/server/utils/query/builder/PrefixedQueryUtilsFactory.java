package query.server.utils.query.builder;

import java.util.HashMap;
import java.util.Map;

public class PrefixedQueryUtilsFactory {

	private static PrefixedQueryUtilsFactory instance;
	
	private static Map<String, String> prefixesToFullURIs;
	private static Map<String, String> varsVocabulary;
	

	
	public PrefixedQueryUtilsFactory(Map<String, String> prefixToFullURI, Map<String, String> varsVocabulary) {
		this.prefixesToFullURIs = new HashMap<String, String>(prefixToFullURI);
		this.varsVocabulary = new HashMap<String, String>(varsVocabulary);
	}

	public static void init(Map<String, String> prefixToFullURI, Map<String, String> varsVocabulary) {
		if (instance == null) {
			instance = new PrefixedQueryUtilsFactory(prefixToFullURI, varsVocabulary); 
		}
	}
	
	public static PrefixedQueryUtilsFactory getInstance() {
		return instance;
	}
	
	// TODO change return type with an interface
	public PrefixedStudyroomQueryBuilder getPrefixedQueryBuilder() {
		return new PrefixedStudyroomQueryBuilder(prefixesToFullURIs, varsVocabulary);
	}
	
	// TODO method that returns a query parser
}
