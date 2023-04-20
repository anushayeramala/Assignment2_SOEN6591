package ca.concordia.soen;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Stack;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jface.text.Document;


public class StaticAnalysisTool {
	public static void main(String[] args) {
		
		  int try_count;

        Stack<File> filestack = new Stack<>();
        filestack.push(new File(args[0]));
        while (!filestack.isEmpty()) {
            File file = filestack.pop();
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                    	filestack.push(f);
                    }
                }
            } else if (file.isFile() && file.getName().endsWith(".java")) {
                String source;
                Path path = file.toPath();
                ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
                parser.setResolveBindings(true);
                try {
                	path = file.toPath();
                    source = Files.lines(path).collect(Collectors.joining("\n"));
                } catch (IOException e) {
                    System.err.println(e);
                    continue;
                }

                parser.setSource(new Document(source).get().toCharArray());
                CompilationUnit cu = (CompilationUnit) parser.createAST(null);
                cu.accept(new LogandThrowVisitor(cu, file.getAbsolutePath()));               
                cu.accept(new NestedTryVisitor(cu, file.getAbsolutePath()));
                cu.accept(new destructive_Wrapping_Visitor(file.getAbsolutePath()));
                cu.accept(new ThrowsKitchenSinkVisitor(path));
                
                
            }
        }
        
        System.out.println();
        System.out.println("Total Throws Kitchen Sink exceptions found: "+ThrowsKitchenSinkVisitor.try_count);
        System.out.println("Total Log and Throw exceptions found: "+LogandThrowVisitor.try_count);
        System.out.println("Total Destructive Wrapping exceptions found: "+destructive_Wrapping_Visitor.try_count);
        System.out.println("Total NestedTry exceptions found: "+NestedTryVisitor.try_count);
    }
}