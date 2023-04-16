package com.cpunisher.hasakafix.apply;

import com.github.gumtreediff.tree.Tree;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

public class CodeGen {
    private static final Set<String> TYPE_SETS = Set.of("SimpleType", "PrimitiveType");
    private static final String INDENT = "    ";

    private int level = 0;

    private void writeIndent(Writer writer) throws IOException {
        for (int i = 0; i < level; i++) {
            writer.write(INDENT);
        }
    }

    private void writeWithLabel(Tree tree, Writer writer) throws IOException {
        writer.write(tree.getLabel());
    }

    private void writeChildren(Tree tree, Writer writer) throws IOException {
        writeChildren(tree, writer, "", 0, tree.getChildren().size());
    }

    private void writeChildren(Tree tree, Writer writer, String separator) throws IOException {
        writeChildren(tree, writer, separator, 0, tree.getChildren().size());
    }

    private void writeChildren(Tree tree, Writer writer, String separator, int start) throws IOException {
        writeChildren(tree, writer, separator, start, tree.getChildren().size());
    }

    private int writeChildren(Tree tree, Writer writer, String separator, int start, String type) throws IOException {
        int end = start;
        write(tree.getChild(end++), writer);
        while (end < tree.getChildren().size() && tree.getChild(end).getType().name.equals(type)) {
            writer.write(separator);
            write(tree.getChild(end++), writer);
        }
        return end;
    }

    private int writeChildren(Tree tree, Writer writer, String separator, int start, String type, boolean include) throws IOException {
        int end = start;
        while (end < tree.getChildren().size() && !tree.getChild(end).getType().name.equals(type)) end++;
        if (include) {
            end++;
        }
        writeChildren(tree, writer, separator, start, end);
        return end;
    }

    private void writeChildren(Tree tree, Writer writer, String separator, int start, int end) throws IOException {
        for (int i = start; i < end; i++) {
            write(tree.getChild(i), writer);
            if (i != end - 1) {
                writer.write(separator);
            }
        }
    }

