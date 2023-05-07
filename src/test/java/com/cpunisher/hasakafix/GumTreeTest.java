package com.cpunisher.hasakafix;

import com.cpunisher.hasakafix.edit.parser.GTSourceParser;
import com.cpunisher.hasakafix.edit.parser.ISourceParser;
import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class GumTreeTest {
    final String CODE_1 = """
            public class Main {
                void f() {
                    v.equals("Hasaka Fix");
                }
            }
            """;

    final String CODE_2 = """
            public class Main {
                void f() {
                    "Hasaka Fix".equals(v);
                }
            }
            """;
    @Test
    public void testGumTree() {
        ISourceParser<Tree> parser = new GTSourceParser(".java");
        Tree oldTree = Objects.requireNonNull(parser.parse(CODE_1));
        Tree newTree = Objects.requireNonNull(parser.parse(CODE_2));
        MappingStore mappings = Matchers.getInstance().getMatcher().match(oldTree, newTree);
        EditScriptGenerator editScriptGenerator = new SimplifiedChawatheScriptGenerator();
        EditScript editScript = editScriptGenerator.computeActions(mappings);
        editScript.forEach(System.out::println);
    }
}
