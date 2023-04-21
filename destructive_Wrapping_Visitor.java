package ca.concordia.soen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

public class DestructiveWrappingVisitor1 {

    public static void main(String[] args) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setResolveBindings(true);
        for (String arg : args) {
            File file = new File(arg);
            if (file.isDirectory()) {
                processDirectory(file, parser);
            } else if (file.isFile()) {
                processFile(file, parser);
            } else {
                System.err.println("Invalid input: " + arg);
            }
        }
    }

    private static void processDirectory(File directory, ASTParser parser) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    processDirectory(file, parser);
                } else if (file.isFile()) {
                    processFile(file, parser);
                }
            }
        }
    }

    private static void processFile(File file, ASTParser parser) {
        String source;
        try {
            source = read(file);
        } catch (IOException e) {
            System.err.println(e);
            return;
        }

        parser.setSource(source.toCharArray());
        ASTNode unit = parser.createAST(null);
        unit.accept(new Visitor(file.getPath()));
    }

    private static String read(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }
        reader.close();
        return builder.toString();
    }

    static class Visitor extends ASTVisitor {
        private String currentMethod;
        private String filePath;

        public Visitor(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            currentMethod = node.getName().getFullyQualifiedName();
            return true;
        }

        public boolean visit(CatchClause node) {
            String caughtTypeName = node.getException().getType().toString();

            List<Statement> statements = node.getBody().statements();
            for (Statement statement : statements) {
                if (statement instanceof ThrowStatement) {
                    Expression expression = ((ThrowStatement) statement).getExpression();
                    String thrownTypeName = expression.toString();
                    if (thrownTypeName.contains("(")) {
                        thrownTypeName = thrownTypeName.substring(4,thrownTypeName.indexOf("("));
                    }
                    else
                    	break;
                    if(!caughtTypeName.equals(thrownTypeName)) {
                        System.out.println("Destructive wrapping detected in file: " + filePath);
                        System.out.println("Line number: " + ((CompilationUnit)node.getRoot()).getLineNumber(expression.getStartPosition()));
                        System.out.println("Caught exception type: " + caughtTypeName);    
                        System.out.println("Thrown exception type: " + thrownTypeName);
                    }
                    break;
                }
            }
            return true;
        }

    }
}

