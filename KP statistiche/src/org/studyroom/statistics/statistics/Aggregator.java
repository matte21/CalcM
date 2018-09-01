package org.studyroom.statistics.statistics;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.studyroom.statistics.statistics.Statistic.*;

public abstract class Aggregator implements Collector<Map<String,Statistic.Value>,Map<String,Statistic.Value>,Map<String,Statistic.Value>>{
	private String name;
	private boolean additive, comparison;
	private Set<Characteristics> characteristics;
	protected Aggregator(String name, boolean additive, boolean comparison){
		this.name=name;
		this.additive=additive;
		this.comparison=comparison;
		characteristics=new HashSet<>(3);
		characteristics.add(Characteristics.CONCURRENT);
		characteristics.add(Characteristics.UNORDERED);
		try{
			if (getClass().getMethod("characteristics")==Aggregator.class.getMethod("characteristics"))
			characteristics.add(Characteristics.CONCURRENT);
		} catch (NoSuchMethodException | SecurityException e){}
	}
	public String getName(){
		return name;
	}
	public String toString(){
		return "aggregatore "+name;
	}
	public boolean isAdditive(){
		return additive;
	}
	public boolean isComparison(){
		return comparison;
	}
	protected abstract void accumulate(Map<String,Statistic.Value> accumulator, Map<String,Statistic.Value> m);
	private Map<String,Statistic.Value> combine(Map<String,Statistic.Value> m1, Map<String,Statistic.Value> m2){
		accumulate(m1,m2);
		return m1;
	}
	@Override
	public final Supplier<Map<String,Statistic.Value>> supplier(){
		return LinkedHashMap<String,Statistic.Value>::new;
	}
	@Override
	public BiConsumer<Map<String,Statistic.Value>,Map<String,Statistic.Value>> accumulator(){
		return this::accumulate;
	}
	@Override
	public BinaryOperator<Map<String,Statistic.Value>> combiner(){
		return this::combine;
	}
	@Override
	public Function<Map<String,Statistic.Value>,Map<String,Statistic.Value>> finisher(){
		return Function.identity();
	}
	@Override
	public Set<Characteristics> characteristics(){
		return characteristics;
	}
	public static final Aggregator COMPARISON=new Aggregator("confronto",false,true){
		@Override
		protected void accumulate(Map<String,Value> accumulator, Map<String,Value> m){
			m.forEach((k,v)->accumulator.put(k,v));
		}
	};
	public static final Aggregator SUM=new Aggregator("totale",true,false){
		@Override
		protected void accumulate(Map<String,Value> accumulator, Map<String,Value> m){
			m.forEach((k,v)->{
				Value v1=accumulator.getOrDefault(k,new Value(0,0));
				accumulator.put(k,new Value(v.getFull()+v1.getFull(),v.getPartial()+v1.getPartial()));
			});
		}
	};
	public static final Aggregator AVERAGE=new Aggregator("media",false,false){
		private static final String K_TOTAL="_$%PoIuYTrEwQ_%$";
		@Override
		protected void accumulate(Map<String,Value> accumulator, Map<String,Value> m){
			m.forEach((k,v)->{
				Value va=accumulator.getOrDefault(k,new Value(0,0));
				accumulator.put(k,new Value(v.getFull()+va.getFull(),v.getPartial()+va.getPartial()));
			});
			int na=accumulator.containsKey(K_TOTAL)?(int)accumulator.get(K_TOTAL).getFull():0;
			int n=m.containsKey(K_TOTAL)?(int)m.get(K_TOTAL).getFull():1;
			accumulator.put(K_TOTAL,new Value(na+n,0));
		}
		@Override
		public Function<Map<String,Statistic.Value>,Map<String,Statistic.Value>> finisher(){
			return m->{
				int n=m.containsKey(K_TOTAL)?(int)m.get(K_TOTAL).getFull():1;
				m.remove(K_TOTAL);
				m.forEach((k,v)->m.put(k,new Value(v.getFull()/n,v.getPartial()/n)));
				return m;
			};
		}
	};
	public static final Aggregator MIN=new Aggregator("minimo",false,false){
		@Override
		protected void accumulate(Map<String,Value> accumulator, Map<String,Value> m){
			m.forEach((k,v)->{
				Value v1=accumulator.getOrDefault(k,new Value(Integer.MAX_VALUE,0));
				accumulator.put(k,v1.compareTo(v)>0?v:v1);
			});
		}
	};
	public static final Aggregator MAX=new Aggregator("massimo",false,false){
		@Override
		protected void accumulate(Map<String,Value> accumulator, Map<String,Value> m){
			m.forEach((k,v)->{
				Value v1=accumulator.getOrDefault(k,new Value(Integer.MIN_VALUE,0));
				accumulator.put(k,v1.compareTo(v)<0?v:v1);
			});
		}
	};
}
