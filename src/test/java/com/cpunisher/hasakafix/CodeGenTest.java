package com.cpunisher.hasakafix;


import com.cpunisher.hasakafix.apply.CodeGen;
import com.cpunisher.hasakafix.edit.parser.GTSourceParser;
import com.cpunisher.hasakafix.edit.parser.ISourceParser;
import com.github.gumtreediff.tree.Tree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.io.StringWriter;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CodeGenTest {

    ISourceParser<Tree> parser = new GTSourceParser(".java");
    CodeGen codeGen = new CodeGen();

    @Test
    public void testTypeDeclaration() throws IOException {
        String source = """
                public class Main<A extends Main, B> extends Parent implements Inter1 {
                    public static void Main(int... a) {
                        f(Main::<A, B>c);
                    }
                }
                """;
        Tree tree = parser.parse(source);
        StringWriter stringWriter = new StringWriter();
        codeGen.write(tree, stringWriter);
        stringWriter.flush();
        assertEqualWithoutFormat(source, stringWriter.toString());
    }

    @Test
    public void testEnum() throws IOException {
        String source = """
                public enum Test implements Interface1 {
                    private A(1, 2),
                    private B(3, 4) {
                        void f() { }
                    },
                    void f2() {
                    }
                }
                """;
        Tree tree = parser.parse(source);
        StringWriter stringWriter = new StringWriter();
        codeGen.write(tree, stringWriter);
        stringWriter.flush();
        System.out.println(stringWriter);
        System.out.println(tree.toTreeString());
        assertEqualWithoutFormat(source, stringWriter.toString());
    }

    @Test
    public void testRecord() throws IOException {
        String source = """
                public record Test<T, U>(T a, U b) implements A, B {
                    void f() {
                        int a = 1;
                    }
                }
                """;
        Tree tree = parser.parse(source);
        StringWriter stringWriter = new StringWriter();
        codeGen.write(tree, stringWriter);
        stringWriter.flush();
        assertEqualWithoutFormat(source, stringWriter.toString());
    }

    @Test
    public void testSwitch() throws IOException {
        String source = """
                public class Main {
                    void f() {
                        switch (a) {
                            case 1, 2: return;
                            case 1, 2 -> { return; }
                            default: return;
                        }
                    }
                }
                """;
        Tree tree = parser.parse(source);
        StringWriter stringWriter = new StringWriter();
        codeGen.write(tree, stringWriter);
        stringWriter.flush();
        System.out.println(stringWriter);
        System.out.println(tree.toTreeString());
        assertEqualWithoutFormat(source, stringWriter.toString());
    }

    private static void assertEqualWithoutFormat(String expected, String actual) {
        String[] expectedWords = expected.split("\\s+");
        String[] actualWords = actual.split("\\s+");
        Assertions.assertEquals(String.join(" ", expectedWords), String.join(" ", actualWords));
    }
}
