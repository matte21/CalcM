package org.studyroom.statistics.view.web;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

class SessionManager {
	private static final String CHARS=IntStream.concat(IntStream.concat(IntStream.rangeClosed('0','9'),IntStream.rangeClosed('A','Z')),IntStream.rangeClosed('a','z')).mapToObj(c->""+(char)c).collect(Collectors.joining());
	private final Map<String,Session> sessions=new ConcurrentHashMap<>();
	private final Consumer<Session> finalizer;
	public SessionManager(){
		this(0,s->{});
	}
	public SessionManager(long sessionTimeout){
		this(sessionTimeout,s->{});
	}
	public SessionManager(long sessionTimeout, Consumer<Session> finalizer){
		this.finalizer=finalizer;
		if (sessionTimeout<=0)
			return;
		Timer t=new Timer(true);
		t.schedule(new TimerTask(){
			@Override
			public void run(){
				long t=System.currentTimeMillis();
				for (Map.Entry<String,Session> e : sessions.entrySet())
					synchronized (e.getValue()){
						if (e.getValue().getLastAccessTime()+sessionTimeout<t){
							sessions.remove(e.getKey());
							finalizer.accept(e.getValue());
						}
					}
			}},sessionTimeout,sessionTimeout/2);
	}
	private String generateKey(){
		String s;
		do {
			StringBuilder sb=new StringBuilder(16);
			for (byte i=0;i<16;i++)
				sb.append(CHARS.charAt((int)(Math.random()*CHARS.length())));
			s=sb.toString();
		} while (sessions.containsKey(s));
		return s;
	}
	public String createSession(){
		String id=generateKey();
		sessions.put(id,new Session());
		return id;
	}
	public boolean deleteSession(String id){
		Session s=sessions.remove(id);
		if (s!=null)
			synchronized (s){
				finalizer.accept(s);
				return true;
			}
		return false;
	}
	public Session getSession(String id){
		Session s=sessions.get(id);
		if (s!=null)
			synchronized (s){
				s.access();
			}
		return s;
	}
}
