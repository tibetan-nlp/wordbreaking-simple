package org.tbrc.tools.wordbreak.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SortTibetanUnicodeStrings extends SortTibetanUnicode {
	
	public SortTibetanUnicodeStrings() { }

	public int compare(Object x, Object y ) {
		return dz_BTCollator.compare((String)x, (String)y);
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<String> sort(List<String> list) {
		ArrayList<String> tmp = new ArrayList<String>();
		
		if (list == null) return tmp;
		
		for (int i = 0; i < list.size(); i++) {
			tmp.add(list.get(i));
		}
		
		Collections.sort(tmp, this);
		
		return tmp;
	}
}
