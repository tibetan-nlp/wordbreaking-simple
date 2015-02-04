package org.tbrc.tools.wordbreak.ahocorasick.trie;

public class FragmentToken extends Token {

    public FragmentToken(YiGeSeq fragment) {
        super(fragment);
    }

    @Override
    public boolean isMatch() {
        return false;
    }

    @Override
    public Emit getEmit() {
        return null;
    }
}
