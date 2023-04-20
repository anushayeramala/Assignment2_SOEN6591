package ca.concordia.soen;

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

class destructive_Wrapping_Visitor  extends ASTVisitor {
	static int try_count;
    private String currentMethod;
    private String filePath;

    public destructive_Wrapping_Visitor (String filePath) {
        this.filePath = filePath;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        currentMethod = node.getName().getFullyQualifiedName();
        return true;
    }

    public boolean visit(CatchClause node) {
        String caughtTypeName = node.getException().getType().toString();

        List<Statement> statements = node.getBody().statements();
        for (Statement statement : statements) {
            if (statement instanceof ThrowStatement) {
                Expression expression = ((ThrowStatement) statement).getExpression();
                String thrownTypeName = expression.toString();
                if (thrownTypeName.contains("(")) {
                    thrownTypeName = thrownTypeName.substring(4,thrownTypeName.indexOf("("));
                }
                else
                	break;
                if(!caughtTypeName.equals(thrownTypeName)) {
                    System.out.println("Destructive wrapping detected in file: " + filePath);
                    System.out.println("Line number: " + ((CompilationUnit)node.getRoot()).getLineNumber(expression.getStartPosition()));
                    System.out.println("Caught exception type: " + caughtTypeName);    
                    System.out.println("Thrown exception type: " + thrownTypeName);
                    try_count++;
                }
                break;
            }
        }
        return true;
    }

}
