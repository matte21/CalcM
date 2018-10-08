package query.server.utils.query.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrefixedStudyroomQueryBuilder {
	
	private String prefixes;
	
	private Map<String, String> varsVocabulary;
	
	private List<String> varsToSelect;
	private List<String> whereFilters;

	private StringBuilder builder;
	
	private static final String PREFIX = "PREFIX";
	private static final String SELECT = "SELECT";
	private static final String WHERE = "WHERE";
	private final static char SPARQL_VAR_SYMBOL = '?';
	
	public PrefixedStudyroomQueryBuilder(Map<String, String> prefixToFullURI, Map<String, String> varsVocabulary) {
		prefixes = buildPrefixes(prefixToFullURI);
		this.varsVocabulary = prependSPARQLVarSymbol(varsVocabulary);
		varsToSelect = new ArrayList<String>();
		whereFilters = new ArrayList<String>();
	}

	private String buildPrefixes(Map<String, String> prefixToFullURI) {
		builder = new StringBuilder();
		for (String prefix : prefixToFullURI.keySet()) {
			builder.append(PREFIX)
				   .append(" ")
				   .append(prefix)
				   .append(":<")
				   .append(prefixToFullURI.get(prefix))
				   .append(">\n");
		}
		return builder.toString();
	}

	private Map<String, String> prependSPARQLVarSymbol(Map<String, String> varsVocabulary) {
		Map<String, String> varsVocabularyWithSPARQLVarSymbol = new HashMap<String, String>();
		for (String varName : varsVocabulary.keySet()) {
			varsVocabularyWithSPARQLVarSymbol.put(varName, SPARQL_VAR_SYMBOL + varsVocabulary.get(varName));
		}
		return varsVocabularyWithSPARQLVarSymbol;
	}
	
	public String getQuery() {
		String selectClause = buildSelectClause();
		String whereClause = buildWhereClause();
		builder = new StringBuilder(prefixes)
				.append(selectClause)
				.append(whereClause);
		return builder.toString();
	}
	
	private String buildSelectClause() {
		builder = new StringBuilder(SELECT).append(" ");
		for (String varName : varsToSelect) {
			builder.append(varName).append(" ");
		}
		return builder.append("\n").toString();
	}

	private String buildWhereClause() {
		builder = new StringBuilder(WHERE).append("{\n");
		for (String filter : whereFilters) {
			builder.append("\t").append(filter).append(" \n");
		}
		return builder.append("}\n").toString();
	}
	
	public PrefixedStudyroomQueryBuilder selectRoomID() {
		varsToSelect.add(varsVocabulary.get("roomID"));
		return this;
	}
	
	public PrefixedStudyroomQueryBuilder selectAddress() {
		varsToSelect.add(varsVocabulary.get("address"));
		
		builder = new StringBuilder(varsVocabulary.get("roomID"));
		builder.append(" locn:address ?addrID . \n\t?addrID locn:fullAddress ")
			   .append(varsVocabulary.get("address"))
			   .append(" .");
		whereFilters.add(builder.toString());
		
		return this;
	}
	
	public PrefixedStudyroomQueryBuilder selectState() {
		varsToSelect.add(varsVocabulary.get("roomState"));
		
		builder = new StringBuilder(varsVocabulary.get("roomID"));
		builder.append(" ")
			   .append("sr:studyRoomState ")
			   .append(varsVocabulary.get("roomState"))
			   .append(" .");
		whereFilters.add(builder.toString());
		
		return this;
	}
	
	public PrefixedStudyroomQueryBuilder selectCapacity() {
		varsToSelect.add(varsVocabulary.get("capacity"));

		builder = new StringBuilder(varsVocabulary.get("roomID"));
		builder.append(" ")
			   .append("sr:hasCapacity ")
			   .append(varsVocabulary.get("capacity"))
			   .append(" .");
		whereFilters.add(builder.toString());
		
		return this;
	}
	
	public PrefixedStudyroomQueryBuilder selectAvailSeats() {
		varsToSelect.add(varsVocabulary.get("availSeats"));
		
		builder = new StringBuilder(varsVocabulary.get("roomID"));
		builder.append(" ")
			   .append("sr:availableSeats ")
			   .append(varsVocabulary.get("availSeats"))
			   .append(" .");
		whereFilters.add(builder.toString());
		
		return this;
	}
	

	// TODO: Note: when using this class, this method should be used as the very last filter
	public PrefixedStudyroomQueryBuilder selectNextTransitionInstant() {
		varsToSelect.add(varsVocabulary.get("nextTransitionInstant"));
		
		builder = new StringBuilder(varsVocabulary.get("roomID"));
		builder.append(" ")
			   .append(varsVocabulary.get("nextTransitionPredicate"))
			   .append(" ")
			   .append(varsVocabulary.get("nextTransitionInstant"))
			   .append(" . \n\t")
			   .append("VALUES ")
			   .append(varsVocabulary.get("nextTransitionPredicate"))
			   .append(" { sr:closesAt sr:opensAt }")
			   .append(" .");
		whereFilters.add(builder.toString());
		
		return this;
	}
	
	public PrefixedStudyroomQueryBuilder selectUniversity() {
		varsToSelect.add(varsVocabulary.get("university"));
		
		builder = new StringBuilder(varsVocabulary.get("roomID"));
		builder.append(" sr:inUniversity ?univ . \n\t?univ sr:hasUniversityID ")
			   .append(varsVocabulary.get("university"))
			   .append(" .");
		whereFilters.add(builder.toString());
		
		return this;
	}
	
	public PrefixedStudyroomQueryBuilder selectCoordinates() {
		varsToSelect.add(varsVocabulary.get("latitude"));
		varsToSelect.add(varsVocabulary.get("longitude"));
		
		builder = new StringBuilder(varsVocabulary.get("roomID"));
		builder.append(" ")
			   .append("geo:location ?point . \n")
			   .append("?point geo:latitude ?lat . \n")
			   .append("?point geo:longitude ?long .");
		whereFilters.add(builder.toString());
		
		return this;
	}
	
	public PrefixedStudyroomQueryBuilder addWhereFilterOnFeature(String featureName) {
		builder = new StringBuilder(varsVocabulary.get("roomID"));
		builder.append(" ")
			   .append("sr:hasStudyRoomFeature ")
			   .append("sr:" + featureName)
			   .append(" .");
		whereFilters.add(builder.toString());
		
		return this;
	}

	public PrefixedStudyroomQueryBuilder addWhereFilterOnRoomState(String roomState) {
		if (roomState == null || !roomState.trim().equals("open") || !roomState.trim().equals("closed")) {
			throw new IllegalArgumentException("roomState arg must be exactly either \"open\" or \"closed\"");
		}
		
		builder = new StringBuilder(varsVocabulary.get("roomID"));
		builder.append(" ")
			   .append("sr:studyRoomState ")
			   .append("sr:" + roomState)
		   	   .append(" .");
		whereFilters.add(builder.toString());
		
		return this;
	}
	
	// TODO Extend this to support an arbitrary number of Near Seats. Notice that the way data is stored into
	// the SIB does not make this very easy. Ideally we would modify the ontology with a nbrOfAvailNearSeats
	public PrefixedStudyroomQueryBuilder addWhereFilterOnTwoNearSeats() {
		builder = new StringBuilder(varsVocabulary.get("roomID"));
		builder.append(" ")
		   	   .append("sr:table ?table . \n")
		       .append("\t?table sr:seat ?seat1 . \n ")
		 	   .append("\t?seat1 sr:near ?seat2 . \n")
		 	   .append("\t?seat1 sr:seatState sr:available . \n")
		 	   .append("\t?seat2 sr:seatState sr:available .");
		whereFilters.add(builder.toString());
		
		return this;
	}
}

















