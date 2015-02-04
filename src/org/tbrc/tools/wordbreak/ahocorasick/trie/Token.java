package org.tbrc.tools.wordbreak.ahocorasick.trie;

public abstract class Token {

    private YiGeSeq fragment;

    public Token(YiGeSeq fragment) {
        this.fragment = fragment;
    }

    public YiGeSeq getFragment() {
        return this.fragment;
    }

    public abstract boolean isMatch();

    public abstract Emit getEmit();

}
