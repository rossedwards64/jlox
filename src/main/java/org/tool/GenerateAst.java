package org.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) {
        if (args.length != 1)  {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Assign   : Token name, Expr value",
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Variable      : Token name",
                "Unary    : Token operator, Expr right"
        ));
        defineAst(outputDir, "Stmt", Arrays.asList(
                "Block      : List<Stmt> statements",
                "Expression : Expr expression",
                "Print      : Expr expression",
                "Var        : Token name, Expr initializer"
        ));
    }

    private static void defineAst(final String outputDir, final String baseName,
                                  final List<String> types) {
        final String path = outputDir + "/" + baseName + ".java";
        try (PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8)) {
            writer.println("package org.jlox;");
            newLine(writer);
            writer.println("import java.util.List;");
            newLine(writer);

            writer.println("abstract class " + baseName + " {");
            defineVisitor(writer, baseName, types);
            writer.println("    abstract <R> R accept(Visitor<R> visitor);");
            newLine(writer);

            /* AST classes */
            for(int i = 0; i < types.size(); i++) {
                String className = types.get(i).split(":")[0].trim();
                String fields = types.get(i).split(":")[1].trim();
                defineType(writer, baseName, className, fields);
                if (i != types.size() - 1) {
                    newLine(writer);
                }
            }
            writer.println("}");
        } catch(IOException e) {
            System.err.println("Error generating syntax tree.");
        }
    }

    private static void defineType(final PrintWriter writer, final String baseName,
                                   final String className, final String fieldList) {
        writer.println("    static class " + className + " extends " + baseName + " {");
        final String[] fields = fieldList.split(", ");
        for (String field : fields) {
            writer.println("        private final "  + field + ";");
        }
        newLine(writer);
        writer.println("        " + className + "(" + fieldList + ") {");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }
        writer.println("        }");
        newLine(writer);
        for (String field : fields) {
            String type = field.split(" ")[0];
            String name = field.split(" ")[1];
            String getterName = "get" + capitaliseFirstLetter(name) + "()";
            writer.println("        public " + type + " " + getterName + " {");
            writer.println("            return " + name + ";");
            writer.println("        }");
            newLine(writer);
        }
        writer.println("        @Override");
        writer.println("        <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");
        writer.println("    }");
    }

    private static void defineVisitor(final PrintWriter writer, final String baseName,
                                      final List<String> types) {
        writer.println("    interface Visitor<R> {");
        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println("    }");
        newLine(writer);
    }

    private static void newLine(final PrintWriter writer) {
        writer.println();
    }

    private static String capitaliseFirstLetter(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
}
