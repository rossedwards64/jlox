package org.jlox;

public class Interpreter implements Expr.Visitor<Object> {
    public void interpret(Expr expr) {
        try {
            Object value = evaluate(expr);
            System.out.println(stringify(value));
        } catch(RuntimeError error) {
            Lox.runtimeError(error);
        }
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
                    return (String) left + (String) right;
                }
                throw new RuntimeError(expr.getOperator(),
                                       "Operands must be two numbers or two strings");
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
    public Object visitGroupingExpr(final Expr.Grouping expr) {
        return evaluate(expr.getExpression());
    }

    @Override
    public Object visitLiteralExpr(final Expr.Literal expr) {
        return expr.getValue();
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

    private Object evaluate(Expr expr) {
        return expr.accept(this);
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
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }
}
