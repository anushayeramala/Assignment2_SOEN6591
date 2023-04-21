package ca.concordia.soen;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jface.text.Document;

public class NestedTryVisitor {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Please provide a directory path");
            return;
        }

        Stack<File> stack = new Stack<>();
        stack.push(new File(args[0]));
        while (!stack.isEmpty()) {
            File file = stack.pop();
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        stack.push(f);
                    }
                }
            } else if (file.isFile() && file.getName().endsWith(".java")) {
                String source;
                try {
                    source = StaticAnalysisDemo.read(file.getAbsolutePath());
                } catch (IOException e) {
                    System.err.println(e);
                    continue;
                }
                ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
                parser.setSource(new Document(source).get().toCharArray());
                CompilationUnit cu = (CompilationUnit) parser.createAST(null);
                cu.accept(new Visitor(cu, file.getAbsolutePath()));
            }
        }
    }

    static class Visitor extends ASTVisitor {
        private CompilationUnit cu;
        private String filePath;

        public Visitor(CompilationUnit cu, String filePath) {
            this.cu = cu;
            this.filePath = filePath;
        }

        @Override
        public boolean visit(TryStatement node) {
            ASTNode parent = node.getParent();
            while (parent != null) {
                if (parent.getNodeType() == ASTNode.TRY_STATEMENT) {
                    System.out.println("Nested try detected at line " + cu.getLineNumber(node.getStartPosition()) +
                            " in file " + filePath);
                    System.out.println(node);
                    break;
                }
                parent = parent.getParent();
            }
            return super.visit(node);
        }
    }
}
