package org.tbrc.tools.wordbreak.ahocorasick.trie;


/**
 * This class models the Tibetan syllable - yi ge - that occurs between tsegs and shads.
 * Usually this is called a tsheg bar - yi ge'i tsheg gnyis bar gyi sa khongs, ming gzhi,
 * but yiGe is shorter :-;)
 * <p>
 * The idea is to replace the use of <code>Character</code> and <code>String</code> in the 
 * original Bor implementation with <code>YiGe</code> and <code>List&lt;YiGe&gt;</code>.
 * <p>
 * The problem with using Character is that it confuses the Aho-Corasick algorithm into
 * considering all sorts of "bad" breaks since each element of the yi ge is treated as a
 * potentially independently occurring Character. We don't want brgyud and rgyud getting
 * confused when we really want to treat each one as though they are separate glyphs or
 * letters as would be done with English and so on.
 * <p>
 * Later various verb forms and so on can be identified via synonym filtering in Lucene
 * for example where the tokens will be yi ge as they are now and where words can become
 * tokens that are stopped or synonymed and so on.
 * <p>
 * A <code>YiGe</code> has a <code>String</code> which is the Unicode sequence of 
 * codepoints that comprise the yi ge. A word will be a <code>List&lt;YiGe&gt;</code>
 * 
 * @author chris
 *
 */
public class YiGe implements Comparable<Object> {
	static char APOSTROPHE = '\u0F60';
	static char TIB_I = '\u0F72';
	static char TIB_O = '\u0F7C';
	static char TIB_S = '\u0F66';



	private String raw;
	private String yiGe;

	public YiGe(String yiGe) {
		this.raw = yiGe;
		this.yiGe = filterEndings(yiGe);
	}
	
	public String filterEndings(String str) {
		if (str == null) {
			return "";
		}

		int len = str.length();
		
		// if the token ends with "'is" then decrement token length by 3
		if (len > 3) {
			if (str.charAt(len - 3) == APOSTROPHE && str.charAt(len - 2) == TIB_I && str.charAt(len - 1) == TIB_S) {
				str = str.substring(0, len - 3);
				if (Trie.parsing) System.err.println("found 'is on " + str);
			}
		} 
		
		if (len > 2) { // if the token ends with "'i" or "'o" then decrement token length by 2
			if (str.charAt(len - 2) == APOSTROPHE && (str.charAt(len - 1) == TIB_I || str.charAt(len - 1) == TIB_O)) {
				str = str.substring(0, len - 2);
			}
		}
		
		return str;
	}


    @Override
    public boolean equals(Object o) {
        
    	if (! (o instanceof YiGe)) {
            return false;
        }
       
        YiGe other = (YiGe) o;
        
        return yiGe.equals(other.toString());
    }

    @Override
    public int compareTo(Object o) {
        
    	if (! (o instanceof YiGe)) {
            return -1;
        }
        
        YiGe other = (YiGe) o;
        
        return yiGe.compareTo(other.toString());
    }

    @Override
    public String toString() {
        return raw;
    }	
}
