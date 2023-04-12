package ca.concordia.soen;

import java.io.IOException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;

class LogandThrowVisitor {

  public static void main(String[] args) { 
    ASTParser parser = ASTParser.newParser(AST.getJLSLatest());

    for (String filename : args) {
      String source;
      try {
        source = StaticAnalysisTool.read(filename);
      } catch (IOException e) {
        System.err.println(e);
        continue;
      }

      parser.setSource(source.toCharArray());
    
      ASTNode root = parser.createAST(null);

      root.accept(new Visitor());
    }
  }

  static class Visitor extends ASTVisitor {
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
	    	
            System.out.println("A log and throw anti-pattern is detected in a catch block at this line number: " + lineNumber);
            System.out.println("The catch clause " + clause.toString() + " is having the log and throw anti pattern.");
            System.out.println();
	    }
	    /*if (hasThrowing) {
	      //System.out.println("Catch clause in " + clause.getRoot().toString() + " is throwing an exception.");
	    	System.out.println(clause.toString());
	    }*/
	    return true;
	  }
	}
}

