package org.tbrc.tools.wordbreak.app;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tbrc.common.server.SortTibetanUnicodeStrings;
import org.tbrc.common.shared.Converter;


public class HeadwordsMerge {
	
	protected  static Converter converter = new Converter(false, false, false, true);

	protected static SortTibetanUnicodeStrings SORTER = new SortTibetanUnicodeStrings();

	protected static boolean isTibLetter(int c) {
		return ('\u0F40' <= c && c <= '\u0F83') || ('\u0F90' <= c && c <= '\u0FBC') || (c == '\u0F00');
	}
	
	protected static String trim(String h) {
		if (h != null) {
			int ix = h.length() - 1;
			
			for (; ix >= 0; ix--) {
				if (isTibLetter(h.charAt(ix))) {
					break;
				}
			}
			
			if (ix == h.length() - 1) {
				return h;
			} else {
				return h.substring(0, ix + 1);
			}
		} else {
			return "";
		}
	}
	
	protected static List<String> sort(Set<String> set) {
		ArrayList<String> tmp = new ArrayList<String>(set);
		return SORTER.sort(tmp);
	}

	protected static Set<String> processUnicodeHeadwords(String filePath) {
		BufferedReader headwordsReader = null;
		Set<String> headwords = new HashSet<String>();	

		try {
			headwordsReader = new BufferedReader(new FileReader(filePath));
			String headword = headwordsReader.readLine();

			while (headword != null) {
				headword = Normalizer.normalize(headword, Normalizer.Form.NFC);
				headword = trim(headword);
				headwords.add(headword);
				headword = headwordsReader.readLine();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (headwordsReader != null) {
					headwordsReader.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return headwords;
	}

	protected static Set<String> processWylieHeadwords(String filePath) {
		BufferedReader headwordsReader = null;
		Set<String> headwords = new HashSet<String>();	

		try {
			headwordsReader = new BufferedReader(new FileReader(filePath));
			String headword = headwordsReader.readLine();

			while (headword != null) {
				String tmp = converter.toUnicode(headword);
				tmp = Normalizer.normalize(tmp, Normalizer.Form.NFC);
				tmp = trim(tmp);
				headwords.add(tmp);
				headword = headwordsReader.readLine();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (headwordsReader != null) {
					headwordsReader.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return headwords;
	}
	
	protected static void writeHeadwords(String outPath, List<String> headwords) {
		BufferedWriter textWriter = null;

		try {
			textWriter = new BufferedWriter(new FileWriter(outPath));
//			File outFile = new File(outFilePath);
//			FileOutputStream outStream = new FileOutputStream(outFile);
//			OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");
//			
//			textWriter = new BufferedWriter(outStreamWriter);
			
			for (String headword : headwords) {
				textWriter.write(headword);
				textWriter.write("\n");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (textWriter != null) {
					textWriter.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 1) {
			String outPath = args[0];
			
			Set<String> headwords = new HashSet<String>();
			
			for (int ix = 1; ix < args.length; ix++) {
				Set<String> tmp;
				String p1 = args[ix];
				
				if ("w".equals(p1)) {
					if (++ix < args.length) {
						p1 = args[ix];
						tmp = processWylieHeadwords(p1);
					} else {
						System.err.println("Argument 'w' not followed by a Wylie headwords file path");
						return;
					}
				} else {
					tmp = processUnicodeHeadwords(p1);
				}
				
				headwords.addAll(tmp);
			}
			
			List<String> sortedHeadwords = sort(headwords);
			
			writeHeadwords(outPath, sortedHeadwords);
			
		} else {
			System.err.println("Too few arguments");
		}
	}

}
