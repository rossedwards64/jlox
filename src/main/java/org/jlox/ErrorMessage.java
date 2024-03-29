package org.jlox;

public enum ErrorMessage {
    EXPECT("Expected %s."),
    EXPECT_BUT_GOT("Expected %s but got %s."),
    EXPECT_AFTER("Expected %s after %s."),
    EXPECT_BEFORE("Expected %s before %s."),

    NO_VAR_NAME(String.format(EXPECT.getMsg(), "variable name")),
    NO_EXPR(String.format(EXPECT.getMsg(), "an expression")),
    NO_PARAM_NAME(String.format(EXPECT.getMsg(), "parameter name")),
    NO_FUNC_NAME(String.format(EXPECT.getMsg(), "%s name")),
    NO_SUPERCLASS_NAME(String.format(EXPECT.getMsg(), "superclass name")),
    NO_RHS(String.format(EXPECT_AFTER.getMsg(), "right-hand expression", "%s")),
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
    NO_CALL_ARGS_END(String.format(EXPECT_AFTER.getMsg(), "')'", "function arguments")),
    NO_FUNC_PARAMS_START(String.format(EXPECT_AFTER.getMsg(), "'('", "%s name")),
    NO_FUNC_PARAMS_END(String.format(EXPECT_AFTER.getMsg(), "')'", "parameters")),
    NO_FUNC_BLOCK_START(String.format(EXPECT_BEFORE.getMsg(), "'{'", "%s body")),
    NO_CLASS_NAME(String.format(EXPECT.getMsg(), "class name")),
    NO_CLASS_BODY_START(String.format(EXPECT_BEFORE.getMsg(), "'{'", "class body")),
    NO_CLASS_BODY_END(String.format(EXPECT_AFTER.getMsg(), "'}'", "class body")),
    NO_PROPERTY(String.format(EXPECT_AFTER.getMsg(), "property name", "'.'")),
    INVALID_CALL_PARAMS(String.format(EXPECT_BUT_GOT.getMsg(), "%d arguments", "%d")),
    NO_RETURN_END(String.format(EXPECT.getMsg(), "';'", "return value")),
    NO_SUPER_ACCESS(String.format(EXPECT_AFTER.getMsg(), "'.'", "'super'")),
    NO_SUPER_METHOD_NAME(String.format(EXPECT.getMsg(), "superclass method name")),

    UNDEFINED_VARIABLE("Undefined variable %s."),
    EXISTING_VARIABLE("Already a variable with this name in scope."),
    EXPR_ERROR("Error parsing expression"),
    COND_ERROR("Error parsing conditional expression"),
    DIVIDE_BY_ZERO("Can't divide by zero."),
    INVALID_ASSIGN("Invalid assignment target."),
    INIT_ACCESS("Can't read local variable in its own initialization."),
    TOP_LEVEL_RETURN("Can't return from top-level code."),
    INVALID_CALL("Can only call functions and classes."),
    ARG_LIMIT("Can't have more than 255 parameters."),
    PARAM_LIMIT("Can't have more than 255 parameters."),
    OPERAND_NUMBER("Operand must be a number."),
    OPERAND_NUMBERS("Operands must be numbers."),
    MATCH_OPERANDS("Operands must be two numbers or two strings."),
    THIS_NO_CLASS("Can't use 'this' outside of a class."),
    RETURN_FROM_INIT("Can't return a value from an initializer."),
    NOT_INSTANCE("Only instances have properties."),
    UNDEFINED_PROPERTY("Undefined property '%s'."),
    SELF_INHERIT("A class can't inherit from itself."),
    SUPER_MUST_BE_CLASS("Superclass must be a class."),
    SUPER_OUTSIDE_CLASS("Can't use 'super' outside of a class."),
    SUPER_NO_SUPERCLASS("Can't use 'super' in a class with no superclass.");

    private final String msg;

    ErrorMessage(final String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
