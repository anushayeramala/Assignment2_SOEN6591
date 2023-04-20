package ca.concordia.soen;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jface.text.Document;

class NestedTryVisitor extends ASTVisitor {
	static int try_count;
    private CompilationUnit cu;
    private String filePath;

    public NestedTryVisitor(CompilationUnit cu, String filePath) {
        this.cu = cu;
        this.filePath = filePath;
    }

    @Override
    public boolean visit(TryStatement node) {
        ASTNode parent = node.getParent();
        while (parent != null) {
            if (parent.getNodeType() == ASTNode.TRY_STATEMENT) {
                System.out.println("Nested try detected at line " + cu.getLineNumber(node.getStartPosition()) +
                        " in file " + filePath);
                System.out.println(node);
                try_count++;
                break;
                
            }
            parent = parent.getParent();
        }
        return super.visit(node);
    }
}
