package org.jlox;

import static org.jlox.TokenType.AND;
import static org.jlox.TokenType.BANG;
import static org.jlox.TokenType.BANG_EQUAL;
import static org.jlox.TokenType.CLASS;
import static org.jlox.TokenType.COMMA;
import static org.jlox.TokenType.DOT;
import static org.jlox.TokenType.ELSE;
import static org.jlox.TokenType.EOF;
import static org.jlox.TokenType.EQUAL;
import static org.jlox.TokenType.EQUAL_EQUAL;
import static org.jlox.TokenType.FALSE;
import static org.jlox.TokenType.FOR;
import static org.jlox.TokenType.FUNC;
import static org.jlox.TokenType.GREATER;
import static org.jlox.TokenType.GREATER_EQUAL;
import static org.jlox.TokenType.IDENTIFIER;
import static org.jlox.TokenType.IF;
import static org.jlox.TokenType.LEFT_BRACE;
import static org.jlox.TokenType.LEFT_PAREN;
import static org.jlox.TokenType.LESS;
import static org.jlox.TokenType.LESS_EQUAL;
import static org.jlox.TokenType.MINUS;
import static org.jlox.TokenType.NIL;
import static org.jlox.TokenType.NUMBER;
import static org.jlox.TokenType.OR;
import static org.jlox.TokenType.PLUS;
import static org.jlox.TokenType.PRINT;
import static org.jlox.TokenType.RETURN;
import static org.jlox.TokenType.RIGHT_BRACE;
import static org.jlox.TokenType.RIGHT_PAREN;
import static org.jlox.TokenType.SEMICOLON;
import static org.jlox.TokenType.SLASH;
import static org.jlox.TokenType.STAR;
import static org.jlox.TokenType.STRING;
import static org.jlox.TokenType.SUPER;
import static org.jlox.TokenType.THIS;
import static org.jlox.TokenType.TRUE;
import static org.jlox.TokenType.VAR;
import static org.jlox.TokenType.WHILE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private static final Map<String, TokenType> KEYWORDS;

    static {
        KEYWORDS = new HashMap<>();
        KEYWORDS.put("and", AND);
        KEYWORDS.put("class", CLASS);
        KEYWORDS.put("else", ELSE);
        KEYWORDS.put("false", FALSE);
        KEYWORDS.put("for", FOR);
        KEYWORDS.put("func", FUNC);
        KEYWORDS.put("if", IF);
        KEYWORDS.put("nil", NIL);
        KEYWORDS.put("or", OR);
        KEYWORDS.put("print", PRINT);
        KEYWORDS.put("return", RETURN);
        KEYWORDS.put("super", SUPER);
        KEYWORDS.put("this", THIS);
        KEYWORDS.put("true", TRUE);
        KEYWORDS.put("var", VAR);
        KEYWORDS.put("while", WHILE);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            /* OPERATORS */
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*':
                if (match('/')) {
                    if (peek() == '\0') {
                        break;
                    } else {
                        advance();
                    }
                } else {
                    addToken(STAR); break;
                }
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '>':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '<':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/'))
                    while (peek() != '\n' && !isAtEnd()) advance();
                else if (match('*')) {
                    comment();
                } else
                    addToken(SLASH);
                break;

            /* STRINGS */
            case '"': string(); break;

            /* WHITESPACE */
            case ' ': case '\r': case '\t': break;
            case '\n': line++; break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void addToken(final TokenType type) {
        addToken(type, null);
    }

    private void addToken(final TokenType type, final Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(final char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean isAlpha(final char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(final char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = KEYWORDS.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void traverseString() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
    }

    private void string() {
        traverseString();
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }
        advance(); /* consume the closing speech mark */
        String value = source.substring(start + 1, current - 1); /* remove the speech marks */
        addToken(STRING, value);
    }

    private void traverseNumber() {
        while (isDigit(peek())) advance();
    }

    private boolean isDigit(final char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        traverseNumber();
        /* check if number is floating point */
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); /* consume the decimal point */
            traverseNumber();
        }
        double value = Double.parseDouble(source.substring(start, current));
        addToken(NUMBER, value);
    }

    private boolean isEndOfComment(final char c) {
        if (c == '*') {
            return peekNext() == '/';
        }
        return false;
    }

    private void comment() {
        while (!isEndOfComment(peek())) advance();
    }
}
