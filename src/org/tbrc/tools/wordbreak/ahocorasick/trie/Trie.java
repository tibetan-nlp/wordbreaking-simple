package org.tbrc.tools.wordbreak.ahocorasick.trie;

import org.tbrc.tools.wordbreak.ahocorasick.interval.IntervalTree;
import org.tbrc.tools.wordbreak.ahocorasick.interval.Intervalable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 *
 * Based on the Aho-Corasick white paper, Bell technologies: ftp://163.13.200.222/assistant/bearhero/prog/%A8%E4%A5%A6/ac_bm.pdf
 * @author Robert Bor
 *
 * 
 * Modified so that String becomes YiGeSeq and Character becomes YiGe. This way the algorithm is operating over sequences of
 * Tibetan syllables instead of raw Unicode codepoints.
 * 
 * @author Chris
 */
public class Trie {
	
	public static boolean parsing = false;

	private TrieConfig trieConfig;

	private State rootState;
	
	private int yiGeCount = 0;

	private boolean failureStatesConstructed = false;

	public Trie(TrieConfig trieConfig) {
		this.trieConfig = trieConfig;
		this.rootState = new State();
	}

	public Trie() {
		this(new TrieConfig());
	}
	
	public int getYiGeCount() {
		return yiGeCount;
	}
	
	public void resetYiGeCount() {
		yiGeCount = 0;
	}

	public Trie caseInsensitive() {
		this.trieConfig.setCaseInsensitive(true);
		return this;
	}

	public Trie removeOverlaps() {
		this.trieConfig.setAllowOverlaps(false);
		return this;
	}

	public Trie onlyWholeWords() {
		this.trieConfig.setOnlyWholeWords(true);
		return this;
	}

	public void addKeyword(String keyword) {
		if (keyword == null || keyword.isEmpty()) {
			return;
		}

		State currentState = this.rootState;

		YiGeSeq seq = new YiGeSeq(keyword);

		for (YiGe s : seq) {
			currentState = currentState.addState(s);
		}

		currentState.addEmit(seq);
	}

	public Collection<Token> tokenize(String textStr) {

		YiGeSeq text = new YiGeSeq(textStr);
		
		yiGeCount += text.length();

		Collection<Token> tokens = new ArrayList<Token>();

		Collection<Emit> collectedEmits = parseText(text);
		int lastCollectedPosition = -1;

		for (Emit emit : collectedEmits) {

			if (emit.getStart() - lastCollectedPosition > 1) {
				tokens.add(createFragment(emit, text, lastCollectedPosition));
			}

			tokens.add(createMatch(emit, text));

			lastCollectedPosition = emit.getEnd();
		}

		if (text.length() - lastCollectedPosition > 1) {
			tokens.add(createFragment(null, text, lastCollectedPosition));
		}

		return tokens;
	}

	private Token createFragment(Emit emit, YiGeSeq text, int lastCollectedPosition) {

		int start = lastCollectedPosition + 1;
		int end = emit == null ? text.length() : emit.getStart();

		YiGeSeq fragment = text.subseq(start, end);

		return new FragmentToken(fragment);
	}

	private Token createMatch(Emit emit, YiGeSeq text) {

		int start = emit.getStart();
		int end = emit.getEnd() + 1;

		YiGeSeq match = text.subseq(start, end);

		return new MatchToken(match, emit);
	}

	@SuppressWarnings("unchecked")
	public Collection<Emit> parseText(YiGeSeq text) {
		
		checkForConstructedFailureStates();

		int position = 0;
		State currentState = this.rootState;
		List<Emit> collectedEmits = new ArrayList<Emit>();
		
		for (YiGe yiGe : text) {
			if (trieConfig.isCaseInsensitive()) {
				// character = Character.toLowerCase(character);
				// nothing to do here since there is no case
				// in Tibetan
			}
			
			currentState = getState(currentState, yiGe);
			storeEmits(position, currentState, collectedEmits);
			position++;
		}

		if (trieConfig.isOnlyWholeWords()) {
			removePartialMatches(text, collectedEmits);
		}

		if (!trieConfig.isAllowOverlaps()) {
			IntervalTree intervalTree = new IntervalTree((List<Intervalable>)(List<?>)collectedEmits);
			intervalTree.removeOverlaps((List<Intervalable>) (List<?>) collectedEmits);
		}

		return collectedEmits;
	}

	private void removePartialMatches(YiGeSeq searchText, List<Emit> collectedEmits) {

//		long size = searchText.length();

		List<Emit> removeEmits = new ArrayList<Emit>();

		for (Emit emit : collectedEmits) {
			// this is looking for non-alphabetic delimiters at the start and end - this should not be
			// necessary with the reworking for Tibetan - but maybe
//			if ( (emit.getStart() == 0 || ! Character.isAlphabetic( searchText.charAt(emit.getStart() - 1) ) ) 
//			     && (emit.getEnd() + 1 == size || !Character.isAlphabetic(searchText.charAt(emit.getEnd() + 1) ) ) ) {
//				continue;
//			}

			removeEmits.add(emit);
		}

		for (Emit removeEmit : removeEmits) {
			collectedEmits.remove(removeEmit);
		}
	}

	private State getState(State currentState, YiGe yiGe) {

		State newCurrentState = currentState.nextState(yiGe);

		while (newCurrentState == null) {
			currentState = currentState.failure();
			newCurrentState = currentState.nextState(yiGe);
		}

		return newCurrentState;
	}

	private void checkForConstructedFailureStates() {

		if ( ! this.failureStatesConstructed ) {
			constructFailureStates();
		}
	}

	private void constructFailureStates() {

		Queue<State> queue = new LinkedList<State>();
//		Queue<State> queue = new LinkedBlockingDeque<State>();

		// First, set the fail state of all depth 1 states to the root state
		for (State depthOneState : this.rootState.getStates()) {
			depthOneState.setFailure(this.rootState);
			queue.add(depthOneState);
		}

		this.failureStatesConstructed = true;

		// Second, determine the fail state for all depth > 1 state
		while (! queue.isEmpty()) {
			State currentState = queue.remove();

			for (YiGe transition : currentState.getTransitions()) {
				State targetState = currentState.nextState(transition);
				
				if (targetState == null) {
					// should figure out why a null state has been added to currentState
					continue;
				}
				queue.add(targetState);

				State traceFailureState = currentState.failure();
				while (traceFailureState.nextState(transition) == null) {
					traceFailureState = traceFailureState.failure();
				}
				State newFailureState = traceFailureState.nextState(transition);
				
				if (targetState == null || newFailureState == null) {
					System.err.println("targetState: " + targetState + ",  newFailureState: " + newFailureState);
				}
				
				targetState.setFailure(newFailureState);
				targetState.addEmit(newFailureState.emit());
			}
		}
	}

	private void storeEmits(int position, State currentState, List<Emit> collectedEmits) {
		Collection<YiGeSeq> emits = currentState.emit();

		if (emits != null && ! emits.isEmpty()) {
			for (YiGeSeq emit : emits) {
				collectedEmits.add(new Emit(position-emit.length()+1, position, emit));
			}
		}
	}

}
