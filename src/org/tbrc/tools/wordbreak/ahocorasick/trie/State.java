package org.tbrc.tools.wordbreak.ahocorasick.trie;

import java.util.*;

/**
 * <p>
 *     A state has various important tasks it must attend to:
 * </p>
 *
 * <ul>
 *     <li>success; when a character points to another state, it must return that state</li>
 *     <li>failure; when a character has no matching state, the algorithm must be able to fall back on a
 *         state with less depth</li>
 *     <li>emits; when this state is passed and keywords have been matched, the matches must be
 *         'emitted' so that they can be used later on.</li>
 * </ul>
 *
 * <p>
 *     The root state is special in the sense that it has no failure state; it cannot fail. If it 'fails'
 *     it will still parse the next character and start from the root node. This ensures that the algorithm
 *     always runs. All other states always have a fail state.
 * </p>
 *
 * @author Robert Bor
 * 
 * Changed the use of Character to YiGe so that entire syllables are handled rather pieces like sub-joined-ra
 * and so on.
 * 
 * @author Chris
 */
public class State {

    /** effective the size of the keyword */
    private final int depth;

    /** only used for the root state to refer to itself in case no matches have been found */
    private final State rootState;

    /**
     * referred to in the white paper as the 'goto' structure. From a state it is possible to go
     * to other states, depending on the character passed.
     */
    private Map<YiGe, State> success = new TreeMap<YiGe, State>();

    /** if no matching states are found, the failure state will be returned */
    private State failure = null;

    /** whenever this state is reached, it will emit the matches keywords for future reference */
    private Set<YiGeSeq> emits = null;

    public State() {
        this(0);
    }

    public State(int depth) {
        this.depth = depth;
        this.rootState = depth == 0 ? this : null;
    }

    private State nextState(YiGe yiGe, boolean ignoreRootState) {
        State nextState = this.success.get(yiGe);
        
        if (! ignoreRootState && nextState == null && this.rootState != null) {
            nextState = this.rootState;
        }
        
        return nextState;
    }

    public State nextState(YiGe yiGe) {
        return nextState(yiGe, false);
    }

    public State nextStateIgnoreRootState(YiGe yiGe) {
        return nextState(yiGe, true);
    }

    public State addState(YiGe yiGe) {
        State nextState = nextStateIgnoreRootState(yiGe);
        
        if (nextState == null) {
            nextState = new State(this.depth+1);
            this.success.put(yiGe, nextState);
        }
        
        return nextState;
    }

    public int getDepth() {
        return this.depth;
    }

    public void addEmit(YiGeSeq keyword) {
        if (this.emits == null) {
            this.emits = new TreeSet<>();
        }
        
        this.emits.add(keyword);
    }

    public void addEmit(Collection<YiGeSeq> emits) {
        for (YiGeSeq emit : emits) {
            addEmit(emit);
        }
    }

    public Collection<YiGeSeq> emit() {
        return this.emits == null ? Collections.<YiGeSeq> emptyList() : this.emits;
    }

    public State failure() {
        return this.failure;
    }

    public void setFailure(State failState) {
        this.failure = failState;
    }

    public Collection<State> getStates() {
        return this.success.values();
    }

    public Collection<YiGe> getTransitions() {
        return this.success.keySet();
    }

}
