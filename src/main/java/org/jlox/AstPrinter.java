package org.jlox;

public class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr)  {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(final Expr.Binary expr) {
        return parenthesise(expr.getOperator().getLexeme(),
                expr.getLeft(), expr.getRight());
    }

    @Override
    public String visitGroupingExpr(final Expr.Grouping expr) {
        return parenthesise("group", expr.getExpression());
    }

    @Override
    public String visitLiteralExpr(final Expr.Literal expr) {
        if (expr.getValue() == null) return "nil";
        return expr.getValue().toString();
    }

    @Override
    public String visitUnaryExpr(final Expr.Unary expr) {
        return parenthesise(expr.getOperator().getLexeme(),
                expr.getRight());
    }

    private String parenthesise(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }
}
