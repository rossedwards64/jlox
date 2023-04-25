package org.jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final Interpreter INTERPRETER = new Interpreter();

    private static boolean HAD_ERROR = false;
    private static boolean HAD_RUNTIME_ERROR = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (HAD_ERROR) System.exit(65);
        if (HAD_RUNTIME_ERROR) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        Reader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("jlox> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            HAD_ERROR = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        if (HAD_ERROR) return;
        Resolver resolver = new Resolver(INTERPRETER);
        resolver.resolve(statements);
        if (HAD_ERROR) return;
        INTERPRETER.interpret(statements);
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    public static void error(Token token, String message) {
        if (token.type() == TokenType.EOF) {
            report(token.line(), " at end", message);
        } else {
            report(token.line(), " at '" + token.lexeme() + "'", message);
        }
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                           "\n[line " + error.getToken().line() + "]");
        HAD_RUNTIME_ERROR = true;
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error " + where + ": " + message);
        HAD_ERROR = true;
    }
}
