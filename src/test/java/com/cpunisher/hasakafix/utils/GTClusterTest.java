package com.cpunisher.hasakafix.utils;

import com.cpunisher.hasakafix.cluster.DCapCalculator;
import com.cpunisher.hasakafix.repo.Simple1;
import com.cpunisher.hasakafix.tree.AUTokenizer;
import com.cpunisher.hasakafix.tree.AUTree;
import com.cpunisher.hasakafix.tree.AUTreeParser;
import org.junit.jupiter.api.Test;

public class GTClusterTest {

    @Test
    public void testDCap() {
        AUTokenizer tokenizer = new AUTokenizer(Simple1.AU_RESULT_1);
        AUTreeParser parser = new AUTreeParser(tokenizer);

        AUTree tree = parser.parse();
        System.out.println(tree);
        DCapCalculator.dcap(tree, 3);
        System.out.println(tree);
    }
}
