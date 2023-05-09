package org.jlox;

import java.util.List;

abstract class Stmt {
    interface Visitor<R> {
        R visitBlockStmt(Block stmt);
        R visitClassStmt(Class stmt);
        R visitExpressionStmt(Expression stmt);
        R visitFunctionStmt(Function stmt);
        R visitIfStmt(If stmt);
        R visitPrintStmt(Print stmt);
        R visitReturnStmt(Return stmt);
        R visitVarStmt(Var stmt);
        R visitWhileStmt(While stmt);
    }

    abstract <R> R accept(Visitor<R> visitor);

    static class Block extends Stmt {
        private final List<Stmt> statements;

        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        public List<Stmt> getStatements() {
            return statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    static class Class extends Stmt {
        private final Token name;
        private final Expr.Variable superclass;
        private final List<Stmt.Function> methods;

        Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods) {
            this.name = name;
            this.superclass = superclass;
            this.methods = methods;
        }

        public Token getName() {
            return name;
        }

        public Expr.Variable getSuperclass() {
            return superclass;
        }

        public List<Stmt.Function> getMethods() {
            return methods;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitClassStmt(this);
        }
    }

    static class Expression extends Stmt {
        private final Expr expression;

        Expression(Expr expression) {
            this.expression = expression;
        }

        public Expr getExpression() {
            return expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    static class Function extends Stmt {
        private final Token name;
        private final List<Token> params;
        private final List<Stmt> body;

        Function(Token name, List<Token> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        public Token getName() {
            return name;
        }

        public List<Token> getParams() {
            return params;
        }

        public List<Stmt> getBody() {
            return body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }
    }

    static class If extends Stmt {
        private final Expr condition;
        private final Stmt thenBranch;
        private final Stmt elseBranch;

        If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        public Expr getCondition() {
            return condition;
        }

        public Stmt getThenBranch() {
            return thenBranch;
        }

        public Stmt getElseBranch() {
            return elseBranch;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    static class Print extends Stmt {
        private final Expr expression;

        Print(Expr expression) {
            this.expression = expression;
        }

        public Expr getExpression() {
            return expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    static class Return extends Stmt {
        private final Token keyword;
        private final Expr value;

        Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        public Token getKeyword() {
            return keyword;
        }

        public Expr getValue() {
            return value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    static class Var extends Stmt {
        private final Token name;
        private final Expr initializer;

        Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        public Token getName() {
            return name;
        }

        public Expr getInitializer() {
            return initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    static class While extends Stmt {
        private final Expr condition;
        private final Stmt body;

        While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        public Expr getCondition() {
            return condition;
        }

        public Stmt getBody() {
            return body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }
}
