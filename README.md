# Assignment2_SOEN6591

Nested_Try Anti-pattern detection

Code Description: 

The code is written in Java and consists of a single main class NestedTryVisitor and an inner class Visitor that extends the ASTVisitor class.

The purpose of this code is to detect the occurrence of a nested try block in a Java codebase. It takes in a directory path as an input, traverses through all the .java files in the directory (and its subdirectories) and detects the occurrence of the nested try block in each file.

The main method first checks if a directory path is provided as input. If not, it prints an error message and returns. If a valid path is provided, the code creates a Stack object and pushes the initial directory into it. It then starts a loop that pops a directory from the stack, checks if it is a directory or a .java file. If it is a directory, it pushes all its subdirectories and files into the stack. If it is a .java file, it reads its content, creates an AST parser, and creates a compilation unit for the file.

The Visitor class is responsible for traversing the AST of each .java file and detecting the occurrence of a nested try block. The Visitor class overrides the visit(TryStatement node) method of the ASTVisitor class. In this method, it checks if the parent node of the TryStatement node is also a TryStatement node. If it is, then it means that the current try block is nested inside another try block. The visitor class then prints the message indicating the occurrence of the nested try block along with the file name and line number where it is found.

Finally, the main method calls the Visitor class for each file, and the output is printed on the console.
