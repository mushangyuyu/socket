package com.test.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Test {
	public static void main(String[] args) {
		List list=new ArrayList();
		list.add("1");
		list.add("2");
		System.out.println(list);
		Map<String, String > map=new HashMap<>();
		map.put("1","12");
		map.put("2", "小明");
		Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
		       while (it.hasNext()) {
		            Entry<String, String> entry = it.next();
	              System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
		       }
		       System.out.println(map);
	}
}
