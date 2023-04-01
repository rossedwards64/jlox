package org.jlox;

import static org.jlox.TokenType.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;


public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expr parse() {
        try {
            return expression();
        } catch (ParseError p) {
            return null;
        }
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        return parseRule(this::comparison, Arrays.asList(BANG, BANG_EQUAL));
    }

    private Expr comparison() {
        return parseRule(this::term, Arrays.asList(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL));
    }

    private Expr term() {
        return parseRule(this::factor, Arrays.asList(MINUS, PLUS));
    }

    private Expr factor() {
        return parseRule(this::unary, Arrays.asList(SLASH, STAR));
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().getLiteral());
        }
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after an expression");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expected an expression");
    }

    private Token consume(TokenType type, String message) throws ParseError {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().getType() == SEMICOLON) return;
            switch (peek().getType()) {
                case CLASS, FOR, FUNC, IF, PRINT, RETURN, LET, WHILE -> {
                    return;
                }
            }
            advance();
        }
    }

    private Expr parseRule(Callable<Expr> exprType, List<TokenType> types) {
        try {
            Expr expr = exprType.call();
            while (match(types.toArray(TokenType[]::new))) {
                Token operator = previous();
                Expr right = exprType.call();
                expr = new Expr.Binary(expr, operator, right);
            }
            return expr;
        } catch (Throwable t) {
            System.err.println("Error parsing rule");
        }
        return null;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
