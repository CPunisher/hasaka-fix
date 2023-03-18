package com.cpunisher.hasakafix;

import com.cpunisher.hasakafix.antiunification.GTPlainAntiUnifier;
import com.cpunisher.hasakafix.antiunification.PlainAntiUnifier2;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.*;
import org.junit.jupiter.api.Test;

public class GTPlainAntiUnifierTest {

    @Test
    public void testAntiUnify() {
        // Test case
        Tree stripedBefore1 = new DefaultTree(TypeSet.type("Block"));
        stripedBefore1.addChild(new DefaultTree(TypeSet.type("Call"), "g"));

        Tree stripedAfter1 = new DefaultTree(TypeSet.type("Block"));
        Tree stripedAfter1If = new DefaultTree(TypeSet.type("If"));
        stripedAfter1If.addChild(new DefaultTree(TypeSet.type("Name"), "c"));
        stripedAfter1If.addChild(new DefaultTree(TypeSet.type("Call"), "g"));
        stripedAfter1.addChild(stripedAfter1If);

        Tree stripedBefore2 = new DefaultTree(TypeSet.type("Block"));
        stripedBefore2.addChild(new DefaultTree(TypeSet.type("Return")));

        Tree stripedAfter2 = new DefaultTree(TypeSet.type("Block"));
        Tree stripedAfter2If = new DefaultTree(TypeSet.type("If"));
        stripedAfter2If.addChild(new DefaultTree(TypeSet.type("Name"), "c"));
        stripedAfter2If.addChild(new DefaultTree(TypeSet.type("Call"), "onResult"));
        stripedAfter2.addChild(stripedAfter2If);

        // Test
        PlainAntiUnifier2 antiUnifier = new PlainAntiUnifier2();
        Tree before = antiUnifier.antiUnify(stripedBefore1, stripedBefore2).get(0).template();
        Tree after = antiUnifier.antiUnify(stripedAfter1, stripedAfter2).get(0).template();
        System.out.println(before.toTreeString());
        System.out.println(after.toTreeString());
        System.out.println(Matchers.getInstance().getMatcher().match(before, after));
    }

    @Test
    public void testAntiUnifyEdit() {
        Tree before1 = new DefaultTree(TypeSet.type("Block"));
        before1.addChild(new DefaultTree(TypeSet.type("Call"), "f"));
        before1.addChild(new DefaultTree(TypeSet.type("Call"), "g"));
        Tree before1Assign1 = new DefaultTree(TypeSet.type("Assign"));
        before1Assign1.addChild(new DefaultTree(TypeSet.type("Name"), "x"));
        before1Assign1.addChild(new DefaultTree(TypeSet.type("Num"), "1"));
        before1.addChild(before1Assign1);

        Tree after1 = new DefaultTree(TypeSet.type("Block"));
        after1.addChild(new DefaultTree(TypeSet.type("Call"), "f"));
        Tree after1Assign1 = new DefaultTree(TypeSet.type("Assign"));
        after1Assign1.addChild(new DefaultTree(TypeSet.type("Name"), "x"));
        after1Assign1.addChild(new DefaultTree(TypeSet.type("Num"), "1"));
        after1.addChild(after1Assign1);
        Tree after1If1 = new DefaultTree(TypeSet.type("If"));
        after1If1.addChild(new DefaultTree(TypeSet.type("Name"), "c"));
        after1If1.addChild(new DefaultTree(TypeSet.type("Call"), "g"));
        after1.addChild(after1If1);

        Tree before2 = new DefaultTree(TypeSet.type("Block"));
        before2.addChild(new DefaultTree(TypeSet.type("Return")));
        Tree before2Assign1 = new DefaultTree(TypeSet.type("Assign"));
        before2Assign1.addChild(new DefaultTree(TypeSet.type("Name"), "y"));
        before2Assign1.addChild(new DefaultTree(TypeSet.type("Num"), "2"));
        before2.addChild(before2Assign1);

        Tree after2 = new DefaultTree(TypeSet.type("Block"));
        Tree after2Assign1 = new DefaultTree(TypeSet.type("Assign"));
        after2Assign1.addChild(new DefaultTree(TypeSet.type("Name"), "y"));
        after2Assign1.addChild(new DefaultTree(TypeSet.type("Num"), "2"));
        after2.addChild(after2Assign1);
        Tree after2If1 = new DefaultTree(TypeSet.type("If"));
        after2If1.addChild(new DefaultTree(TypeSet.type("Name"), "c"));
        after2If1.addChild(new DefaultTree(TypeSet.type("Call"), "onResult"));
        after2.addChild(after2If1);

        Matcher matcher = Matchers.getInstance().getMatcher();
        GTPlainAntiUnifier antiUnifier = new GTPlainAntiUnifier(new PlainAntiUnifier2());
        MappingStore mappings1 = matcher.match(before1, after1);
        MappingStore mappings2 = matcher.match(before2, after2);
        GTPlainAntiUnifier.GTAntiUnifierData result = antiUnifier.antiUnify(
                new GTTreeEdit((before1), (after1), mappings1),
                new GTTreeEdit((before2), (after2), mappings2)
        );
        System.out.println(result.template().before().toTreeString());
        System.out.println(result.template().after().toTreeString());
        System.out.println(result.template().mappings());
    }
}
