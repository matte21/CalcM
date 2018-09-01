package org.studyroom.model;

import java.io.*;
import java.time.*;
import java.util.*;

public class StudyRoom implements Serializable {
	private static final long serialVersionUID=1;
	private String URI,name,university;
	private Table[] tables;
	private String[] features;
	private int capacity,availableSeats;
	private boolean open;
	private LocalDateTime openTime,closeTime;
	public StudyRoom(String URI, Table[] tables, String name, String university, String...features){
		this(URI,tables);
		this.name=name;
		this.university=university;
		this.features=features;
	}
	public StudyRoom(String URI, int capacity, String name, String university, String...features){
		this(URI,capacity);
		this.name=name;
		this.university=university;
		this.features=features;
	}
	public StudyRoom(String URI, Table[] tables){
		this.URI=URI;
		this.tables=tables;
	}
	public StudyRoom(String URI, int capacity){
		this(URI,new Table[0]);
		this.capacity=capacity;
		this.availableSeats=capacity;
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
		return tables.length==0?capacity:(int)Arrays.stream(tables).flatMap(t->Arrays.stream(t.getSeats())).count();
	}
	public int getAvailableSeats(){
		return tables.length==0?availableSeats:(int)Arrays.stream(tables).flatMap(t->Arrays.stream(t.getSeats())).filter(Seat::isAvailable).count();
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
	public LocalDateTime getOpenTime(){
		return openTime;
	}
	public void setOpenTime(LocalDateTime openTime){
		this.openTime=openTime;
	}
	public LocalDateTime getCloseTime(){
		return closeTime;
	}
	public void setCloseTime(LocalDateTime closeTime){
		this.closeTime=closeTime;
	}
	public Table getTable(String URI){
		for (Table t : tables)
			if (t.getURI().equals(URI))
				return t;
		return null;
	}
	public Seat getSeat(String URI){
		for (Table t : tables){
			Seat s=t.getSeat(URI);
			if (s!=null)
				return s;
		}
		return null;
	}
	/*public Seat getSeat2(String URI){	//si è dimostrata meno efficiente se 
		return Arrays.stream(tables).flatMap(t->Arrays.stream(t.getSeats())).filter(s->s.getURI().equals(URI)).findFirst().orElse(null);
	}*/
	@Override
	public String toString(){
		return URI;
	}
	@Override
	public boolean equals(Object o){
		return o instanceof StudyRoom && ((StudyRoom)o).getURI().equals(URI);
	}
	@Override
	public int hashCode(){
		return URI.hashCode();
	}
}
