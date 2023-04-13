package ca.concordia.soen;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
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
			if (!throwDecList.isEmpty()) {
				HashSet<String> throwDecSet = new HashSet<String>();
				for (Type exceptionType: throwDecList) {
					throwDecSet.add(exceptionType.toString());
					System.out.println(exceptionType);
				}
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
//							System.out.println(ifStatement.getThenStatement());
//							System.out.println(ifStatement.getElseStatement());
							Statement thenStatement = ifStatement.getThenStatement();
							Statement elseStatement = ifStatement.getElseStatement();
//							System.out.println(Statement.BLOCK);
//							System.out.println(thenStatement.getNodeType());
//							System.out.println(elseStatement.getNodeType());
							
							Visitor.findThrows(elseStatement, throwDecSet);
							Visitor.findThrows(thenStatement, throwDecSet);
						
						}
						else if (statements.get(i).getNodeType() == Statement.SWITCH_STATEMENT) {
							SwitchStatement switchStatement = (SwitchStatement) statements.get(i);
						}
					}
				}
				
				Iterator value = throwDecSet.iterator();
				while (value.hasNext()) {
					System.out.println("At the end");
					System.out.println(value.next());
				}
				
				if (!throwDecSet.isEmpty()) {
					//print line number & method
					System.out.println("We found it");
					System.out.println(methodDeclaration.getName());
					CompilationUnit cu = ((CompilationUnit) methodDeclaration.getRoot());
					int lineNum = cu.getLineNumber(methodDeclaration.getStartPosition());
					System.out.println("Throw Kitchen Sink Found at: Line: "+lineNum+" Method name: "+methodDeclaration.getName());
				}
			}
			
			return true;
		}
		public static void findThrows(Statement statement, HashSet<String> throwDecSet) {
			if (statement.getNodeType() == Statement.VARIABLE_DECLARATION_STATEMENT) {
				VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement;
				//System.out.println("Statemennttt "+variableDeclarationStatement.getType());
//				Iterator value = throwDecSet.iterator();
//				while (value.hasNext()) {
//					String type = (String) value.next();
//					System.out.println("In iterator: "+variableDeclarationStatement.getType().toString().equals(type.toString()));
//					System.out.println(variableDeclarationStatement.getType());
//					System.out.println(type.toString());
//					System.out.println(variableDeclarationStatement.getType().getClass().toString());
//					System.out.println(type.getClass());
//				}
				
				if (throwDecSet.contains(variableDeclarationStatement.getType().toString())) {
//					System.out.println("Here1");
					throwDecSet.remove(variableDeclarationStatement.getType().toString());
				}
			}
			else if (statement.getNodeType() == Statement.THROW_STATEMENT) {
				ThrowStatement thenThrow = (ThrowStatement) statement;
//				System.out.println("thenThrow: "+thenThrow);
//				System.out.println("thenThrow.getExpression(): "+thenThrow.getExpression());
				Expression throwExpression = thenThrow.getExpression();
//				System.out.println("throwExpression.getNodeType(): "+throwExpression.getNodeType());
//				System.out.println(Expression.CLASS_INSTANCE_CREATION);
//				System.out.println(Expression.VARIABLE_DECLARATION_STATEMENT);
//				System.out.println(throwExpression.getParent());
				if (throwExpression.getNodeType() == Expression.CLASS_INSTANCE_CREATION) {
					ClassInstanceCreation variableDeclarationExpression = (ClassInstanceCreation) throwExpression;
//					System.out.println("Expressionnn "+variableDeclarationExpression.getType());
					if (throwDecSet.contains(variableDeclarationExpression.getType().toString())) {
//						System.out.println("Here2");
						throwDecSet.remove(variableDeclarationExpression.getType().toString());
					}
				}
				
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
