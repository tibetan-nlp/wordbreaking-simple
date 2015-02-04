package org.tbrc.tools.wordbreak.ahocorasick.trie;

public class MatchToken extends Token {

    private Emit emit;

    public MatchToken(YiGeSeq fragment, Emit emit) {
        super(fragment);
        this.emit = emit;
    }

    @Override
    public boolean isMatch() {
        return true;
    }

    @Override
    public Emit getEmit() {
        return this.emit;
    }

}
