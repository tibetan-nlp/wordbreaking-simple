package org.tbrc.tools.wordbreak.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.tbrc.tools.wordbreak.ahocorasick.trie.Token;
import org.tbrc.tools.wordbreak.ahocorasick.trie.Trie;

public class CountWordsInDocs {
	
	protected static String clausePuct = "[\u0F0D-\u0F14]";


	//=========== UTILITIES

	protected boolean isTibLetterOrDigit(int c) {
		return ('\u0F40' <= c && c <= '\u0F83') || ('\u0F90' <= c && c <= '\u0FBC') || ('\u0F20' <= c && c <= '\u0F33') || (c == '\u0F00');
	}

	/** Collects only Tibetan Letter or Digit characters.*/
	protected boolean isTokenChar(int c) {
		return isTibLetterOrDigit(c);
	}
	
	protected static class VolDirsOnly implements FilenameFilter {
		public VolDirsOnly() {

		}

		@Override
		public boolean accept(File d, String nm) {
			return nm.startsWith("W") && d.isDirectory();
		}
	}
	
	protected static class TextsOnly implements FilenameFilter {
		public TextsOnly() {

		}

		@Override
		public boolean accept(File f, String nm) {
			return nm.endsWith(".txt");
		}
	}


	//=========== INIT DICTIONARY
	
	protected Trie initDict(String dictFilePath){
		BufferedReader dictReader = null;

		try {
			File dictFile = new File(dictFilePath);
			FileInputStream dictStream = new FileInputStream(dictFile);
			InputStreamReader dictStreamReader = new InputStreamReader(dictStream, "UTF-8");
			
			dictReader = new BufferedReader(dictStreamReader);
			
			return initDict(dictReader);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (dictReader != null) {
					dictReader.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return null;
	}

	protected Trie initDict(BufferedReader dictReader) {
		Trie dict = new Trie();

		String entryStr = null;
		try {
			while ((entryStr = dictReader.readLine()) != null) {

				dict.addKeyword(entryStr);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return dict;
	}


	//=========== PROCESS TEXT
	
	protected List<String> getClauses(String textFilePath) {
		BufferedReader textReader = null;
		List<String> clauses = new ArrayList<String>();

		try {
			File textFile = new File(textFilePath);
			FileInputStream textStream = new FileInputStream(textFile);
			InputStreamReader textStreamReader = new InputStreamReader(textStream, "UTF-8");
			
			textReader = new BufferedReader(textStreamReader);
			
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ( (line = textReader.readLine()) != null ) {
				line = Normalizer.normalize(line, Normalizer.Form.NFC);
				sb.append(line);
			}
			
			String content = sb.toString();
			
			String[] pieces = content.split(clausePuct);

			clauses = Arrays.asList(pieces);
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (textReader != null) {
					textReader.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return clauses;
	}
	
	protected Collection<Token> parse(String textFilePath, Trie dict) {
		
		List<String> clauses = getClauses(textFilePath);

		Collection<Token> tokens = new ArrayList<Token>();
		
		for (String clause : clauses) {
			Collection<Token> clauseTokens = dict.tokenize(clause);
			tokens.addAll(clauseTokens);
		}
		
		return tokens;
	}
	
	protected void writeTokens(String outFilePath, List<Token> tokens) {
		BufferedWriter textWriter = null;

		try {
			File outFile = new File(outFilePath);
			FileOutputStream outStream = new FileOutputStream(outFile);
			OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");
			
			textWriter = new BufferedWriter(outStreamWriter);
			
			for (Token token : tokens) {
				String tokenStr = token.getClass().getSimpleName() + ": " + token.getFragment() + "\n";
				
				textWriter.write(tokenStr);
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
		CountWordsInDocs instance = new CountWordsInDocs();
		
		long startDict = System.currentTimeMillis();
		Trie dict = instance.initDict(args[0]);
		long endDict = System.currentTimeMillis();
		
		System.err.println("Processed dictionary " + args[0] + " in " + (endDict - startDict) + " ms\n");
		
		Trie.parsing = true;
		
		String srcDirPath = args[1];
		
		FilenameFilter volDirsOnly = new VolDirsOnly();
		FilenameFilter textsOnly = new TextsOnly();
		
		File srcDir = new File(srcDirPath);
		String[] volDirsNms = srcDir.list(volDirsOnly);
		Arrays.sort(volDirsNms);
		
		int volNum = 1;
		
		for (String volDirNm : volDirsNms) {
			File volDir = new File(srcDirPath, volDirNm);
			String[] textsNms = volDir.list(textsOnly);
			
			String volDirPath = volDir.getAbsolutePath();
			
			int wordCount = 0;
			dict.resetYiGeCount();

			try {
				long startParsing = System.currentTimeMillis();

				for (String textNm : textsNms) {
					String textPath = volDirPath + "/" + textNm;

					List<Token> parsed = (List<Token>) instance.parse(textPath, dict);

					wordCount += parsed.size();
				}

				long endParsing = System.currentTimeMillis();

				System.err.println("Volume: " + volNum + " has " + dict.getYiGeCount() + " syllables and " + wordCount + " words. Parsed in " + (endParsing - startParsing) + " ms");
			} catch (Exception ex) {
				System.err.println("Processing volume: " + volNum + "failed");
				ex.printStackTrace();
			}

			volNum++;
		}
	}

}
