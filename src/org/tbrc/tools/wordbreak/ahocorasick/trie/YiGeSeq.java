package org.tbrc.tools.wordbreak.ahocorasick.trie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import static java.lang.Math.min;

public class YiGeSeq implements Iterable<YiGe>, Comparable<Object> {

	// for reference in constructing a more robust regex to split with - would be used in 
	// toYiGe for running text perhaps. Probably should parse running text into clauses
	// and the parse each clause into list of yiGe.
	protected static boolean isTibLetterOrDigit(int c) {
		return ('\u0F40' <= c && c <= '\u0F83') || ('\u0F90' <= c && c <= '\u0FBC') || ('\u0F20' <= c && c <= '\u0F33') || (c == '\u0F00');
	}
	
	protected static String simpleTibPuct = "[\u0F0B-\u0F12]";
	protected static String nonLetterRegEx = "[^\u0F00\u0F40-\u0F83\u0F90-\u0FBC]";
	protected static String nonLetterDigitRegEx = "[^\u0F00\u0F20-\u0F40-\u0F33\u0F83\u0F90-\u0FBC]";

	/**
	 * This routine "parses" the String argument to produce a sequence of the constituent
	 * yi ge that are present in the String.
	 * <p>
	 * A principal use of this routine is in <code>Trie.addKeyword</code> to build the 
	 * dictionary used for wordbreaking; hence, the argument is named <code>keyword</code>. 
	 * This routine may also be used to parse an input string for breaking into its sequence 
	 * of yi ge.
	 * 
	 * @param keyword the Unicode Tibetan string to be chunked into yi ge
	 * @param word a List<YiGe> to which the yi ge in keyword are added
	 * @return
	 */	
	public static List<YiGe> toYiGe(String keyword, List<YiGe> word) {
		
		if (keyword == null || keyword.isEmpty()){
			return word;
		}

		// parse the keyword into a list of syllables using tsheg - rinchen pung shad
		String[] strs = keyword.split(nonLetterRegEx);

		for (String s : strs) {
			if (! s.isEmpty()) {
				word.add(new YiGe(s));
			}
		}

		return word;
	}
	
	public static String join(List<YiGe> seq) {
		
		if (seq == null) {
			return "";
		}
		
		String delim = "\u0F0C";
		StringBuilder sb = new StringBuilder();
		
		String pfx = "";
		
		for (YiGe s : seq) {
			sb.append( pfx + s.toString() );
			pfx = delim;
		}
		
		return sb.toString();
	}
	
	public class Iter implements Iterator<YiGe> {
		int ix = 0;
		boolean removed = false;
		
		public Iter() {
		}
		
		@Override
		public boolean hasNext() {
			return ix < seq.size();
		}
		
		@Override
		public YiGe next() {
			if (hasNext()) {
				YiGe s = seq.get(ix);
				ix ++;
				removed = false;
				return s;
			} else {
				throw new NoSuchElementException();
			}
		}
		
		@Override
		public void remove() {
			if (! removed) {
				ix --;
				seq.remove(ix);
				removed = true;
			}
		}
	}

	
	// the original string
	protected String str;
	// the resulting sequence of syllables
	protected List<YiGe> seq = new ArrayList<YiGe>();
	
	public YiGeSeq(String str) {
		this.str = str;
		toYiGe(str, seq);
	}
	
	public YiGeSeq(List<YiGe> seq) {
		this.seq = seq;
		this.str = toString();
	}
	
	public Iter iterator() {
		return new Iter();
	}

    @Override
    public boolean equals(Object o) {
        
    	if (! (o instanceof YiGeSeq)) {
            return false;
        }
       
        YiGeSeq other = (YiGeSeq) o;
        List<YiGe> oSeq = other.seq;
        
        if (seq.size() != oSeq.size()) {
        	return false;
        }
        
        for (int i = 0; i < seq.size(); i++) {
        	if (! seq.get(i).equals(oSeq.get(i)) ) {
        		return false;
        	}
        }
        
        return true;
    }

    @Override
    public int compareTo(Object o) {
        
    	if (! (o instanceof YiGeSeq)) {
            return -1;
        }
        
        YiGeSeq other = (YiGeSeq) o;
        List<YiGe> oSeq = other.seq;
        
        int sz = seq.size();
        int osz = oSeq.size();
        
        int comp = 0;
        for (int i = 0; i < min(sz, osz); i++) {
        	comp = seq.get(i).compareTo(oSeq.get(i));
        	
        	if (comp != 0) {
        		break;
        	}
        }
        
        if (comp == 0) {
        	if (sz < osz) {
        		comp = -1;
        	} else if (sz > osz) {
        		comp = 1;
        	}
        }
        
        return comp;
    }
	
	@Override
	public String toString() {
		return join(seq);
	}
	
	public int length() {
		return seq.size();
	}
	
	public YiGeSeq subseq(int start, int end) {
		if (start < 0 || end > seq.size() || start > end) {
			throw new IndexOutOfBoundsException("subseq with size: " + seq.size() + " start: " + start + " end: " + end);
		}
		
		return new YiGeSeq(seq.subList(start, end));
	}
}
