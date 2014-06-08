package org.morphling.parsers.truffle;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;

public class UninitializedNonterminalCall extends GrammarNode {
    private enum CallNodeType {
        UNOPTIMIZED, CACHEDEXECUTE, CACHEDCALL
    }

    private static final CallNodeType callNodeType = CallNodeType.CACHEDEXECUTE;

    final NonterminalName nonterminalName;

    public UninitializedNonterminalCall(ParserState p, NonterminalName nonterminalName) {
        super(p);
        this.nonterminalName = nonterminalName;
    }

    @Override
    public boolean executeParse(VirtualFrame frame) {
        CompilerAsserts.neverPartOfCompilation();

        Alternatives alternatives;
        GrammarNode replacementNode;


        alternatives = state.lookupInEnv(nonterminalName);

        switch (callNodeType) {
            case UNOPTIMIZED:
                replacementNode = new UnoptimizedNonterminalCall(state, nonterminalName);
                break;
            case CACHEDEXECUTE:
                replacementNode = new CachedNonterminalCallExecute(state, alternatives);
                break;
            case CACHEDCALL:
                replacementNode = new CachedNonterminalCallTruffle(state, alternatives);
                break;
            default:
                System.err.println("Not implemented org.morphling.parsers.truffle.UninitializedNonterminalCall.executeParse");
                replacementNode = null;
        }

        replace(replacementNode);


        return replacementNode.executeParse(frame);
    }
}
