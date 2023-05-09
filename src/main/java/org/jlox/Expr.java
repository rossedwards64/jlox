package org.jlox;

import java.util.List;

abstract class Expr {
    interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitBinaryExpr(Binary expr);
        R visitCallExpr(Call expr);
        R visitGroupingExpr(Grouping expr);
        R visitGetExpr(Get expr);
        R visitLiteralExpr(Literal expr);
        R visitLogicalExpr(Logical expr);
        R visitSelfExpr(Self expr);
        R visitSetExpr(Set expr);
        R visitUnaryExpr(Unary expr);
        R visitVariableExpr(Variable expr);
    }

    abstract <R> R accept(Visitor<R> visitor);

    static class Assign extends Expr {
        private final Token name;
        private final Expr value;

        Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        public Token getName() {
            return name;
        }

        public Expr getValue() {
            return value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    static class Binary extends Expr {
        private final Expr left;
        private final Token operator;
        private final Expr right;

        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public Expr getLeft() {
            return left;
        }

        public Token getOperator() {
            return operator;
        }

        public Expr getRight() {
            return right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    static class Call extends Expr {
        private final Expr callee;
        private final Token paren;
        private final List<Expr> args;

        Call(Expr callee, Token paren, List<Expr> args) {
            this.callee = callee;
            this.paren = paren;
            this.args = args;
        }

        public Expr getCallee() {
            return callee;
        }

        public Token getParen() {
            return paren;
        }

        public List<Expr> getArgs() {
            return args;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    static class Grouping extends Expr {
        private final Expr expression;

        Grouping(Expr expression) {
            this.expression = expression;
        }

        public Expr getExpression() {
            return expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    static class Get extends Expr {
        private final Expr object;
        private final Token name;

        Get(Expr object, Token name) {
            this.object = object;
            this.name = name;
        }

        public Expr getObject() {
            return object;
        }

        public Token getName() {
            return name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    static class Literal extends Expr {
        private final Object value;

        Literal(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    static class Logical extends Expr {
        private final Expr left;
        private final Token operator;
        private final Expr right;

        Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public Expr getLeft() {
            return left;
        }

        public Token getOperator() {
            return operator;
        }

        public Expr getRight() {
            return right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    static class Self extends Expr {
        private final Token keyword;

        Self(Token keyword) {
            this.keyword = keyword;
        }

        public Token getKeyword() {
            return keyword;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSelfExpr(this);
        }
    }

    static class Set extends Expr {
        private final Expr object;
        private final Token name;
        private final Expr value;

        Set(Expr object, Token name, Expr value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        public Expr getObject() {
            return object;
        }

        public Token getName() {
            return name;
        }

        public Expr getValue() {
            return value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }

    static class Unary extends Expr {
        private final Token operator;
        private final Expr right;

        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        public Token getOperator() {
            return operator;
        }

        public Expr getRight() {
            return right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    static class Variable extends Expr {
        private final Token name;

        Variable(Token name) {
            this.name = name;
        }

        public Token getName() {
            return name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }
}
