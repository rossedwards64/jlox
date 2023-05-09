package org.jlox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static org.jlox.ErrorMessage.*;
import static org.jlox.TokenType.*;

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
            if (match(CLASS)) return classDeclaration();
            if (match(FUNC)) return function("function");
            if (match(LET)) return varDeclaration();
            return statement();
        } catch (ParseError e) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, NO_CLASS_NAME.getMsg());
        Expr.Variable superclass = null;
        if (match(LESS)) {
            consume(IDENTIFIER, NO_SUPERCLASS_NAME.getMsg());
            superclass = new Expr.Variable(previous());
        }
        consume(LEFT_BRACE, NO_CLASS_BODY_START.getMsg());
        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("method"));
        }
        consume(RIGHT_BRACE, NO_CLASS_BODY_END.getMsg());
        return new Stmt.Class(name, superclass, methods);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, NO_VAR_NAME.getMsg());
        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
        consume(SEMICOLON, NO_VAR_END.getMsg());
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(FOR)) return forStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, NO_IF_START.getMsg());
        Expr condition = expression();
        consume(RIGHT_PAREN, NO_IF_END.getMsg());
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, NO_STMT_END.getMsg());
        return new Stmt.Print(value);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }
        consume(SEMICOLON, NO_RETURN_END.getMsg());
        return new Stmt.Return(keyword, value);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, NO_WHILE_START.getMsg());
        Expr condition = expression();
        consume(RIGHT_PAREN, NO_WHILE_END.getMsg());
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, NO_FOR_START.getMsg());
        Stmt initializer;
        if (match(SEMICOLON)) initializer = null;
        else if (match(LET)) initializer = varDeclaration();
        else initializer = expressionStatement();
        Expr condition = consumeForLoopClause(SEMICOLON,
                NO_FOR_COND_END.getMsg());
        Expr increment = consumeForLoopClause(RIGHT_PAREN,
                NO_FOR_CLAUSE_END.getMsg());
        Stmt body = statement();
        if (increment != null)
            body = new Stmt.Block(Arrays.asList(body,
                    new Stmt.Expression(increment)));
        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);
        if (initializer != null) body = new Stmt.Block(Arrays.asList(initializer, body));
        return body;
    }

    private Expr expression() {
        return assignment();
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, NO_STMT_END.getMsg());
        return new Stmt.Expression(expr);
    }

    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, String.format(NO_FUNC_NAME.getMsg(), kind));
        consume(LEFT_PAREN, String.format(NO_FUNC_PARAMS_START.getMsg(), kind));
        List<Token> params = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (params.size() >= 255) {
                    error(peek(), PARAM_LIMIT.getMsg());
                }
                params.add(consume(IDENTIFIER, NO_PARAM_NAME.getMsg()));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, NO_FUNC_PARAMS_END.getMsg());
        consume(LEFT_BRACE, String.format(NO_FUNC_BLOCK_START.getMsg(), kind));
        List<Stmt> body = block();
        return new Stmt.Function(name, params, body);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, NO_BLOCK_END.getMsg());
        return statements;
    }

    private Expr assignment() {
        Expr expr = or();
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).getName();
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get get) {
                return new Expr.Set(get.getObject(), get.getName(), value);
            }
            throw error(equals, INVALID_ASSIGN.getMsg());
        }
        return expr;
    }

    private Expr or() {
        return conditional(this::and, OR);
    }

    private Expr and() {
        return conditional(this::equality, AND);
    }

    private Expr conditional(Callable<Expr> exprType, TokenType type) {
        try {
            Expr expr = exprType.call();
            while (match(type)) {
                Token operator = previous();
                Expr right = exprType.call();
                expr = new Expr.Logical(expr, operator, right);
            }
            return expr;
        } catch (Exception e) {
            System.err.println(COND_ERROR.getMsg());
        }
        return null;
    }

    private Expr equality() {
        return binary(this::comparison, Arrays.asList(EQUAL_EQUAL, BANG_EQUAL));
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
        return call();
    }

    private Expr call() {
        Expr expr = primary();
        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER,
                        NO_PROPERTY.getMsg());
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> args = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (args.size() >= 255) {
                    error(peek(), ARG_LIMIT.getMsg());
                }
                args.add(expression());
            } while (match(COMMA));
        }
        Token paren = consume(RIGHT_PAREN,
                NO_CALL_ARGS_END.getMsg());
        return new Expr.Call(callee, paren, args);
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal());
        }
        if (match(SUPER)) {
            Token keyword = previous();
            consume(DOT, NO_SUPER_ACCESS.getMsg());
            Token method = consume(IDENTIFIER,
                    NO_SUPER_METHOD_NAME.getMsg());
            return new Expr.Super(keyword, method);
        }
        if (match(SELF)) return new Expr.Self(previous());
        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, NO_EXPR_END.getMsg());
            return new Expr.Grouping(expr);
        }
        throw error(peek(), NO_EXPR.getMsg());
    }

    private Expr binary(Callable<Expr> exprType, List<TokenType> types) {
        try {
            Expr left = exprType.call();
            while (match(types.toArray(TokenType[]::new))) {
                Token operator = previous();
                Expr right = exprType.call();
                if (right == null) {
                    throw error(operator,
                            String.format(NO_RHS.getMsg(),
                                    operator.lexeme()));
                }
                left = new Expr.Binary(left, operator, right);
            }
            return left;
        } catch (Exception e) {
            System.err.println(EXPR_ERROR.getMsg());
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

    private Expr consumeForLoopClause(TokenType tokenType, String message) {
        Expr clause = null;
        if (!check(tokenType)) {
            clause = expression();
        }
        consume(tokenType, message);
        return clause;
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
