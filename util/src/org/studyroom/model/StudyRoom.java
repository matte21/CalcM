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
