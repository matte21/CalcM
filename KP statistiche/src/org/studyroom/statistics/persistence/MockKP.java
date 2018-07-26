package org.studyroom.statistics.persistence;

import java.util.*;
import java.util.function.*;
import org.studyroom.model.*;

class MockKP extends Persistence {
	private static MockKP instance;
	public static MockKP getInstance(){
		if (instance==null)
			instance=new MockKP();
		return instance;
	}
	
	private final List<StudyRoom> sr;
	public MockKP(){
		sr=new ArrayList<>();
		sr.add(new StudyRoom("sr1",new Table[]{
				new Table("sr1t1",new Seat[]{
						new Seat("sr1t1s1"),
						new Seat("sr1t1s2"),
						new Seat("sr1t1s3"),
						new Seat("sr1t1s4"),
						new Seat("sr1t1s5"),
						new Seat("sr1t1s6")
				}),
				new Table("sr1t2",new Seat[]{
						new Seat("sr1t2s1"),
						new Seat("sr1t2s2"),
						new Seat("sr1t2s3"),
						new Seat("sr1t2s4"),
						new Seat("sr1t2s5"),
						new Seat("sr1t2s6")
				}),
				new Table("sr1t3",new Seat[]{
						new Seat("sr1t3s1"),
						new Seat("sr1t3s2")
				})
		},"Aula studio lab4","Alma Mater Studiorum"));
		sr.add(new StudyRoom("sr2",new Table[]{
				new Table("sr2t1",new Seat[]{
						new Seat("sr2t1s1"),
						new Seat("sr2t1s2"),
						new Seat("sr2t1s3"),
						new Seat("sr2t1s4"),
						new Seat("sr2t1s5"),
						new Seat("sr2t1s6"),
						new Seat("sr2t1s7"),
						new Seat("sr2t1s8")
				})
		},"Aula studio biblioteca","Alma Mater Studiorum"));
		sr.get(0).getTables()[0].getSeats()[1].setChairAvailable(false);
		sr.get(0).getTables()[0].getSeats()[1].setDeskAvailable(false);
		sr.get(0).getTables()[0].getSeats()[2].setChairAvailable(false);
		sr.get(0).getTables()[0].getSeats()[2].setDeskAvailable(false);
		sr.get(0).getTables()[0].getSeats()[5].setChairAvailable(false);
		sr.get(0).getTables()[0].getSeats()[5].setDeskAvailable(false);
		sr.get(0).getTables()[1].getSeats()[4].setChairAvailable(false);
		sr.get(0).getTables()[1].getSeats()[4].setDeskAvailable(false);
		sr.get(1).getTables()[0].getSeats()[1].setChairAvailable(false);
		sr.get(1).getTables()[0].getSeats()[1].setDeskAvailable(false);
		sr.get(1).getTables()[0].getSeats()[2].setChairAvailable(false);
		sr.get(1).getTables()[0].getSeats()[2].setDeskAvailable(false);
		sr.get(1).getTables()[0].getSeats()[3].setChairAvailable(false);
		sr.get(1).getTables()[0].getSeats()[3].setDeskAvailable(false);
		sr.get(1).getTables()[0].getSeats()[4].setChairAvailable(false);
		sr.get(1).getTables()[0].getSeats()[4].setDeskAvailable(false);
		sr.get(1).getTables()[0].getSeats()[5].setChairAvailable(false);
		sr.get(1).getTables()[0].getSeats()[5].setDeskAvailable(false);
		sr.get(1).getTables()[0].getSeats()[6].setChairAvailable(false);
		sr.get(1).getTables()[0].getSeats()[6].setDeskAvailable(false);
		Timer t=new Timer(true);
		t.schedule(new TimerTask(){
			@Override
			public void run(){
				if (Math.random()<.85)
					return;
				Function<Integer,Integer> f=n->(int)(Math.random()*n);
				StudyRoom r=sr.get(f.apply(sr.size()));
				Table t=r.getTables()[f.apply(r.getTables().length)];
				Seat s=t.getSeats()[f.apply(t.getSeats().length)];
				boolean b=!s.isAvailable();
				s.setChairAvailable(b);
				s.setDeskAvailable(b);
			}
		},5000,1000);
	}

	@Override
	public Collection<StudyRoom> getStudyRooms(){
		return sr;
	}
}
