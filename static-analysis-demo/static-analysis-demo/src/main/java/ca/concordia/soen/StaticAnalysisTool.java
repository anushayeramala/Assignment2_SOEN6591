package ca.concordia.soen;

import java.io.IOException;

public class StaticAnalysisTool {
	
	public static double destructive_divide(double dividend, double divisor) throws IOException {
	    try {
	        return dividend / divisor;
	    } catch (ArithmeticException e) {
	       throw new RuntimeException("An error occurred", e);
	    }
	}
     
	 public void doSomething() throws MyException {
	        try {
	            throw new IOException("Something went wrong");
	        } catch (IOException e) {
	            throw new MyException("An error occurred", e);
	        }
	    }
    public void foo() {
        try {
            bar();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void bar() throws Exception {
        try {
        } catch (Exception ex) {
            throw ex;
        }
    }
    
    public void over_catch() throws ArithmeticException {
    	System.out.println("Inside over catch");
    	try {
            int a = 4, b = 0;
         } 
    	catch(ArithmeticException ae) {
        	 System.out.println("Arithmetic error");
         }	
    	catch(Exception ae) {
       	 System.out.println("Generic error");
        }	
    }
    
    public void nestedTryAntiPattern() {
        try {
               int x = 10 / 0; // This will throw an ArithmeticException
            try {
                String str = null;
                int length = str.length(); // This will throw a NullPointerException
            } catch (NullPointerException e2) {
                System.out.println("Caught NullPointerException: " + e2.getMessage());
            }
        } catch (ArithmeticException e1) {
            System.out.println("Caught ArithmeticException: " + e1.getMessage());
        }
    }

}
