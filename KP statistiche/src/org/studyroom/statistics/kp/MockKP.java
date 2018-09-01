package org.studyroom.statistics.kp;

import java.util.*;
import java.util.function.*;
import org.studyroom.model.*;
import org.studyroom.statistics.persistence.*;
import static org.studyroom.statistics.persistence.SeatStateChange.*;

public class MockKP {
	private static MockKP instance=new MockKP();
	public static MockKP getInstance(){
		return instance;
	}
	private final StudyRoom[] sr;
	private final KPPersistence persistence;
	public MockKP(){
		sr=new StudyRoom[]{
			new StudyRoom("sr1",new Table[]{
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
				},"Aula studio lab4","Alma Mater Studiorum"),
			new StudyRoom("sr2",new Table[]{
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
				},"Aula studio biblioteca","Alma Mater Studiorum")};
		persistence=new KPPersistence(new StudyRoom[]{
				new StudyRoom("sr1",sr[0].getCapacity(),"Aula studio lab4","Alma Mater Studiorum"),
				new StudyRoom("sr2",sr[1].getCapacity(),"Aula studio biblioteca","Alma Mater Studiorum")
		});
	}
	public void start(){
		initSeat(0,0,1,CHAIR_OCCUPIED);
		initSeat(0,0,1,DESK_OCCUPIED);
		initSeat(0,0,2,CHAIR_OCCUPIED);
		initSeat(0,0,2,DESK_OCCUPIED);
		initSeat(0,0,4,CHAIR_OCCUPIED);
		initSeat(0,0,4,DESK_OCCUPIED);
		initSeat(0,0,5,CHAIR_OCCUPIED);
		initSeat(0,0,5,DESK_OCCUPIED);
		initSeat(0,1,4,CHAIR_OCCUPIED);
		initSeat(0,1,4,DESK_OCCUPIED);
		initSeat(1,0,1,CHAIR_OCCUPIED);
		initSeat(1,0,1,DESK_OCCUPIED);
		initSeat(1,0,2,CHAIR_OCCUPIED);
		initSeat(1,0,2,DESK_OCCUPIED);
		initSeat(1,0,3,CHAIR_OCCUPIED);
		initSeat(1,0,3,DESK_OCCUPIED);
		initSeat(1,0,4,CHAIR_OCCUPIED);
		initSeat(1,0,4,DESK_OCCUPIED);
		initSeat(1,0,5,CHAIR_OCCUPIED);
		initSeat(1,0,5,DESK_OCCUPIED);
		Timer t=new Timer(true);
		t.schedule(new TimerTask(){
			@Override
			public void run(){
				if (Math.random()<.85)
					return;
				Function<Integer,Integer> f=n->(int)(Math.random()*n);
				StudyRoom r=sr[f.apply(sr.length)];
				Table t=r.getTables()[f.apply(r.getTables().length)];
				Seat s=t.getSeats()[f.apply(t.getSeats().length)];
				boolean b=Math.random()<.5;
				SeatStateChange o,c=b?(s.isChairAvailable()?CHAIR_OCCUPIED:CHAIR_FREE):(s.isDeskAvailable()?DESK_OCCUPIED:DESK_FREE);
				if (c.isChairChanged()){
					s.setChairAvailable(c.isFree());
					o=s.isDeskAvailable()?DESK_FREE:DESK_OCCUPIED;
				} else {
					s.setDeskAvailable(c.isFree());
					o=s.isChairAvailable()?CHAIR_FREE:CHAIR_OCCUPIED;
				}
				persistence.notifyChange(s.getURI(),t.getURI(),r.getURI(),c,o);
				System.out.println("Posti occupati: "+(sr[0].getCapacity()-sr[0].getAvailableSeats())+", "+(sr[1].getCapacity()-sr[1].getAvailableSeats()));
			}
		},1000,1000);
	}
	/**unchecked input parameters*/
	private void initSeat(int isr, int it, int is, SeatStateChange state){
		StudyRoom sr=this.sr[isr];
		Table t=sr.getTables()[it];
		Seat s=t.getSeats()[is];
		SeatStateChange other=null;
		switch (state){
		case CHAIR_FREE:
			s.setChairAvailable(true);
			other=s.isDeskAvailable()?DESK_FREE:DESK_OCCUPIED;
			break;
		case CHAIR_OCCUPIED:
			s.setChairAvailable(false);
			other=s.isDeskAvailable()?DESK_FREE:DESK_OCCUPIED;
			break;
		case DESK_FREE:
			s.setDeskAvailable(true);
			other=s.isChairAvailable()?CHAIR_FREE:CHAIR_OCCUPIED;
			break;
		case DESK_OCCUPIED:
			s.setDeskAvailable(false);
			other=s.isChairAvailable()?CHAIR_FREE:CHAIR_OCCUPIED;
			break;
		}
		persistence.initState(s.getURI(),t.getURI(),sr.getURI(),state,other);
	}
}
