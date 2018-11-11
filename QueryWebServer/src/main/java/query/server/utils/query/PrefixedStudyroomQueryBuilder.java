package query.server.utils.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrefixedStudyroomQueryBuilder {
	
	private String prefixes;
	
	private Map<String, String> varsVocabulary;
	
	private List<String> varsToSelect;
	private List<String> whereFilters;
	private List<String> filterClauses;
	
	private StringBuilder builder;
	
	private static final String PREFIX = "PREFIX";
	private static final String SELECT = "SELECT";
	private static final String WHERE = "WHERE";
	private String DISTINCT = "";

	private boolean latInSelect;
	private boolean longInSelect;
	
	private static final char SPARQL_VAR_SYMBOL = '?';
	
	public PrefixedStudyroomQueryBuilder(Map<String, String> prefixToFullURI, Map<String, String> varsVocabulary) {
		prefixes = buildPrefixes(prefixToFullURI);
		this.varsVocabulary = prependSPARQLVarSymbol(varsVocabulary);
		varsToSelect = new ArrayList<String>();
		whereFilters = new ArrayList<String>();
		filterClauses = new ArrayList<String>();
		latInSelect = false;
		longInSelect = false;
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
		builder = new StringBuilder(SELECT).append(" ").append(DISTINCT).append(" ");
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
		if (filterClauses.size() > 0) {
			builder.append("\tFILTER ( ");
			for (String filterClause : filterClauses) {
				builder.append(filterClause).append(" && ");
			}
			// Remove spurius "&&" at the end of the filter clause
			builder.delete(builder.length() - 3, builder.length());	
			builder.append(")\n").toString();
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
			   .append("\n\t")
			   .append("FILTER ( ( ")
			   .append(varsVocabulary.get("nextTransitionPredicate"))
			   .append(" = sr:closesAt ) || ( ")
			   .append(varsVocabulary.get("nextTransitionPredicate"))
			   .append(" = sr:opensAt ")
			   .append(") )");
		whereFilters.add(builder.toString());
		
		return this;
	}

	// TODO: Note: when using this class, this method should be used as the very last filter
	public PrefixedStudyroomQueryBuilder selectNextTransitionInstantAndPredicate() {
		varsToSelect.add(varsVocabulary.get("nextTransitionInstant"));
		varsToSelect.add(varsVocabulary.get("nextTransitionPredicate"));
		
		builder = new StringBuilder(varsVocabulary.get("roomID"));
		builder.append(" ")
			   .append(varsVocabulary.get("nextTransitionPredicate"))
			   .append(" ")
			   .append(varsVocabulary.get("nextTransitionInstant"))
			   .append(" .");
		whereFilters.add(builder.toString());
		
		builder = new StringBuilder("( ( ");
		builder.append(varsVocabulary.get("nextTransitionPredicate"))
		   	   .append(" = sr:closesAt ) || ( ")
		   	   .append(varsVocabulary.get("nextTransitionPredicate"))
		   	   .append(" = sr:opensAt ) )");
		filterClauses.add(builder.toString());
		   
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
		if (roomState == null || !(roomState.trim().equals("open") || roomState.trim().equals("closed"))) {
			throw new IllegalArgumentException("roomState arg must be exactly either \"open\" or \"closed\". " 
					+ roomState + " was received.");
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
	// the SIB does not make this easy. Ideally we would modify the ontology with a nbrOfAvailNearSeats 
	// predicate with domain sr:Seat and range xsd:Int.
	public PrefixedStudyroomQueryBuilder addWhereFilterOnTwoNearSeats() {
		builder = new StringBuilder(varsVocabulary.get("roomID"));
		builder.append(" ")
		   	   .append("sr:table ?table . \n")
		       .append("\t?table sr:seat ?seat1 . \n ")
		 	   .append("\t?seat1 sr:near ?seat2 . \n")
		 	   .append("\t?seat1 sr:seatState sr:available . \n")
		 	   .append("\t?seat2 sr:seatState sr:available .");
		whereFilters.add(builder.toString());
		DISTINCT = "DISTINCT";
		
		return this;
	}
	
	public PrefixedStudyroomQueryBuilder addWhereFilterOnAvailSeats(int numAvailSeats) {
		if (numAvailSeats < 1) {
			throw new IllegalArgumentException("numAvailSeats arg must be bigger than 1");
		}

		builder = new StringBuilder("(");
		builder.append(" ")
			   .append(varsVocabulary.get("availSeats"))
			   .append(" >= ")
			   .append("\"" + numAvailSeats  + "\"")
			   .append(" )");
		filterClauses.add(builder.toString());
		
		return this;
	}

	public PrefixedStudyroomQueryBuilder addWhereFilterOnThreeNearSeats() {
		builder = new StringBuilder(varsVocabulary.get("roomID"));
		builder.append(" ")
		   	   .append("sr:table ?table . \n")
		       .append("\t?table sr:seat ?seat1 . \n ")
		 	   .append("\t?seat1 sr:near ?seat2 . \n")
		 	   .append("\t?seat2 sr:near ?seat3 . \n")
		 	   .append("\t?seat1 sr:seatState sr:available . \n")
		 	   .append("\t?seat2 sr:seatState sr:available . \n")
		 	   .append("\t?seat3 sr:seatState sr:available . \n");
		whereFilters.add(builder.toString());
		
		builder = new StringBuilder("(");
		builder.append(" ")
			   .append("?seat1")
			   .append(" != ")
			   .append("?seat3")			   
			   .append(" )");
		filterClauses.add(builder.toString());
		
		DISTINCT = "DISTINCT";
		
		return this;
	}

	public PrefixedStudyroomQueryBuilder selectLatitude() {
		varsToSelect.add(varsVocabulary.get("latitude"));
		
		if (longInSelect) {
			builder = new StringBuilder("?point geo:lat ?latitude .");
		} else {
			builder = new StringBuilder(varsVocabulary.get("roomID"));
			builder.append(" ")
				   .append("geo:location ?point . \n")
				   .append("?point geo:lat ?latitude .");
		}
		whereFilters.add(builder.toString());		
		
		latInSelect = true;

		return this;
	}

	public PrefixedStudyroomQueryBuilder selectLongitude() {
		varsToSelect.add(varsVocabulary.get("longitude"));
		
		if (latInSelect) {
			builder = new StringBuilder("?point geo:long ?longitude .");
		} else {
			builder = new StringBuilder(varsVocabulary.get("roomID"));
			builder.append(" ")
				   .append("geo:location ?point . \n")
				   .append("?point geo:long ?longitude .");		
		}
		whereFilters.add(builder.toString());

		longInSelect = true;

		return this;
	}
	
}

















