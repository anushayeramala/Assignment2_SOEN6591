package ca.concordia.soen;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;



 class LogandThrowVisitor extends ASTVisitor {
	  private String filename;
	  private CompilationUnit cu;
	  static int try_count;
	  public LogandThrowVisitor()
	  {
		  
	  }
	  
	  public LogandThrowVisitor(CompilationUnit cu, String filename) {
        this.cu = cu;
        this.filename = filename;
    }
	  
	  public boolean visit(MethodInvocation methodInvocation) {
		    
		    return true; 
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
	    	try_count++;
	    	System.out.println("A log and throw anti-pattern is detected in a file, whose file path is: " + filename);
          System.out.println("The anti-pattern is detected in a catch block at this line number: " + lineNumber);
          System.out.println("The catch clause \n" + clause.toString() + " is having the log and throw anti pattern.");
          
          System.out.println();
          
          //System.out.println("Total Number of over-catch try: "+LogandThrowVisitor.try_count);
	    }
	    
	    return true;
	  }
	}