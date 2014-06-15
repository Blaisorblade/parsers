package org.morphling.parsers.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public class Sequence extends GrammarNode {
    private final @Children GrammarNode[] sequence;

    public Sequence(ParserState p, GrammarNode... sequence) {
        super(p);
        this.sequence = sequence;
        adoptChildren();
    }

    @Override @ExplodeLoop
    public boolean executeParse(VirtualFrame frame) {
        for (GrammarNode grammarNode : sequence) {
            if (grammarNode.executeParse(frame))
                continue;
            else
                return false;
        }
        return true;
    }
}
