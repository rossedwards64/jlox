package org.jlox;

import java.util.List;

public abstract class Expr {
    public interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitBinaryExpr(Binary expr);
        R visitCallExpr(Call expr);
        R visitGroupingExpr(Grouping expr);
        R visitGetExpr(Get expr);
        R visitLiteralExpr(Literal expr);
        R visitLogicalExpr(Logical expr);
        R visitSelfExpr(Self expr);
        R visitSetExpr(Set expr);
        R visitSuperExpr(Super expr);
        R visitUnaryExpr(Unary expr);
        R visitVariableExpr(Variable expr);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public static class Assign extends Expr {
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
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    public static class Binary extends Expr {
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
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    public static class Call extends Expr {
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
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    public static class Grouping extends Expr {
        private final Expr expression;

        Grouping(Expr expression) {
            this.expression = expression;
        }

        public Expr getExpression() {
            return expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    public static class Get extends Expr {
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
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    public static class Literal extends Expr {
        private final Object value;

        Literal(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    public static class Logical extends Expr {
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
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    public static class Self extends Expr {
        private final Token keyword;

        Self(Token keyword) {
            this.keyword = keyword;
        }

        public Token getKeyword() {
            return keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSelfExpr(this);
        }
    }

    public static class Set extends Expr {
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
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }

    public static class Super extends Expr {
        private final Token keyword;
        private final Token method;

        Super(Token keyword, Token method) {
            this.keyword = keyword;
            this.method = method;
        }

        public Token getKeyword() {
            return keyword;
        }

        public Token getMethod() {
            return method;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSuperExpr(this);
        }
    }

    public static class Unary extends Expr {
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
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    public static class Variable extends Expr {
        private final Token name;

        Variable(Token name) {
            this.name = name;
        }

        public Token getName() {
            return name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }
}
