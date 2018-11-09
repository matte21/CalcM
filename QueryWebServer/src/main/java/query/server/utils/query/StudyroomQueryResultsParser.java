package query.server.utils.query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import sofia_kp.SSAP_sparql_response;

public class StudyroomQueryResultsParser {

	private SSAP_sparql_response results;
	private Set<Vector<String[]>> filteredResultsBuffer;
	
	private Map<String, String> varsVocabulary;

	private static final String OPEN = "open";
	private static final String CLOSED = "closed";
	
	private static final String NEXT_TRANSITION_INSTANT_VAR_NAME = "nextTransitionInstant"; 
	
	public StudyroomQueryResultsParser(SSAP_sparql_response results, Map<String, String> prefixToFullURI, 
									   Map<String, String> varsVocabulary) {
		
		this.results = results;
		this.varsVocabulary = varsVocabulary;
		filteredResultsBuffer = null;
	}

	public Set<Map<String, String>> getResults() {
		if (filteredResultsBuffer == null) {
			filteredResultsBuffer = new HashSet<Vector<String[]>>(results.getResults());
		}
		
		Set<Map<String, String>> essentialResults = new HashSet<Map<String, String>>();
		
		for (Vector<String[]> singleRowResults : filteredResultsBuffer) {
			Map<String, String> essentialSingleRow = new HashMap<String, String>();
			for (String[] singleVarInSingleRow : singleRowResults) {
				// The returned var value is in the form <prefix>#<value> but we only care about the value.
				// Notice that some values are not prefixed, that is why we need the if-else statement.
				// FIXME the solution used here (string.split around '#') is brittle, if there are more than 
				// one # in the result it doesn't work. A more solid solution should be adopted.
				String[] splitValue = singleVarInSingleRow[2].split("#");
				String varValueWithNoPrefix;
				if (splitValue.length > 1) {
					// Value is prefixed
					varValueWithNoPrefix = splitValue[1];
				} else {
					// Value is not prefixed
					varValueWithNoPrefix = splitValue[0];					
				}
				essentialSingleRow.put(singleVarInSingleRow[0], varValueWithNoPrefix);
			}
			essentialResults.add(essentialSingleRow);
		}
		
		return essentialResults;
	}

	public boolean varIsNextTransitionInstant(String varName) {
		return varsVocabulary.get(NEXT_TRANSITION_INSTANT_VAR_NAME).equalsIgnoreCase(varName.trim());
	}
	
	public StudyroomQueryResultsParser onlyOpenRooms() {
		if (filteredResultsBuffer == null) {
			filteredResultsBuffer = new HashSet<Vector<String[]>>(results.getResults());
		}
		filterResultsOnState(OPEN);
		return this;
	}
	
	public StudyroomQueryResultsParser onlyClosedRooms() {
		if (filteredResultsBuffer == null) {
			filteredResultsBuffer = new HashSet<Vector<String[]>>(results.getResults());
		}
		filterResultsOnState(CLOSED);	
		return this;
	}
	
	public void resetFilters() {
		filteredResultsBuffer = null;
	}
	
	private void filterResultsOnState(String state) {
		filteredResultsBuffer = filteredResultsBuffer.parallelStream()
				 .filter(resultEntry -> {
					 for (String[] resVar : resultEntry) {
						 if (resVar[0].equalsIgnoreCase(varsVocabulary.get("roomState"))) {
							 return resVar[2].endsWith(state);
						 }
					 }
					 throw new IllegalStateException("Received results from SIB with no room state."
					 		+ " This should NEVER happen.");
				 }).collect(Collectors.<Vector<String[]>>toSet());		
	}	
	
}
