package ca.concordia.soen;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;

public class ThrowsKitchenSinkFinder {
	public static void main(String[] args) { 
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		
	    for (String filename : args) {
	    	String source;
	        try {
	          source = StaticAnalysisDemo.read(filename);
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
		public boolean visit(MethodDeclaration methodDeclaration) {
			List<Type> list = methodDeclaration.thrownExceptionTypes();
			for (Type exceptionType: list) {
				System.out.println(exceptionType);
			}
			if (list.size() > 1) {
				//check if the exception are used in a case by case basis
			}

			return true;
		}
	}
}
