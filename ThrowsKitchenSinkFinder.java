package ca.concordia.soen;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

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
			List<Type> throwDecList = methodDeclaration.thrownExceptionTypes();
			for (Type exceptionType: throwDecList) {
				System.out.println(exceptionType);
			}
			HashSet<Type> throwDecSet = new HashSet<Type>(throwDecList);
			if (throwDecList.size() > 1) {
				Block block = methodDeclaration.getBody();
				//System.out.println("block "+block);
				//System.out.println(block.statements());
				//System.out.println(block.statements().size());
				List<Statement> statements = block.statements();
				//System.out.println(statements.get(1).getNodeType());
				//System.out.println(Statement.IF_STATEMENT);
				for (int i = 0; i < statements.size(); i++) {
					if (statements.get(i).getNodeType() == Statement.IF_STATEMENT) {
						IfStatement ifStatement = (IfStatement) statements.get(i);
						System.out.println(ifStatement.getThenStatement());
						System.out.println(ifStatement.getElseStatement());
						Statement thenStatement = ifStatement.getThenStatement();
						Statement elseStatement = ifStatement.getElseStatement();
						System.out.println(Statement.BLOCK);
						System.out.println(thenStatement.getNodeType());
						System.out.println(elseStatement.getNodeType());
						
						Visitor.findThrows(elseStatement, throwDecSet);
						Visitor.findThrows(thenStatement, throwDecSet);
					
					}
					else if (statements.get(i).getNodeType() == Statement.SWITCH_STATEMENT) {
						SwitchStatement switchStatement = (SwitchStatement) statements.get(i);
					}
				}
			}
			
			return true;
		}
		public static void findThrows(Statement statement, HashSet<Type> throwDecSet) {
			if (statement.getNodeType() == Statement.VARIABLE_DECLARATION_STATEMENT) {
				VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement;
				
			}
			else if (statement.getNodeType() == Statement.THROW_STATEMENT) {
				ThrowStatement thenThrow = (ThrowStatement) statement;
				System.out.println(thenThrow);
				System.out.println(thenThrow.getExpression());
			}
			else if (statement.getNodeType() == Statement.BLOCK) {
				//Check if throw statement exists in block
				Block block = (Block) statement;
				List<Statement> statements = block.statements();
				for (int i = 0; i < statements.size(); i++) {
					Visitor.findThrows(statements.get(i), throwDecSet);
				}
			}
		}
	}
}
