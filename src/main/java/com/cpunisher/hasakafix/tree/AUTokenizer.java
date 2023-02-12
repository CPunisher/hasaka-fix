package com.cpunisher.hasakafix.tree;

import com.cpunisher.hasakafix.utils.IdentityPair;

import java.util.Iterator;

public class AUTokenizer implements Iterator<AUToken> {

    private final char[] chars;
    private int pos = 0;

    public AUTokenizer(String source) {
        this.chars = source.toCharArray();
    }

    private void skipSpaces() {
        while (pos < chars.length && Character.isWhitespace(chars[pos])) pos++;
    }

    private boolean validName(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '#' || ch == '_' || ch == '.';
    }

    private String nextWord() {
        StringBuilder stringBuilder = new StringBuilder();
        while (validName(chars[pos])) {
            stringBuilder.append(chars[pos]);
            pos++;
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean hasNext() {
        skipSpaces();
        return pos < chars.length;
    }

    @Override
    public AUToken next() {
        return _next();
    }

    public AUToken peek() {
        int markPos = pos;
        AUToken next = _next();
        pos = markPos;
        return next;
    }

    private AUToken _next() {
        skipSpaces();
        char next = chars[pos];
        switch (next) {
            case '(' -> {
                AUToken token = new AUToken(AUToken.LPAREN, "(", new IdentityPair<>(pos, pos));
                pos++;
                return token;
            }
            case ')' -> {
                AUToken token = new AUToken(AUToken.RPAREN, ")", new IdentityPair<>(pos, pos));
                pos++;
                return token;
            }
            case ',' -> {
                AUToken token = new AUToken(AUToken.COMMAS, ",", new IdentityPair<>(pos, pos));
                pos++;
                return token;
            }
            default -> {
                int startPos = pos;

                String word = nextWord();
                char begin = word.charAt(0);
                int type = AUToken.CONSTANT_VARIABLE;
                if (chars[pos] == '(') type = AUToken.FUNCTION_SYMBOL;
                else if (Character.isUpperCase(begin)) type = AUToken.HEDGE_VARIABLE;
                else if (begin >= 'u' && begin <= 'z') type = AUToken.TERM_VARIABLE;
                else if (begin == '#') type = AUToken.HOLE;
                return new AUToken(type, word, new IdentityPair<>(startPos, pos - 1));
            }
        }
    }
}
