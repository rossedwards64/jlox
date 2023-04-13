package org.jlox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static org.jlox.TokenType.BANG;
import static org.jlox.TokenType.BANG_EQUAL;
import static org.jlox.TokenType.EOF;
import static org.jlox.TokenType.EQUAL;
import static org.jlox.TokenType.FALSE;
import static org.jlox.TokenType.GREATER;
import static org.jlox.TokenType.GREATER_EQUAL;
import static org.jlox.TokenType.IDENTIFIER;
import static org.jlox.TokenType.LEFT_BRACE;
import static org.jlox.TokenType.LEFT_PAREN;
import static org.jlox.TokenType.LESS;
import static org.jlox.TokenType.LESS_EQUAL;
import static org.jlox.TokenType.LET;
import static org.jlox.TokenType.MINUS;
import static org.jlox.TokenType.NIL;
import static org.jlox.TokenType.NUMBER;
import static org.jlox.TokenType.PLUS;
import static org.jlox.TokenType.PRINT;
import static org.jlox.TokenType.RIGHT_BRACE;
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

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(LET)) return varDeclaration();
            return statement();
        } catch (ParseError e) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
        consume(SEMICOLON, "Expect ';' after anvariable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(PRINT)) return printStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after anvalue.");
        return new Stmt.Print(value);
    }

    private Expr expression() {
        return assignment();
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after anvalue.");
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expect '}' after anblock.");
        return statements;
    }

    private Expr assignment() {
        Expr expr = equality();
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).getName();
                return new Expr.Assign(name, value);
            }
            throw error(equals, "Invalid assignment target.");
        }
        return expr;
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
        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
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
                            "Expected right-hand expression after an" +
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
