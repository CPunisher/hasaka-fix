package com.cpunisher.hasakafix.repo;

public class Getafix {
    public static final String OLD_1_JAVA =
            """
                    class Test {
                        public void f() {
                            content.remove();
                        }
                    }
                    """;
    public static final String NEW_1_JAVA =
            """
                    class Test {
                        public void f() {
                            if (content == null)
                               return;
                            content.remove();
                        }
                    }
                    """;
    public static final String OLD_2_JAVA =
            """
                    class Test {
                        public void f() {
                            viewer.initialize();
                            content.render();
                        }
                    }
                    """;
    public static final String NEW_2_JAVA =
            """
                    class Test {
                        public void f() {
                            viewer.initialize();
                            if (content == null)
                                return;
                            content.render();
                        }
                    }
                    """;

    public static final String OLD_3_JAVA =
            """
                    class Test {
                        public void f() {
                            t.start();
                        }
                    }
                    """;
    public static final String NEW_3_JAVA =
            """
                    class Test {
                        public void f() {
                            if (t == null)
                                return;
                            t.start();
                        }
                    }
                    """;

    public static final String OLD_4_JAVA =
            """
                    class Test {
                        public void f() {
                            int x = list.get();
                        }
                    }
                    """;
    public static final String NEW_4_JAVA =
            """
                    class Test {
                        public void f() {
                            if (list == null)
                                return;
                            int x = list.get();
                        }
                    }
                    """;
}
