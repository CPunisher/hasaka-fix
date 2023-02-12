package com.cpunisher.hasakafix.tree;

import com.cpunisher.hasakafix.utils.Either;
import com.cpunisher.hasakafix.utils.IdentityPair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.cpunisher.hasakafix.tree.AUToken.tokenNames;

public class AUTreeParser {

    private AUTokenizer tokenizer;

    public AUTreeParser(AUTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public AUTree parse() {
        return parseFunction();
    }

    private Either<AUTree, AUHole> parseLabel() {
        AUToken token = tokenizer.peek();
        switch (token.type()) {
            case AUToken.FUNCTION_SYMBOL -> {
                return Either.first(parseFunction());
            }
            case AUToken.HEDGE_VARIABLE, AUToken.TERM_VARIABLE, AUToken.CONSTANT_VARIABLE, AUToken.HOLE -> {
                return parseVariable();
            }
        }
        throw new IllegalStateException("Unexpected token " + tokenNames[token.type()]);
    }

    private AUTree parseFunction() {
        List<Either<AUTree, AUHole>> children = new ArrayList<>();
        AUToken label = expect(AUToken.FUNCTION_SYMBOL);
        expect(AUToken.LPAREN);
        // body
        if (tokenizer.peek().type() != AUToken.RPAREN) {
            do {
                children.add(parseLabel());
            } while (eatIf(AUToken.COMMAS));
        }
        AUToken rParen = expect(AUToken.RPAREN);
        return new AUTree(new IdentityPair<>(label.pos().first, rParen.pos().second), label.value(), "", children);
    }

    private Either<AUTree, AUHole> parseVariable() {
        AUToken token = tokenizer.next();
        switch (token.type()) {
            case AUToken.HEDGE_VARIABLE, AUToken.TERM_VARIABLE, AUToken.CONSTANT_VARIABLE -> {
                return Either.first(new AUTree(token.pos(), "", token.value(), Collections.emptyList()));
            }
            case AUToken.HOLE -> {
                return Either.second(new AUHole(token.pos(), token.value()));
            }
        }
        throw new IllegalStateException("Unexpected token " + tokenNames[token.type()]);
    }

    private AUToken expect(int type) {
        AUToken token = tokenizer.next();
        if (token.type() != type) {
            throw new IllegalStateException("Unexpected token " + tokenNames[token.type()] + ". Expect: " + tokenNames[type]);
        }
        return token;
    }

    private boolean eatIf(int type) {
        AUToken token = tokenizer.peek();
        if (token.type() == type) {
            tokenizer.next();
            return true;
        }
        return false;
    }
}