    public void write(Tree tree, Writer writer) throws IOException {
        String type = tree.getType().toString();

        // leaf
        Set<String> leafType = new HashSet<>();
        leafType.add("ModuleQualifiedName");
        leafType.add("QualifiedName");
        leafType.add("SimpleName");
        leafType.add("PrimitiveType");
        leafType.add("Modifier");
        leafType.add("StringLiteral");
        leafType.add("NumberLiteral");
        leafType.add("CharacterLiteral");
        leafType.add("BooleanLiteral");
        leafType.add("TextElement");
        leafType.add("INFIX_EXPRESSION_OPERATOR");
        leafType.add("PREFIX_EXPRESSION_OPERATOR");
        leafType.add("POSTFIX_EXPRESSION_OPERATOR");
        leafType.add("TYPE_DECLARATION_KIND");
        leafType.add("ASSIGNMENT_OPERATOR");
        if (leafType.contains(type)) {
            writeWithLabel(tree, writer);
        }

        int last = 0;
        switch (type) {
            case "AnnotationTypeDeclaration":
                break;
            case "AnnotationTypeMemberDeclaration":
                break;
            case "AnonymousClassDeclaration":
                writer.write(" {\n");
                level++;
                writeChildren(tree, writer, "\n");
                level--;
                writer.write("\n");
                writeIndent(writer);
                writer.write("}");
                break;
            case "ArrayAccess":
                write(tree.getChild(0), writer);
                writer.write("[");
                write(tree.getChild(1), writer);
                writer.write("]");
                break;
            case "ArrayCreation":
                writer.write("new ");
                writeChildren(tree, writer, " ");
                break;
            case "ArrayInitializer":
                writer.write("{ ");
                writeChildren(tree, writer, ", ");
                writer.write(" }");
                break;
            case "ArrayType":
                writeChildren(tree, writer);
                break;
            case "AssertStatement":
                writeIndent(writer);
                writer.write("assert ");
                write(tree.getChild(0), writer);
                if (tree.getChildren().size() > 1) {
                    writer.write(" : ");
                    write(tree.getChild(1), writer);
                }
                writer.write(";");
                break;
            case "Assignment":
                writeChildren(tree, writer, " ");
                break;
            case "Block":
                writer.write(" {\n");
                level++;
                writeChildren(tree, writer, "\n");
                level--;
                writer.write("\n");
                writeIndent(writer);
                writer.write("}");
                break;
//            case "BlockComment":
//                break;
            case "BreakStatement", "ContinueStatement":
                writeIndent(writer);
                writer.write("break");
                if (tree.getChildren().size() > 0) {
                    writer.write(" ");
                    write(tree.getChild(0), writer);
                }
                writer.write(";");
                break;
//            case "CaseDefaultExpression":
//                break;
            case "CastExpression":
                writer.write("(");
                write(tree.getChild(0), writer);
                writer.write(") ");
                write(tree.getChild(1), writer);
                break;
            case "CatchClause":
                writer.write(" catch (");
                write(tree.getChild(0), writer);
                writer.write(")");
                write(tree.getChild(1), writer);
                break;
            case "ClassInstanceCreation":
                writer.write("new ");
                write(tree.getChild(0), writer);
                writer.write("(");
                writeChildren(tree, writer, ", ", 1);
                writer.write(")");
                break;
            case "CompilationUnit":
                writeChildren(tree, writer, "\n");
                break;
            case "ConditionalExpression":
                write(tree.getChild(0), writer);
                writer.write(" ? ");
                write(tree.getChild(1), writer);
                writer.write(" : ");
                write(tree.getChild(2), writer);
                break;
            case "ConstructorInvocation":
                writeIndent(writer);
                writer.write("this(");
                writeChildren(tree, writer, ", ");
                writer.write(");");
                break;
            case "CreationReference":
                write(tree.getChild(0), writer);
                writer.write("::");
                if (tree.getChildren().size() > 1) {
                    writer.write("<");
                    writeChildren(tree, writer, ", ", 1);
                    writer.write(">");
                }
                writer.write("new");
                break;
            case "Dimension":
                writer.write("[");
                writeChildren(tree, writer);
                writer.write("]");
                break;
            case "DoStatement":
                writeIndent(writer);
                writer.write("do");
                write(tree.getChild(0), writer);
                writer.write(" while (");
                write(tree.getChild(1), writer);
                writer.write(");");
                break;
            case "EmptyStatement":
                writeIndent(writer);
                writer.write(";");
                break;
            case "EnhancedForStatement":
                writeIndent(writer);
                writer.write("for (");
                write(tree.getChild(0), writer);
                writer.write(" : ");
                write(tree.getChild(1), writer);
                writer.write(")");
                write(tree.getChild(2), writer);
                break;
            case "EnumConstantDeclaration":
                writeIndent(writer);
                last = writeChildren(tree, writer, " ", 0, "SimpleName", false);
                writer.write(" ");
                write(tree.getChild(last++), writer);
                if (last < tree.getChildren().size() && !tree.getChild(last).getType().name.equals("AnonymousClassDeclaration")) {
                    writer.write("(");
                    last = writeChildren(tree, writer, ", ", last, "AnonymousClassDeclaration", false);
                    writer.write(")");
                }
                writeChildren(tree, writer, "", last);
                break;
            case "EnumDeclaration":
                writeIndent(writer);
                last = writeChildren(tree, writer, " ", 0, "SimpleName", false);
                writer.write(" enum ");
                write(tree.getChild(last++), writer);
                if (tree.getChild(last).getType().name.equals("SimpleType")) {
                    writer.write(" implements ");
                    last = writeChildren(tree, writer, ", ", last, "EnumConstantDeclaration", false);
                }
                writer.write(" {\n");
                level++;
                while (tree.getChild(last).getType().name.equals("EnumConstantDeclaration")) {
                    write(tree.getChild(last++), writer);
                    writer.write(",\n");
                }
                writeChildren(tree, writer, "\n", last);
                writer.write("\n");
                level--;
                writeIndent(writer);
                writer.write("}");
                break;
            case "ExportsDirective":
                writer.write("exports ");
                write(tree.getChild(0), writer);
                if (tree.getChildren().size() > 1) {
                    writer.write(" to ");
                    writeChildren(tree, writer, ", ", 1);
                }
                break;
            case "ExpressionMethodReference":
                write(tree.getChild(0), writer);
                writer.write("::");
                if (!tree.getChild(1).getType().name.equals("SimpleName")) {
                    writer.write("<");
                    last = writeChildren(tree, writer, ", ", 1, "SimpleName", false);
                    writer.write(">");
                }
                write(tree.getChild(last), writer);
                break;
            case "ExpressionStatement":
                writeIndent(writer);
                write(tree.getChild(0), writer);
                writer.write(";");
                break;
            case "FieldAccess":
                write(tree.getChild(0), writer);
                writer.write(".");
                write(tree.getChild(1), writer);
                break;
            case "FieldDeclaration":
                writeIndent(writer);
                while (!TYPE_SETS.contains(tree.getChild(last).getType().name)) {
                    write(tree.getChild(last++), writer);
                }
                write(tree.getChild(last++), writer);
                writeChildren(tree, writer, ", ", last);
                writer.write(";");
                break;
            case "ForStatement":
                // TODO, split expression
                writeIndent(writer);
                writer.write("for (");
                write(tree.getChild(0), writer);
                writer.write("; ");
                write(tree.getChild(1), writer);
                writer.write("; ");
                write(tree.getChild(2), writer);
                writer.write(")");
                write(tree.getChild(3), writer);
                break;
            case "GuardedPattern":
                write(tree.getChild(0), writer);
                writer.write(" when ");
                write(tree.getChild(1), writer);
                break;
            case "IfStatement":
                writeIndent(writer);
                writer.write("if (");
                write(tree.getChild(0), writer);
                writer.write(")");
                write(tree.getChild(1), writer);
                if (tree.getChildren().size() > 2) {
                    writer.write(" else");
                    write(tree.getChild(2), writer);
                }
                break;
            case "ImportDeclaration":
                writer.write("import ");
                write(tree.getChild(0), writer);
                writer.write(";");
                break;
            case "InfixExpression":
                writeChildren(tree, writer, " ");
                break;
            case "Initializer":
                writeIndent(writer);
                writeChildren(tree, writer, " ");
                break;
            case "InstanceofExpression", "PatternInstanceofExpression":
                write(tree.getChild(0), writer);
                writer.write(" instanceof ");
                write(tree.getChild(1), writer);
                break;
            case "IntersectionType":
                writeChildren(tree, writer, " & ");
                break;
//            case "Javadoc":
//                break;
//            case "JavaDocRegion":
//                break;
//            case "JavaDocTextElement":
//                break;
            case "LabeledStatement":
                write(tree.getChild(0), writer);
                writer.write(" : ");
                write(tree.getChild(1), writer);
                break;
            case "LambdaExpression":
                writer.write("(");
                while (last < tree.getChildren().size()
                        && Set.of("VariableDeclarationFragment", "SingleVariableDeclaration").contains(tree.getChild(last).getType().name)) {
                    write(tree.getChild(last++), writer);
                    if (last < tree.getChildren().size()
                            && Set.of("VariableDeclarationFragment", "SingleVariableDeclaration").contains(tree.getChild(last).getType().name)) {
                        writer.write(", ");
                    }
                }
                writer.write(")");
                writer.write(" -> ");
                write(tree.getChild(last), writer);
                break;
//            case "LineComment":
//                break;
            case "MarkerAnnotation":
                writer.write("@");
                write(tree.getChild(0), writer);
                break;
//            case "MemberRef":
//                break;
            case "MemberValuePair":
                write(tree.getChild(0), writer);
                writer.write(" = ");
                write(tree.getChild(1), writer);
                break;
            case "MethodDeclaration":
                writeIndent(writer);
                // before param
                last = writeChildren(tree, writer, " ", 0, "SimpleName", true);
                // param
                writer.write("(");
                last = writeChildren(tree, writer, ", ", last, "Block", false);
                writer.write(")");
                // body
                writeChildren(tree, writer, "\n", last);
                break;
            case "MethodInvocation":
                if (tree.getChild(last).getType().name.equals("METHOD_INVOCATION_RECEIVER")) {
                    write(tree.getChild(last++), writer);
                    writer.write(".");
                }
                if (TYPE_SETS.contains(tree.getChild(last).getType().name)) {
                    writer.write("<");
                    last = writeChildren(tree, writer, " ,", last, "Type");
                    writer.write(">");
                }
                write(tree.getChild(last++), writer);
                writer.write("(");
                if (last < tree.getChildren().size()) {
                    write(tree.getChild(last), writer);
                }
                writer.write(")");
                break;
//            case "MethodRef":
//                break;
//            case "MethodRefParameter":
//                break;
            case "ModuleDeclaration":
                writeIndent(writer);
                last = writeChildren(tree, writer, " ", 0, "SimpleName", true);
                writer.write(" {\n");
                level++;
                writeChildren(tree, writer, "\n", last);
                level--;
                writer.write("\n");
                writeIndent(writer);
                writer.write("}\n");
                break;
            case "ModuleModifier":
                writeChildren(tree, writer);
                break;
            case "NameQualifiedType":
                write(tree.getChild(0), writer);
                writer.write(".");
                writeChildren(tree, writer, " ", 1);
                break;
            case "NormalAnnotation":
                writer.write("@");
                write(tree.getChild(0), writer);
                writer.write("(");
                writeChildren(tree, writer, ", ", 1);
                writer.write(")\n");
                break;
            case "NullLiteral":
                writer.write("null");
                break;
            case "NullPattern":
                writeChildren(tree, writer);
                break;
            case "OpensDirective":
                writer.write("opens ");
                write(tree.getChild(0), writer);
                if (tree.getChildren().size() > 1) {
                    writer.write(" to ");
                    writeChildren(tree, writer, ", ", 1);
                }
                break;
            case "PackageDeclaration":
                writer.write("package ");
                write(tree.getChild(0), writer);
                writer.write(";");
                break;
            case "ParameterizedType":
                write(tree.getChild(0), writer);
                writer.write("<");
                writeChildren(tree, writer, ", ", 1);
                writer.write(">");
                break;
            case "ParenthesizedExpression":
                writer.write("(");
                write(tree.getChild(0), writer);
                writer.write(")");
                break;
            case "PostfixExpression":
            case "PrefixExpression":
                writeChildren(tree, writer);
                break;
            case "ProvidesDirective":
                writer.write("provides ");
                write(tree.getChild(0), writer);
                writer.write(" with ");
                writeChildren(tree, writer, ", ", 1);
                break;
            case "QualifiedType":
                write(tree.getChild(0), writer);
                writer.write(".");
                writeChildren(tree, writer, " ", 1);
                break;
            case "RecordDeclaration":
                writeIndent(writer);
                last = writeChildren(tree, writer, " ", 0, "SimpleName", false);
                writer.write(" record ");
                write(tree.getChild(last++), writer);
                if (last < tree.getChildren().size() && tree.getChild(last).getType().name.equals("TypeParameter")) {
                    writer.write("<");
                    last = writeChildren(tree, writer, ", ", last, "TypeParameter");
                    writer.write(">");
                }
                writer.write("(");
                if (last < tree.getChildren().size() && tree.getChild(last).getType().name.equals("SingleVariableDeclaration")) {
                    last = writeChildren(tree, writer, ", ", last, "SingleVariableDeclaration");
                }
                writer.write(")");
                if (last < tree.getChildren().size() && TYPE_SETS.contains(tree.getChild(last).getType().name)) {
                    writer.write(" implements ");
                    write(tree.getChild(last++), writer);
                    while (last < tree.getChildren().size() && TYPE_SETS.contains(tree.getChild(last).getType().name)) {
                        writer.write(", ");
                        write(tree.getChild(last++), writer);
                    }
                }
                level++;
                writer.write(" {\n");
                writeChildren(tree, writer, "\n", last);
                level--;
                writer.write("\n");
                writeIndent(writer);
                writer.write("}\n");
                break;
            case "RecordPattern":
                // TODO, UNKNOWN
                break;
            case "RequiresDirective":
                writer.write("requires ");
                writeChildren(tree, writer, " ");
                break;
            case "ReturnStatement":
                writeIndent(writer);
                writer.write("return");
                if (tree.getChildren().size() > 0) {
                    writer.write(" ");
                    writeChildren(tree, writer);
                }
                writer.write(";");
                break;
            case "SimpleType":
                writeChildren(tree, writer);
                break;
            case "SingleMemberAnnotation":
                writer.write("@");
                write(tree.getChild(0), writer);
                writer.write("(");
                write(tree.getChild(1), writer);
                writer.write(")");
                break;
            case "SingleVariableDeclaration":
                last = writeChildren(tree, writer, " ", 0 , "Dimension", false);
                if (last < tree.getChildren().size() && tree.getChild(last).getType().name.equals("Dimension")) {
                    last = writeChildren(tree, writer, "", last, "Dimension");
                }
                if (last < tree.getChildren().size()) {
                    writeChildren(tree, writer, " ", last);
                }
                break;
            case "SuperConstructorInvocation":
                // TODO, other
                writeIndent(writer);
                writer.write("super(");
                writeChildren(tree, writer, ", ");
                writer.write(");");
                break;
            case "SuperFieldAccess":
                if (tree.getChildren().size() >= 2) {
                    write(tree.getChild(last++), writer);
                    writer.write(".");
                }
                writer.write("super.");
                write(tree.getChild(last), writer);
                break;
            case "SuperMethodInvocation":
                // TODO, other
                writeIndent(writer);
                write(tree.getChild(0), writer);
                writer.write("(");
                writeChildren(tree, writer, ", ", 1);
                writer.write(")");
                break;
            case "SuperMethodReference":
                // TODO, other
                writer.write("super::");
                write(tree.getChild(0), writer);
                break;
            case "SwitchCase":
                writeIndent(writer);
                if (tree.getChildren().size() > 0) {
                    writer.write("case ");
                    writeChildren(tree, writer, ", ");
                } else {
                    writer.write("default");
                }
                break;
            case "SwitchStatement":
            case "SwitchExpression":
                writeIndent(writer);
                writer.write("switch (");
                write(tree.getChild(0), writer);
                writer.write(") {\n");
                level++;
                for (int i = 1; i < tree.getChildren().size(); i += 2) {
                    write(tree.getChild(i), writer);
                    if (tree.getChild(i + 1).getType().name.equals("Block")) {
                        writer.write(" ->");
                    } else {
                        writer.write(": ");
                    }
                    write(tree.getChild(i + 1), writer);
                    if (i + 2 < tree.getChildren().size()) {
                        writer.write("\n");
                    }
                }
                level--;
                writer.write("\n");
                writeIndent(writer);
                writer.write("}");
                break;
            case "SynchronizedStatement":
                writeIndent(writer);
                writer.write("synchronized (");
                write(tree.getChild(0), writer);
                writer.write(")");
                write(tree.getChild(1), writer);
                break;
//            case "TagElement":
//                break;
//            case "TagProperty":
//                break;
//            case "TextBlock":
//                break;
            case "ThisExpression":
                if (tree.getChildren().size() > 0) {
                    write(tree.getChild(0), writer);
                    writer.write(".");
                }
                writer.write("this");
                break;
            case "ThrowStatement":
                writeIndent(writer);
                writer.write("throw ");
                write(tree.getChild(0), writer);
                writer.write(";");
                break;
            case "TryStatement":
                writeIndent(writer);
                writer.write("try");
                last = 0;
                if (!tree.getChild(last).getType().name.equals("Block")) {
                    writer.write("(");
                    last = writeChildren(tree, writer, "\n", last, "Block", false);
                    writer.write(")");
                }
                write(tree.getChild(last++), writer);
                last = writeChildren(tree, writer, "\n", last, "Block", false);
                if (last < tree.getChildren().size()) {
                    writer.write(" finally");
                    write(tree.getChild(last), writer);
                }
                break;
            case "TypeDeclaration":
                // TODO, match
                writeIndent(writer);
                last = writeChildren(tree, writer, " ", 0, "SimpleName", true);
                if (tree.getChild(last).getType().name.equals("TypeParameter")) {
                    writer.write("<");
                    last = writeChildren(tree, writer, ", ", last, "TypeParameter");
                    writer.write(">");
                }
                if (TYPE_SETS.contains(tree.getChild(last).getType().name)) {
                    writer.write(" extends ");
                    write(tree.getChild(last++), writer);
                }
                if (TYPE_SETS.contains(tree.getChild(last).getType().name)) {
                    writer.write(" implements ");
                    last = writeChildren(tree, writer, ", ", last, "SimpleType");
                }
                writer.write(" {\n");
                level++;
                writeChildren(tree, writer, "\n", last);
                level--;
                writer.write("\n}\n");
                break;
            case "TypeDeclarationStatement":
                writeChildren(tree, writer);
                break;
            case "TypeMethodReference":
                write(tree.getChild(last++), writer);
                writer.write("::");
                if (TYPE_SETS.contains(tree.getChild(last).getType().name)) {
                    writer.write("<");
                    last = writeChildren(tree, writer, ", ", last, "SimpleName", false);
                    writer.write(">");
                }
                write(tree.getChild(last), writer);
                break;
            case "TypeLiteral":
                write(tree.getChild(0), writer);
                writer.write(".class");
                break;
            case "TypeParameter":
                last = writeChildren(tree, writer, " ", 0, "SimpleName", true);
                if (last < tree.getChildren().size()) {
                    writer.write(" extends ");
                    writeChildren(tree, writer, " & ", last);
                }
                break;
            case "TypePattern":
                writeChildren(tree, writer);;
                break;
            case "UnionType":
                writeChildren(tree, writer, " | ");
                break;
            case "UsesDirective":
                writer.write("uses ");
                write(tree.getChild(0), writer);
                writer.write(";");
                break;
            case "VariableDeclarationExpression":
                last = writeChildren(tree, writer, " ", 0, "VariableDeclarationFragment", false);
                writer.write(" ");
                writeChildren(tree, writer, ", ", last);
                break;
            case "VariableDeclarationFragment":
                write(tree.getChild(last++), writer);
                if (last < tree.getChildren().size() && tree.getType().name.equals("Dimension")) {
                    last = writeChildren(tree, writer, "", last, "Dimension");
                }
                if (last < tree.getChildren().size()) {
                    writer.write(" = ");
                    write(tree.getChild(last), writer);
                }
                break;
            case "VariableDeclarationStatement":
                writeIndent(writer);
                last = writeChildren(tree, writer, " ", 0, "VariableDeclarationFragment", false);
                writer.write(" ");
                writeChildren(tree, writer, ", ", last);
                writer.write(";");
                break;
            case "WhileStatement":
                writeIndent(writer);
                writer.write("while (");
                write(tree.getChild(0), writer);
                writer.write(")");
                write(tree.getChild(1), writer);
                break;
            case "WildcardType":
                // TODO, extends / super
                writer.write("?");
                if (tree.getChildren().size() > 0) {
                    writer.write(" extends ");
                    write(tree.getChild(0), writer);
                }
                break;
            case "YieldStatement":
                writer.write("Yield ");
                writeChildren(tree, writer, " ");
                break;
            case "METHOD_INVOCATION_RECEIVER":
                writeChildren(tree, writer);
                break;
            case "METHOD_INVOCATION_ARGUMENTS":
                writeChildren(tree, writer, ", ");
                break;
            case "VARARGS_TYPE":
                write(tree.getChild(0), writer);
                writer.write("...");
                break;
        }
    }

    public static String generate(Tree tree) throws IOException {
        CodeGen codeGen = new CodeGen();
        StringWriter writer = new StringWriter();
        codeGen.write(tree, writer);
        return writer.toString();
    }
}
