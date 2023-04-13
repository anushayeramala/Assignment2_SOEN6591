package ca.concordia.soen;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class ThrowsKitchenSinkFinder {
	public static void main(String[] args) { 
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		
	    for (String filename : args) {
	    	String source;
	        try {
	        	List<Path> fileList = getFilesFromDir(filename);
	        	for (int i = 0; i < fileList.size(); i++) {
	        		Path path = fileList.get(i);
	        		source = read(path);
	        		parser.setSource(source.toCharArray());
	        		ASTNode root = parser.createAST(null);
	        		root.accept(new Visitor(path));
	        	}
	        } catch (IOException e) {
	        	System.err.println(e);
	        	continue;
	        }
	    }
		
	}
	
	public static List<Path> getFilesFromDir(String dirName) throws IOException {
		List<Path> fileList = new ArrayList<>();
		Path path1 = Paths.get(dirName);
		DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dirName));
		for (Path path: stream) {
			if (Files.isDirectory(path)) {
				fileList.addAll(getFilesFromDir(path.toString()));
			}
			else if (getExtension(path.getFileName().toString()).equals("java")) {
				fileList.add(path);
			}
		}
		return fileList;
	}
	
	public static String getExtension(String fileName) {
		int index = fileName.lastIndexOf('.');
		String extension = "";
		if (index > 0) extension = fileName.substring(index+1);
		return extension;
	}
	
	public static String read(Path path) throws IOException {
		String source = "";
		
		try {
	   
			source = Files.lines(path).collect(Collectors.joining("\n"));
		}
		catch (Exception e) {
			System.err.println(e);
		}
	
	    return source;
	}
	
	static class Visitor extends ASTVisitor {
		Path path;
		
		public Visitor(Path path) {
			this.path = path;
		}
		
		@Override
		public boolean visit(MethodDeclaration methodDeclaration) {
			List<Type> throwDecList = methodDeclaration.thrownExceptionTypes();
			if (throwDecList.size() > 1) {
				HashSet<String> throwDecSet = new HashSet<String>();
				for (Type exceptionType: throwDecList) {
					throwDecSet.add(exceptionType.toString());
				}
				Block block = methodDeclaration.getBody();
				
				if (block != null) {

					List<Statement> statements = block.statements();
	
					for (int i = 0; i < statements.size(); i++) {
						if (statements.get(i).getNodeType() == Statement.IF_STATEMENT) {
							IfStatement ifStatement = (IfStatement) statements.get(i);
							Statement thenStatement = ifStatement.getThenStatement();
							Statement elseStatement = ifStatement.getElseStatement();
							Visitor.findThrows(elseStatement, throwDecSet);
							Visitor.findThrows(thenStatement, throwDecSet);
						
						}
						else if (statements.get(i).getNodeType() == Statement.SWITCH_STATEMENT) {
							SwitchStatement switchStatement = (SwitchStatement) statements.get(i);
							List<Statement> list = switchStatement.statements();
							for (int j = 0; j < list.size(); j++) {
								Visitor.findThrows(list.get(j), throwDecSet);
							}
						}
					}
					
					if (!throwDecSet.isEmpty()) {
						CompilationUnit cu = ((CompilationUnit) methodDeclaration.getRoot());
						int lineNum = cu.getLineNumber(methodDeclaration.getStartPosition());
						System.out.println("Throw Kitchen Sink Found at: File Path: "+this.path+" Line: "+lineNum+" Method name: "+methodDeclaration.getName());
					}
				}
			}
			
			return true;
		}
		public static void findThrows(Statement statement, HashSet<String> throwDecSet) {
			if (statement != null) {
				if (statement.getNodeType() == Statement.VARIABLE_DECLARATION_STATEMENT) {
					VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement;
					
					if (throwDecSet.contains(variableDeclarationStatement.getType().toString())) {
						throwDecSet.remove(variableDeclarationStatement.getType().toString());
					}
				}
				else if (statement.getNodeType() == Statement.THROW_STATEMENT) {
					ThrowStatement thenThrow = (ThrowStatement) statement;
					Expression throwExpression = thenThrow.getExpression();
					if (throwExpression.getNodeType() == Expression.CLASS_INSTANCE_CREATION) {
						ClassInstanceCreation variableDeclarationExpression = (ClassInstanceCreation) throwExpression;
						if (throwDecSet.contains(variableDeclarationExpression.getType().toString())) {
							throwDecSet.remove(variableDeclarationExpression.getType().toString());
						}
					}
					
				}
				else if (statement.getNodeType() == Statement.BLOCK) {
					Block block = (Block) statement;
					List<Statement> statements = block.statements();
					for (int i = 0; i < statements.size(); i++) {
						Visitor.findThrows(statements.get(i), throwDecSet);
					}
				}
			}
		}
	}
}
