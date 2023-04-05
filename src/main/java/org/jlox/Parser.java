package org.jlox;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static org.jlox.TokenType.BANG;
import static org.jlox.TokenType.BANG_EQUAL;
import static org.jlox.TokenType.COMMA;
import static org.jlox.TokenType.EOF;
import static org.jlox.TokenType.FALSE;
import static org.jlox.TokenType.GREATER;
import static org.jlox.TokenType.GREATER_EQUAL;
import static org.jlox.TokenType.LEFT_PAREN;
import static org.jlox.TokenType.LESS;
import static org.jlox.TokenType.LESS_EQUAL;
import static org.jlox.TokenType.MINUS;
import static org.jlox.TokenType.NIL;
import static org.jlox.TokenType.NUMBER;
import static org.jlox.TokenType.PLUS;
import static org.jlox.TokenType.RIGHT_PAREN;
import static org.jlox.TokenType.SEMICOLON;
import static org.jlox.TokenType.SLASH;
import static org.jlox.TokenType.STAR;
import static org.jlox.TokenType.STRING;
import static org.jlox.TokenType.TRUE;

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
        return binary(this::equality, List.of(COMMA));
    }

    private Expr equality() {
        return binary(this::comparison, Arrays.asList(BANG, BANG_EQUAL));
    }

    private Expr comparison() {
        return binary(this::term, Arrays.asList(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL));
    }

    private Expr term() {
        return binary(this::factor, Arrays.asList(MINUS, PLUS));
    }

    private Expr factor() {
        return binary(this::unary, Arrays.asList(SLASH, STAR));
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
            return new Expr.Literal(previous().literal());
        }
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after an expression");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expected an expression");
    }

    private Expr binary(Callable<Expr> exprType, List<TokenType> types) {
        try {
            Expr left = exprType.call();
            while (match(types.toArray(TokenType[]::new))) {
                Token operator = previous();
                Expr right = exprType.call();
                if (right == null) {
                    throw error(operator,
                            "Expected right-hand expression after " +
                                    operator.lexeme());
                }
                left = new Expr.Binary(left, operator, right);
            }
            return left;
        } catch (Exception e) {
            System.err.println("Error parsing expression");
        }
        return null;
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
            if (previous().type() == SEMICOLON) return;
            switch (peek().type()) {
                case CLASS, FOR, FUNC, IF, PRINT, RETURN, LET, WHILE -> {
                    return;
                }
            }
            advance();
        }
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
        return peek().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
