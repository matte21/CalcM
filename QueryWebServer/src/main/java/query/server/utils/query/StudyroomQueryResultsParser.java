package query.server.utils.query;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sofia_kp.SSAP_sparql_response;

public class StudyroomQueryResultsParser {

	private SSAP_sparql_response results;
	private Set<Vector<String[]>> filteredResultsBuffer;
	private double refLat, refLong;
	private int rangeInKM;
	
	private Map<String, String> varsVocabulary;
	private boolean rangeSpecified;
	private boolean refLatSpecified;
	private boolean refLongSpecified;

	private static final String OPEN = "open";
	private static final String CLOSED = "closed";
	
	private static final String NEXT_TRANSITION_INSTANT_VAR_NAME = "nextTransitionInstant"; 
	
	private static final String XSD_DATE_TIMESTAMP_PATTERN = "uuuu-MM-dd'T'HH:mm:ssxxx";  
	private static final DateTimeFormatter XSD_DATE_TIMESTAMP_FORMATTER = 
																	DateTimeFormatter.ofPattern(XSD_DATE_TIMESTAMP_PATTERN);
		
	private static final double AVERAGE_RADIUS_OF_EARTH_KM = 6371;
	
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	
	// Static fields used for logging
	private final static Logger LOG = LogManager.getLogger();
	
	public StudyroomQueryResultsParser(SSAP_sparql_response results, Map<String, String> prefixToFullURI, 
									   Map<String, String> varsVocabulary) {
		
		LOG.debug("Size of results: " + results.resultsNumber());
		
		this.results = results;
		this.varsVocabulary = varsVocabulary;
		filteredResultsBuffer = null;

		rangeSpecified = false;
		refLatSpecified = false;
		refLongSpecified = false;
	}

	public Set<Map<String, String>> getResults() {
		if (filteredResultsBuffer == null) {
			filteredResultsBuffer = new HashSet<Vector<String[]>>(results.getResults());
		}
		
		if (rangeSpecified && refLatSpecified && refLongSpecified) {
			LOG.debug("Filtering on distance wrt to (" + refLat + "," + refLong + ").");
			filterOnDistance();
			LOG.debug("Filtering on distance done.");
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

	private void filterOnDistance() {
		Set<Vector<String[]>> roomsToFilterOutForDistance = new HashSet<Vector<String[]>>(); 
		for (Vector<String[]> singleRoomResults : filteredResultsBuffer) {
			double roomLat = Double.NaN;
			double roomLong = Double.NaN;
			for (String[] roomVar : singleRoomResults) {
				LOG.debug("Checking variable " + roomVar[0]);
				if (roomVar[0].equalsIgnoreCase(LATITUDE)) {
					roomLat = Double.parseDouble(roomVar[2]);
					LOG.debug("Found room latitude: " + roomLat);
				}
				if (roomVar[0].equalsIgnoreCase(LONGITUDE)) {
					roomLong = Double.parseDouble(roomVar[2]);
					LOG.debug("Found room longitude: " + roomLong);
				}
			}
			if (!Double.isNaN(roomLat) && !Double.isNaN(roomLong) && !roomCoordWithinRange(roomLat, roomLong)) {
				LOG.debug("Room with (" + roomLat + "," + roomLong + ") coords found"); 
				roomsToFilterOutForDistance.add(singleRoomResults);
			}
		}
		filteredResultsBuffer = filteredResultsBuffer.parallelStream().filter(resultEntry -> {
			return !roomsToFilterOutForDistance.contains(resultEntry);
		}).collect(Collectors.<Vector<String[]>>toSet());
	}
	
	private boolean roomCoordWithinRange(double roomLat, double roomLong) {
		double latDistance = Math.toRadians(refLat - roomLat);
	    double lngDistance = Math.toRadians(refLong - roomLong);

	    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
	      + Math.cos(Math.toRadians(refLat)) * Math.cos(Math.toRadians(roomLat))
	      * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

	    int distance = (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_KM * c));
	    LOG.debug("Room distant " + distance + " kms found.");
	    return distance <= rangeInKM;
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
	
	public StudyroomQueryResultsParser roomsInCurrentStateForAtLeast(int minutes) {
		if (filteredResultsBuffer == null) {
			filteredResultsBuffer = new HashSet<Vector<String[]>>(results.getResults());
		}
		Instant nowInstant = Instant.now();
		filteredResultsBuffer = filteredResultsBuffer.parallelStream()
							 	.filter(resultEntry -> {
								for (String[] resVar : resultEntry) {
									if (resVar[0].equalsIgnoreCase(varsVocabulary.get("nextTransitionInstant"))) {
										boolean roomMatches = isInCurrentStateForAtLeast(minutes, resVar[2], nowInstant);
										return roomMatches;
									}
								}
								throw new IllegalStateException("Received results from SIB with no next transition instant."
								 		+ " This should NEVER happen.");
							}).collect(Collectors.<Vector<String[]>>toSet());
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
	
	private boolean isInCurrentStateForAtLeast(int minutes, String nextTransitionDateTimestampString, Instant nowInstant) {
		TemporalAccessor nextTransitionDateTimestamp = 
				XSD_DATE_TIMESTAMP_FORMATTER.parse(nextTransitionDateTimestampString);
		long nextTransEpochSeconds = Instant.from(nextTransitionDateTimestamp).getEpochSecond();
		long nowEpochSeconds = nowInstant.getEpochSecond();
		return (nextTransEpochSeconds - nowEpochSeconds) / 60 > minutes;
	}

	public StudyroomQueryResultsParser removeRoomWithID(String roomID) {
		if (filteredResultsBuffer == null) {
			filteredResultsBuffer = new HashSet<Vector<String[]>>(results.getResults());
		}
		filteredResultsBuffer = filteredResultsBuffer.parallelStream()
			 	.filter(resultEntry -> {
				for (String[] resVar : resultEntry) {
					if (resVar[0].equalsIgnoreCase(varsVocabulary.get("roomID"))) {
						return !roomID.equalsIgnoreCase(resVar[2]);
					}
				}
				throw new IllegalStateException("Received results from SIB with no Room ID."
				 		+ " This should NEVER happen.");
			 	}).collect(Collectors.<Vector<String[]>>toSet());
		return this;
	}

	public void addRangeInKM(int rangeInKM) {
		this.rangeInKM = rangeInKM;
		rangeSpecified = true;
	}

	public void addLatitude(double refLat) {
		this.refLat = refLat;
		refLatSpecified = true;
	}

	public void addLongitude(double refLong) {
		this.refLong = refLong;
		refLongSpecified = true;
	}
}
