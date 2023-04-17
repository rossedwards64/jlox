package org.jlox;

public enum ErrorMessage {
    EXPECT("Expected %s."),
    EXPECT_AFTER("Expected %s after %s."),
    NO_VAR_NAME(String.format(EXPECT.getMsg(), "variable name")),
    NO_EXPR(String.format(EXPECT.getMsg(), "an expression")),
    NO_VAR_END(String.format(EXPECT_AFTER.getMsg(), "';'", "a variable declaration")),
    NO_IF_START(String.format(EXPECT_AFTER.getMsg(), "'('", "'if'")),
    NO_IF_END(String.format(EXPECT_AFTER.getMsg(), "')'", "if condition")),
    NO_STMT_END(String.format(EXPECT_AFTER.getMsg(), "';'", "a value")),
    NO_EXPR_END(String.format(EXPECT_AFTER.getMsg(), ")", "an expression")),
    NO_WHILE_START(String.format(EXPECT_AFTER.getMsg(), "'('", "'while'")),
    NO_WHILE_END(String.format(EXPECT_AFTER.getMsg(), "')'", "condition")),
    NO_FOR_START(String.format(EXPECT_AFTER.getMsg(), "'('", "'for'")),
    NO_FOR_COND_END(String.format(EXPECT_AFTER.getMsg(), "';'", "loop condition")),
    NO_FOR_CLAUSE_END(String.format(EXPECT_AFTER.getMsg(), "')'", "for clauses")),
    NO_BLOCK_END(String.format(EXPECT_AFTER.getMsg(), "'}'", "a block")),
    ;

    private final String msg;

    ErrorMessage(final String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
