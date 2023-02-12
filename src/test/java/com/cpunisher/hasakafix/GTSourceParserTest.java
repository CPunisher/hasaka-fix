package com.cpunisher.hasakafix;

import com.cpunisher.hasakafix.edit.parser.GTSourceParser;
import com.cpunisher.hasakafix.edit.parser.ISourceParser;
import com.cpunisher.hasakafix.repo.Simple1;
import com.github.gumtreediff.tree.Tree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GTSourceParserTest {
    @Test
    public void testParse() {
        ISourceParser<Tree> parser = new GTSourceParser(".java");
        Tree tree = parser.parse(Simple1.NEW_WORKER_DOT_JAVA);
        assertNotNull(tree);
        assertEquals(Simple1.TREE_NEW_WORKER_DOT_JAVA, tree.toTreeString());
    }
}
