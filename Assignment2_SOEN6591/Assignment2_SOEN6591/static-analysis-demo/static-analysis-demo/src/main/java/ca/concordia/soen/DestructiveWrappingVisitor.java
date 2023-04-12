package ca.concordia.soen;

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

//Destructive wrapping detecting class
public class DestructiveWrappingVisitor {
	
    public static void main(String[] args) {
    	ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
    	parser.setResolveBindings(true);
        for (String filename : args) {
          String source;
          try {
            source = StaticAnalysisTool.read(filename);
          } catch (IOException e) {
            System.err.println(e);
            continue;
          }
      
        parser.setSource(source.toCharArray());
        ASTNode unit = parser.createAST(null);
        unit.accept(new Visitor());
      }
    }
    
    static class Visitor extends ASTVisitor {
    	private String currentMethod;
	   
    	@Override
        public boolean visit(MethodDeclaration node) {
            currentMethod = node.getName().getFullyQualifiedName();
            return true;
        }
    	
    	public boolean visit(CatchClause node) {
    	    String caughtTypeName = node.getException().getType().toString();
    	    caughtTypeName = caughtTypeName.substring(caughtTypeName.lastIndexOf('.') + 1);

    	    List<Statement> statements = node.getBody().statements();
    	    for (Statement statement : statements) {
    	        if (statement instanceof ThrowStatement) {
    	            Expression expression = ((ThrowStatement) statement).getExpression();
    	            String thrownTypeName = expression.toString();
    	            if (thrownTypeName.contains("(")) {
    	                thrownTypeName = thrownTypeName.substring(4,thrownTypeName.indexOf("("));
    	            }
    	            //int lineNumber = unit.getLineNumber(statement.getStartPosition());
    	            if(!caughtTypeName.equals(thrownTypeName)) {
    	            System.out.println("Destructive wrapping detected at: ");
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
