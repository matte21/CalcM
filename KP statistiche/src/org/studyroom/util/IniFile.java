package org.studyroom.util;

import java.io.*;
import java.util.*;

public class IniFile {
	public static Map<String,Map<String,String>> read(File f) throws IOException {
		Map<String,Map<String,String>> cont=new HashMap<>();
		Map<String,String> secCont=null;
		String sec=null;
		try (BufferedReader r=new BufferedReader(new FileReader(f))){
			String l;
			while ((l=r.readLine())!=null){
				if (l.matches("\\[.*\\]")){
					if (sec!=null)
						cont.put(sec,secCont);
					sec=l.substring(1,l.length()-1);
					secCont=new HashMap<>();
				} else {
					String[] kv=l.split("=",2);
					if (sec==null || kv.length<2)
						throw new IOException("Malformed input");
					secCont.put(kv[0],kv[1]);
				}
			}
		}
		if (sec!=null)
			cont.put(sec,secCont);
		return cont;
	}
	public static void write(File f, Map<String,Map<String,String>> content){
		try (PrintWriter w=new PrintWriter(f)){
			for (Map.Entry<String,Map<String,String>> sec : content.entrySet()){
				w.println("["+sec.getKey()+"]");
				for (Map.Entry<String,String> e : sec.getValue().entrySet())
					w.println(e.getKey()+"="+e.getValue());
			}
		} catch (FileNotFoundException e){
			e.printStackTrace();
			return;
		}
	}
}
