package org.jlox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static org.jlox.ErrorMessage.*;
import static org.jlox.LoxConstants.*;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;

    Resolver(final Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private enum FunctionType {
        NONE,
        FUNCTION,
        INITIALISER,
        METHOD
    }

    private enum ClassType {
        NONE,
        CLASS,
        SUBCLASS
    }

    public void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt statement) {
        statement.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;
        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme())) {
            Lox.error(name, EXISTING_VARIABLE.getMsg());
        }
        scope.put(name.lexeme(), false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme(), true);
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme())) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for (Token param : function.getParams()) {
            declare(param);
            define(param);
        }
        resolve(function.getBody());
        endScope();
        currentFunction = enclosingFunction;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.getValue());
        resolveLocal(expr, expr.getName());
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.getLeft());
        resolve(expr.getRight());
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.getCallee());
        for (Expr arg : expr.getArgs()) {
            resolve(arg);
        }
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.getObject());
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.getLeft());
        resolve(expr.getRight());
        return null;
    }

    @Override
    public Void visitSelfExpr(Expr.Self expr) {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.getKeyword(),
                    THIS_NO_CLASS.getMsg());
        }
        resolveLocal(expr, expr.getKeyword());
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.getValue());
        resolve(expr.getObject());
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.getKeyword(),
                    SUPER_OUTSIDE_CLASS.getMsg());
        } else if (currentClass != ClassType.SUBCLASS) {
            Lox.error(expr.getKeyword(),
                    SUPER_NO_SUPERCLASS.getMsg());
        }
        resolveLocal(expr, expr.getKeyword());
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.getExpression());
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() &&
            scopes.peek().get(expr.getName().lexeme()) == Boolean.FALSE) {
            Lox.error(expr.getName(), INIT_ACCESS.getMsg());
        }
        resolveLocal(expr, expr.getName());
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.getRight());
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.getStatements());
        endScope();
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;
        declare(stmt.getName());
        define(stmt.getName());
        if (stmt.getSuperclass() != null) {
            if (stmt.getName().lexeme().equals(
                    stmt.getSuperclass().getName().lexeme()
            )) {
                Lox.error(stmt.getSuperclass().getName().line(),
                        SELF_INHERIT.getMsg());
            }
            currentClass = ClassType.SUBCLASS;
            resolve(stmt.getSuperclass());
            beginScope();
            scopes.peek().put(SUPER.getName(), true);
        }
        beginScope();
        scopes.peek().put(SELF.getName(), true);
        for (Stmt.Function method : stmt.getMethods()) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.getName().lexeme().equals(INIT.getName())) {
                declaration = FunctionType.INITIALISER;
            }
            resolveFunction(method, declaration);
        }
        endScope();
        if (stmt.getSuperclass() != null) endScope();
        currentClass = enclosingClass;
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.getExpression());
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.getName());
        define(stmt.getName());
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.getCondition());
        resolve(stmt.getThenBranch());
        if (stmt.getElseBranch() != null) {
            resolve(stmt.getElseBranch());
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.getExpression());
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.getKeyword(), TOP_LEVEL_RETURN.getMsg());
        }
        if (stmt.getValue() != null) {
            if (currentFunction == FunctionType.INITIALISER) {
                Lox.error(stmt.getKeyword(),
                        RETURN_FROM_INIT.getMsg());
            }
            resolve(stmt.getValue());
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.getName());
        if (stmt.getInitializer() != null) {
            resolve(stmt.getInitializer());
        }
        define(stmt.getName());
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.getCondition());
        resolve(stmt.getBody());
        return null;
    }
}
