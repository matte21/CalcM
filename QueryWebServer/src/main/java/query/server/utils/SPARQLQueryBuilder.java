package query.server.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SPARQLQueryBuilder {

	private String query;
	
	private Map<String, String> prefixes;
	private List<String> selectClauseVariables;
	private List<String> whereClauseFilters;

	public SPARQLQueryBuilder() {
		init();
	}
	
	public void reset() {
		init();
	}
	
	private void init() {
		query = null;
		prefixes = new HashMap<>();
		selectClauseVariables = new ArrayList<>();
		whereClauseFilters = new ArrayList<>();		
	}
	
	public void addPrefixMapping(String prefix, String fullName) {
		prefixes.put(prefix, fullName);
	}
	
	public void addVarToSelect(String varName) {
		selectClauseVariables.add(varName);
	}
	
	public void addWhereFilter(String filter) {
		whereClauseFilters.add(filter);
	}
	
	public String getQuery() {
		if (query != null) {
			return query;
		}
		
		String prefixes = buildPrefixes();
		String selectClause = buildSelectClause();
		String whereClause = buildWhereClause();
		
		query = prefixes + selectClause + whereClause;
		return query;
	}
	
	private String buildPrefixes() {
		StringBuilder prefixesBuilder = new StringBuilder();
		for (Map.Entry<String, String> prefix : prefixes.entrySet()) {
			prefixesBuilder.append("PREFIX " + prefix.getKey() + ":<" + prefix.getValue() + "> \n");
		}
		return prefixesBuilder.toString();
	}
	
	private String buildSelectClause() {
		StringBuilder selectClauseBuilder = new StringBuilder("SELECT");
		for (String varName : selectClauseVariables) {
			selectClauseBuilder.append(" " + varName);
		}
		selectClauseBuilder.append(" \n");
		return selectClauseBuilder.toString();
	}

	private String buildWhereClause() {
		StringBuilder whereClauseBuilder = new StringBuilder("WHERE { \n");
		for (String filter : whereClauseFilters) {
			whereClauseBuilder.append("\t" + filter + " . \n");
		}
		whereClauseBuilder.append("} \n");
		return whereClauseBuilder.toString();	
	}
	
}
