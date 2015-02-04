package org.tbrc.tools.wordbreak.ahocorasick.trie;


import org.tbrc.tools.wordbreak.ahocorasick.interval.Interval;
import org.tbrc.tools.wordbreak.ahocorasick.interval.Intervalable;

public class Emit extends Interval implements Intervalable {

    private final YiGeSeq keyword;

    public Emit(final int start, final int end, final YiGeSeq keyword) {
        super(start, end);
        this.keyword = keyword;
    }

    public YiGeSeq getKeyword() {
        return this.keyword;
    }

    @Override
    public String toString() {
        return super.toString() + "=" + this.keyword;
    }

}
