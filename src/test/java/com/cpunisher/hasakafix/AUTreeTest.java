package com.cpunisher.hasakafix;

import com.cpunisher.hasakafix.repo.Simple1;
import com.cpunisher.hasakafix.tree.AUToken;
import com.cpunisher.hasakafix.tree.AUTokenizer;
import com.cpunisher.hasakafix.tree.AUTree;
import com.cpunisher.hasakafix.tree.AUTreeParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AUTreeTest {

    @Test
    public void testTokenizer() {
        AUTokenizer tokenizer = new AUTokenizer(Simple1.AU_RESULT_1);
        List<AUToken> tokenList = new ArrayList<>();
        tokenizer.forEachRemaining(tokenList::add);
        System.out.println(tokenList);
    }

    @Test
    public void testParser() {
        AUTokenizer tokenizer = new AUTokenizer(Simple1.AU_RESULT_1);
        AUTreeParser parser = new AUTreeParser(tokenizer);
        AUTree tree = parser.parse();
        System.out.println(tree);
        assertEquals(3, tree.size());
    }
}
