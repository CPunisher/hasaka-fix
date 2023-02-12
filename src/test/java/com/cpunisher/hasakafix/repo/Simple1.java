package com.cpunisher.hasakafix.repo;

public class Simple1 {
    public static final String OLD_WORKER_DOT_JAVA = "public class Worker {\n" +
            "    public long getRuntime() {\n" +
            "        return now - start;\n" +
            "    }\n" +
            "\n" +
            "    public void doWork() {\n" +
            "        task.makeProgress();\n" +
            "    }\n" +
            "}";

    public static final String NEW_WORKER_DOT_JAVA = "public class Worker {\n" +
            "    private long getRuntime() {\n" +
            "        return now - start;\n" +
            "    }\n" +
            "\n" +
            "    public void doWork() {\n" +
            "        if (task == null) {\n" +
            "            return;\n" +
            "        }\n" +
            "        task.makeProgress();\n" +
            "    }\n" +
            "}";
    public static final String TREE_NEW_WORKER_DOT_JAVA = "CompilationUnit [0,210]\n"+
        "    TypeDeclaration [0,210]\n"+
        "        Modifier: public [0,6]\n"+
        "        TYPE_DECLARATION_KIND: class [7,12]\n"+
        "        SimpleName: Worker [13,19]\n"+
        "        MethodDeclaration [26,87]\n"+
        "            Modifier: private [26,33]\n"+
        "            PrimitiveType: long [34,38]\n"+
        "            SimpleName: getRuntime [39,49]\n"+
        "            Block [52,87]\n"+
        "                ReturnStatement [62,81]\n"+
        "                    InfixExpression [69,80]\n"+
        "                        SimpleName: now [69,72]\n"+
        "                        INFIX_EXPRESSION_OPERATOR: - [73,74]\n"+
        "                        SimpleName: start [75,80]\n"+
        "        MethodDeclaration [93,208]\n"+
        "            Modifier: public [93,99]\n"+
        "            PrimitiveType: void [100,104]\n"+
        "            SimpleName: doWork [105,111]\n"+
        "            Block [114,208]\n"+
        "                IfStatement [124,173]\n"+
        "                    InfixExpression [128,140]\n"+
        "                        SimpleName: task [128,132]\n"+
        "                        INFIX_EXPRESSION_OPERATOR: == [133,135]\n"+
        "                        NullLiteral [136,140]\n"+
        "                    Block [142,173]\n"+
        "                        ReturnStatement [156,163]\n"+
        "                ExpressionStatement [182,202]\n"+
        "                    MethodInvocation [182,201]\n"+
        "                        METHOD_INVOCATION_RECEIVER [182,186]\n"+
        "                            SimpleName: task [182,186]\n"+
        "                        SimpleName: makeProgress [187,199]";
}
