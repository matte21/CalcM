package org.studyroom.statistics.view.web;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

public class Session implements Iterable<Map.Entry<String,Object>> {
	private Map<String,Object> m=new ConcurrentHashMap<>();
	private long lastAccess=System.currentTimeMillis();
	void access(){
		lastAccess=System.currentTimeMillis();
	}
	public long getLastAccessTime(){
		return lastAccess;
	}
	public synchronized void set(String key, Object value){
		lastAccess=System.currentTimeMillis();
		m.put(key,value);
	}
	public synchronized void setAll(Map<? extends String,? extends Object> entries){
		lastAccess=System.currentTimeMillis();
		m.putAll(entries);
	}
	public synchronized void setAll(Collection<Map.Entry<? extends String,? extends Object>> entries){
		lastAccess=System.currentTimeMillis();
		for (Map.Entry<? extends String,? extends Object> e : entries)
			m.put(e.getKey(),e.getValue());
	}
	public synchronized Object remove(String key){
		lastAccess=System.currentTimeMillis();
		return m.remove(key);
	}
	public synchronized Object get(String key){
		lastAccess=System.currentTimeMillis();
		return m.get(key);
	}
	public synchronized Object getOrDefault(String key, Object value){
		lastAccess=System.currentTimeMillis();
		return m.getOrDefault(key,value);
	}
	public synchronized Object getOrDefault(String key, Supplier<Object> value){
		lastAccess=System.currentTimeMillis();
		if (m.containsKey(key))
			return m.get(key);
		else
			return value.get();
	}
	public synchronized Object getOrSetDefault(String key, Object value){
		lastAccess=System.currentTimeMillis();
		if (m.containsKey(key))
			return m.get(key);
		else {
			m.put(key,value);
			return value;
		}
	}
	public synchronized Object getOrSetDefault(String key, Supplier<Object> value){
		lastAccess=System.currentTimeMillis();
		if (m.containsKey(key))
			return m.get(key);
		else {
			Object v=value.get();
			m.put(key,v);
			return v;
		}
	}
	public boolean containsKey(String key){
		return m.containsKey(key);
	}
	public synchronized void forEach(BiConsumer<? super String,? super Object> action){
		lastAccess=System.currentTimeMillis();
		m.forEach(action);
	}
	public synchronized Stream<Map.Entry<String,Object>> stream(){
		lastAccess=System.currentTimeMillis();
		return m.entrySet().stream();
	}
	public synchronized Stream<Map.Entry<String,Object>> parallelStream(){
		lastAccess=System.currentTimeMillis();
		return m.entrySet().parallelStream();
	}
	@Override
	public synchronized Iterator<Map.Entry<String,Object>> iterator(){
		lastAccess=System.currentTimeMillis();
		return m.entrySet().iterator();
	}
}
