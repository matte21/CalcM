//import java.util.stream.*;
//import org.studyroom.model.*;
//
//public class Test {
//	public static void main(String[] args){
//		final int nt=10000,ns=3000;
//		final String uri="S1111-900";
//		
//		StudyRoom sr=new StudyRoom("SR1",IntStream.range(0,nt).mapToObj(i->new Table("T"+i,
//				IntStream.range(0,ns).mapToObj(j->new Seat("S"+i+"-"+j)).toArray(Seat[]::new)
//			)).toArray(Table[]::new));
//		System.out.println("Avvio test con "+nt+" tavoli e "+ns+" posti per tavolo");
//		long t0=System.currentTimeMillis();
//		Seat s1=sr.getSeat(uri);
//		long t1=System.currentTimeMillis();
//		Seat s2=sr.getSeat2(uri);
//		long t2=System.currentTimeMillis();
//		System.out.println("getSeat:  "+(t1-t0)+"ms -> "+s1);
//		System.out.println("getSeat2: "+(t2-t1)+"ms -> "+s2);
//	}
//}
