package org.studyroom.demo.viewmodel;

import java.util.*;
import java.util.stream.*;
import org.studyroom.demo.kp.*;
import org.studyroom.model.*;
import org.studyroom.viewmodel.*;

public class CreateViewModel extends ViewModel implements ICreateViewModel {
	private static int nextRoomID=1;
	private final KPSensorSimulator kp;
	private final RoomViewModel roomvm;
	private String id, name, university;
	private boolean idValid, nameValid;
	private final ArrayList<Integer> tables=new ArrayList<>();
	public CreateViewModel(KPSensorSimulator kp, RoomViewModel rvm){
		this.kp=kp;
		roomvm=rvm;
		reset();
	}

	public void reset(){
		setIdValid(true);
		setNameValid(true);
		for (;kp.existsID("room"+nextRoomID);nextRoomID++);
		setId("room"+nextRoomID);
		Collection<String> lu=roomvm.getUniversities();
		setUniversity(lu.isEmpty()?"Universita' di Bologna":lu.iterator().next());
		String name="Aula studio "+nextRoomID;
		for (int i=1;kp.existsStudyRoom(university,name);name="Aula studio "+nextRoomID+" ("+(++i)+")");
		setName(name);
		firePropertyChange("tables",tables,Collections.emptyList());
		tables.clear();
	}

	@Override
	public String getId(){
		return id;
	}
	@Override
	public void setId(String id){
		if (this.id==null || !this.id.equals(id)){
			firePropertyChange("id",this.id,id);
			this.id=id;
		}
	}
	@Override
	public boolean isIdValid(){
		return idValid;
	}
	private void setIdValid(boolean valid){
		if (valid!=idValid){
			idValid=valid;
			firePropertyChange("idValid",!valid,valid);
		}
	}
	@Override
	public boolean validateID(){
		boolean valid=id.matches("\\w+") && !kp.existsID(id);
		setIdValid(valid);
		return valid;
	}

	@Override
	public String getName(){
		return name;
	}
	@Override
	public void setName(String name){
		if (this.name==null || !this.name.equals(name)){
			firePropertyChange("name",this.name,name);
			this.name=name;
		}
	}
	@Override
	public boolean isNameValid(){
		return nameValid;
	}
	private void setNameValid(boolean valid){
		if (valid!=nameValid){
			nameValid=valid;
			firePropertyChange("nameValid",!valid,valid);
		}
	}
	@Override
	public boolean validateName(){
		boolean valid=!kp.existsStudyRoom(university,name);
		setNameValid(valid);
		return valid;
	}

	@Override
	public String getUniversity(){
		return university;
	}
	@Override
	public void setUniversity(String university){
		if (this.university==null || !this.university.equals(university)){
			firePropertyChange("university",this.university,university);
			this.university=university;
		}
	}

	@Override
	public List<Integer> getTables(){
		return tables;
	}
	@Override
	public void addTable(){
		tables.add(1);
		fireListInsertion("tables",1,tables.size()-1);
	}
	@Override
	public void addSeat(int tableIndex){
		int n=tables.get(tableIndex);
		tables.set(tableIndex,n+1);
		fireListChange("tables",n,n+1,tableIndex);
	}
	@Override
	public void removeSeat(int tableIndex){
		int n=tables.get(tableIndex);
		if (n>1){
			tables.set(tableIndex,n-1);
			fireListChange("tables",n,n-1,tableIndex);
		} else {
			tables.remove(tableIndex);
			fireListRemoval("tables",n,tableIndex);
		}
	}

	@Override
	public void cancel(){
		roomvm.setView("room");
	}
	@Override
	public void createStudyRoom(){
		if (!(validateID() && validateName()))
			return;
		StudyRoom sr=new StudyRoom(id,IntStream.rangeClosed(1,tables.size()).mapToObj(i->new Table(id+"t"+i,IntStream.rangeClosed(1,tables.get(i-1)).mapToObj(j->kp.sensorSeatForSeat(id+"t"+i+"s"+j)).toArray(Seat[]::new))).toArray(Table[]::new),name,university);
		roomvm.addRoom(sr);
		roomvm.setView("room");
		nextRoomID++;
		new Thread(this::reset).start();
	}
}
