package ca.concordia.soen;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

public class LogandThrowVisitor_1 {

	  public static void main(String[] args) {
	    ASTParser parser = ASTParser.newParser(AST.getJLSLatest());

	    String folderName = args[0];
	    File folder = new File(folderName);

	    if (!folder.isDirectory()) {
	      System.err.println(folderName + " is not a directory");
	      return;
	    }

	    File[] files = folder.listFiles(new JavaFileFilter());

	    if (files == null || files.length == 0) {
	      System.err.println("No Java files found in " + folderName);
	      return;
	    }

	    for (File file : files) {
	      String source;
	      try {
	        source = read(file.getAbsolutePath());
	      } catch (IOException e) {
	        System.err.println(e);
	        continue;
	      }

	      parser.setSource(source.toCharArray());

	      ASTNode root = parser.createAST(null);

	      //root.accept(new Visitor());
	      Visitor visitor = new Visitor(file.getAbsolutePath());
	      root.accept(visitor);
	    }
	  }

	  public static String read(String filename) throws IOException {
	    Path path = Paths.get(filename);

	    String source = Files.lines(path).collect(Collectors.joining("\n"));

	    return source;
	  }

	  private static class JavaFileFilter implements FileFilter {
	    @Override
	    public boolean accept(File pathname) {
	      return pathname.getName().endsWith(".java");
	    }
	  }

  static class Visitor extends ASTVisitor {
	  private String filename;
	  public Visitor(String filename) {
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
	        		  methodInvocation.getName().getIdentifier().equals("config") || 
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

