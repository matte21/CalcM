package org.studyroom.model;

import java.io.*;
import java.util.*;

public class StudyRoom implements Serializable {
	private static final long serialVersionUID=1;
	private String URI,name,university;
	private Table[] tables;
	private String[] features;
	//private int capacity,availableSeats;
	private boolean open;
	private Date openTime,closeTime;
	public StudyRoom(String URI, Table[] tables, String name, String university, String...features){
		this(URI,tables);
		this.name=name;
		this.university=university;
		this.features=features;
	}
	public StudyRoom(String URI, Table[] tables){
		this.URI=URI;
		this.tables=tables;
		//capacity=(int)Arrays.stream(tables).flatMap(t->Arrays.stream(t.getSeats())).count();
		//availableSeats=(int)Arrays.stream(tables).flatMap(t->Arrays.stream(t.getSeats())).filter(Seat::isAvailable).count();
	}
	public String getURI(){
		return URI;
	}
	public String getName(){
		return name;
	}
	public String[] getFeatures(){
		return Arrays.copyOf(features,features.length);
	}
	public int getCapacity(){
		return /*capacity*/(int)Arrays.stream(tables).flatMap(t->Arrays.stream(t.getSeats())).count();
	}
	public int getAvailableSeats(){
		return /*availableSeats*/(int)Arrays.stream(tables).flatMap(t->Arrays.stream(t.getSeats())).filter(Seat::isAvailable).count();
	}
	public boolean isOpen(){
		return open;
	}
	public void setOpen(boolean open){
		this.open=open;
	}
	public Table[] getTables(){
		return Arrays.copyOf(tables,tables.length);
	}
	public String getUniversity(){
		return university;
	}
	public Date getOpenTime(){
		return openTime;
	}
	public void setOpenTime(Date openTime){
		this.openTime=openTime;
	}
	public Date getCloseTime(){
		return closeTime;
	}
	public void setCloseTime(Date closeTime){
		this.closeTime=closeTime;
	}
	@Override
	public String toString(){
		return URI;
	}
}
