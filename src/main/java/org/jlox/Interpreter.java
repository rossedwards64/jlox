package org.jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jlox.ErrorMessage.DIVIDE_BY_ZERO;
import static org.jlox.ErrorMessage.INVALID_CALL;
import static org.jlox.ErrorMessage.INVALID_CALL_PARAMS;
import static org.jlox.ErrorMessage.MATCH_OPERANDS;
import static org.jlox.ErrorMessage.NOT_INSTANCE;
import static org.jlox.ErrorMessage.OPERAND_NUMBER;
import static org.jlox.ErrorMessage.OPERAND_NUMBERS;
import static org.jlox.ErrorMessage.SUPER_MUST_BE_CLASS;
import static org.jlox.LoxConstants.*;

public class Interpreter implements Expr.Visitor<Object>,
                                    Stmt.Visitor<Void> {
    private final Environment globals = new Environment();
    private final Map<Expr, Integer> locals = new HashMap<>();
    private Environment environment = globals;

    public Interpreter() {
        globals.define("clock",
            new LoxCallable() {
                @Override
                public int arity() {
                    return 0;
                }

                @Override
                public Object call(final Interpreter interpreter, final List<Object> args) {
                    return (double) System.currentTimeMillis() / 1000.0;
                }

                @Override
                public String toString() {
                    return "<native fn>";
                }
            }
        );
    }

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitAssignExpr(final Expr.Assign expr) {
        Object value = evaluate(expr.getValue());
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.getName(), value);
        } else {
            globals.assign(expr.getName(), value);
        }
        return value;
    }

    @Override
    public Object visitBinaryExpr(final Expr.Binary expr) {
        Object left = evaluate(expr.getLeft());
        Object right = evaluate(expr.getRight());
        switch (expr.getOperator().type()) {
            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                } else if (left instanceof String && right instanceof String) {
                    return left + (String) right;
                } else if (left instanceof String && right instanceof Double) {
                    return (String) left + right;
                } else if (left instanceof Double && right instanceof String) {
                    return left + (String) right;
                }
                throw new RuntimeError(expr.getOperator(),
                        MATCH_OPERANDS.getMsg());
            }
            case BANG_EQUAL -> {
                return !isEqual(left, right);
            }
            case EQUAL_EQUAL -> {
                return isEqual(left, right);
            }
            case MINUS -> {
                checkNumberOperands(expr.getOperator(), left, right);
                return (double) left - (double) right;
            }
            case SLASH -> {
                checkNumberOperands(expr.getOperator(), left, right);
                if ((double) left == 0 || (double) right == 0) {
                    throw new RuntimeError(expr.getOperator(),
                            DIVIDE_BY_ZERO.getMsg());
                }
                return (double) left / (double) right;
            }
            case STAR -> {
                checkNumberOperands(expr.getOperator(), left, right);
                return (double) left * (double) right;
            }
            case GREATER -> {
                checkNumberOperands(expr.getOperator(), left, right);
                return (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.getOperator(), left, right);
                return (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperands(expr.getOperator(), left, right);
                return (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.getOperator(), left, right);
                return (double) left <= (double) right;
            }
        }
        return null;
    }

    @Override
    public Object visitCallExpr(final Expr.Call expr) {
        Object callee = evaluate(expr.getCallee());
        List<Object> args = new ArrayList<>();
        for (Expr arg : expr.getArgs()) {
            args.add(evaluate(arg));
        }
        if (!(callee instanceof final LoxCallable function)) {
            throw new RuntimeError(expr.getParen(),
                    INVALID_CALL.getMsg());
        }
        if (args.size() != function.arity()) {
            throw new RuntimeError(expr.getParen(),
                    String.format(INVALID_CALL_PARAMS.getMsg(),
                            function.arity(), args.size()));
        }
        return function.call(this, args);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.getObject());
        if (object instanceof LoxInstance) {
            return ((LoxInstance) object).get(expr.getName());
        }
        throw new RuntimeError(expr.getName(), NOT_INSTANCE.getMsg());
    }

    @Override
    public Object visitLogicalExpr(final Expr.Logical expr) {
        Object left = evaluate(expr.getLeft());
        if (expr.getOperator().type() == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }
        return evaluate(expr.getRight());
    }

    @Override
    public Object visitSelfExpr(Expr.Self expr) {
        return lookUpVariable(expr.getKeyword(), expr);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.getObject());
        if(!(object instanceof LoxInstance)) {
            throw new RuntimeError(expr.getName(),
                    "Only instances have fields.");
        }
        Object value = evaluate(expr.getValue());
        ((LoxInstance) object).set(expr.getName(), value);
        return null;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int distance = locals.get(expr);
        LoxClass superclass = (LoxClass) environment.getAt(
                distance, SUPER.getName());
        LoxInstance object = (LoxInstance) environment.getAt(
                distance - 1, THIS.getName());
        LoxFunction method = superclass.findMethod(expr.getMethod().lexeme());
        return method.bind(object);
    }

    @Override
    public Object visitGroupingExpr(final Expr.Grouping expr) {
        return evaluate(expr.getExpression());
    }

    @Override
    public Object visitLiteralExpr(final Expr.Literal expr) {
        return expr.getValue();
    }

    @Override
    public Object visitVariableExpr(final Expr.Variable expr) {
        return lookUpVariable(expr.getName(), expr);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme());
        } else {
            return globals.get(name);
        }
    }

    @Override
    public Object visitUnaryExpr(final Expr.Unary expr) {
        Object right = evaluate(expr.getRight());
        switch (expr.getOperator().type()) {
            case MINUS -> {
                checkNumberOperand(expr.getOperator(), right);
                return -(double) right;
            }
            case BANG -> {
                return !isTruthy(right);
            }
        }
        return null;
    }

    @Override
    public Void visitBlockStmt(final Stmt.Block stmt) {
        executeBlock(stmt.getStatements(), new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Object superclass = null;
        if (stmt.getSuperclass() != null) {
            superclass = evaluate(stmt.getSuperclass());
            if (!(superclass instanceof LoxClass)) {
                throw new RuntimeError(stmt.getSuperclass().getName(),
                        SUPER_MUST_BE_CLASS.getMsg());
            }
        }
        environment.define(stmt.getName().lexeme(), null);
        if (stmt.getSuperclass() != null) {
            environment = new Environment(environment);
            environment.define(SUPER.getName(), superclass);
        }
        Map<String, LoxFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.getMethods()) {
            LoxFunction function = new LoxFunction(method,
                    environment,
                    method.getName().lexeme().equals(INIT.getName()));
            methods.put(method.getName().lexeme(), function);
        }
        LoxClass clazz = new LoxClass(stmt.getName().lexeme(),
                (LoxClass) superclass, methods);
        if (superclass != null) {
            environment = environment.getEnclosing();
        }
        environment.assign(stmt.getName(), clazz);
        return null;
    }

    @Override
    public Void visitExpressionStmt(final Stmt.Expression stmt) {
        evaluate(stmt.getExpression());
        return null;
    }

    @Override
    public Void visitFunctionStmt(final Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt,
                environment, false);
        environment.define(stmt.getName().lexeme(), function);
        return null;
    }

    @Override
    public Void visitIfStmt(final Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.getCondition()))) {
            execute(stmt.getThenBranch());
        } else if (stmt.getElseBranch() != null) {
            execute(stmt.getElseBranch());
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(final Stmt.Print stmt) {
        Object value = evaluate(stmt.getExpression());
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(final Stmt.Return stmt) {
        Object value = null;
        if (stmt.getValue() != null) value = evaluate(stmt.getValue());
        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(final Stmt.Var stmt) {
        Object value = null;
        if (stmt.getInitializer() != null) {
            value = evaluate(stmt.getInitializer());
        }
        environment.define(stmt.getName().lexeme(), value);
        return null;
    }

    @Override
    public Void visitWhileStmt(final Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.getCondition()))) {
            execute(stmt.getBody());
        }
        return null;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    public void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    public void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    private boolean isEqual(Object lhs, Object rhs) {
        if (lhs == null && rhs == null) return true;
        if (lhs == null) return false;
        return lhs.equals(rhs);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, OPERAND_NUMBER.getMsg());
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, OPERAND_NUMBERS.getMsg());
    }
}
