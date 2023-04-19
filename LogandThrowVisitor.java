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


public class LogandThrowVisitor {
	public static void main(String[] args) {

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
                ASTParser parser = ASTParser.newParser(AST.getJLSLatest());

                try {
                	Path path = Paths.get(file.getName());
                    source = Files.lines(path).collect(Collectors.joining("\n"));
                } catch (IOException e) {
                    System.err.println(e);
                    continue;
                }

                parser.setSource(new Document(source).get().toCharArray());
                CompilationUnit cu = (CompilationUnit) parser.createAST(null);
                cu.accept(new Visitor(cu, file.getAbsolutePath()));
            }
        }
    }
  static class Visitor extends ASTVisitor {
	  private String filename;
	  private CompilationUnit cu;
	  public Visitor(String filename) {
	      this.filename = filename;
	    }
	  public Visitor()
	  {
		  
	  }
	  
	  public Visitor(CompilationUnit cu, String filename) {
          this.cu = cu;
          this.filename = filename;
      }
	  @Override
	  
	  public boolean visit(CatchClause clause) {
	    boolean hasLogging = false;
	    boolean hasThrowing = false;
	    int lineNumber = ((CompilationUnit) clause.getRoot()).getLineNumber(clause.getStartPosition());
	    for (Object statement : clause.getBody().statements()) {
	      if (statement instanceof ExpressionStatement) {
	        ExpressionStatement exprStmt = (ExpressionStatement) statement;
	        if (exprStmt.getExpression() instanceof MethodInvocation) {
	          MethodInvocation methodInvocation = (MethodInvocation) exprStmt.getExpression();
	          if (methodInvocation.getName().getIdentifier().equals("log") || 
	        		  methodInvocation.getName().getIdentifier().equals("logp") || 
	        		  methodInvocation.getName().getIdentifier().equals("logrb") || 
	        		  methodInvocation.getName().getIdentifier().equals("error") || 
	        		  methodInvocation.getName().getIdentifier().equals("warning") || 
	        		  methodInvocation.getName().getIdentifier().equals("info") || 
	        		  methodInvocation.getName().getIdentifier().equals("debug") || 
	        		  methodInvocation.getName().getIdentifier().equals("fatal") || 
	        		  methodInvocation.getName().getIdentifier().equals("trace") || 
	        		  methodInvocation.getName().getIdentifier().equals("setLevel") || 
	        	  methodInvocation.getName().getIdentifier().equals("printStackTrace")) {
	            hasLogging = true;
	          }
	        }
	      } else if (statement instanceof ThrowStatement) {
	        hasThrowing = true;
	      }
	    }
	    if (hasLogging && hasThrowing) {
	    	System.out.println("A log and throw anti-pattern is detected in a file, whose file path is: " + filename);
            System.out.println("The anti-pattern is detected in a catch block at this line number: " + lineNumber);
            System.out.println("The catch clause \n" + clause.toString() + " is having the log and throw anti pattern.");
            
            System.out.println();
	    }
	    
	    return true;
	  }
	}
}

