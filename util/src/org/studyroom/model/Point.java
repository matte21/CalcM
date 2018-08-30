package org.studyroom.model;

public class Point {
	private double lat,lon,alt;

	public double getLat(){
		return lat;
	}
	public double getLon(){
		return lon;
	}
	public double getAlt(){
		return alt;
	}
	@Override
	public String toString(){
		return String.format("(%.6f;%.6f;%.0f)",lat,lon,alt);
	}
}
