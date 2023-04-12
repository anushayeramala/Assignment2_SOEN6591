package ca.concordia.soen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    public static void just_throw() throws Exception{
		// TODO Auto-generated method stub
		  
		    	System.out.println("Inside just throw");
		    	try {
		            int a = 4, b = 0;
		            throw new ArithmeticException();
		         } catch(ArithmeticException ae) {
		           throw new Exception();
		         }	
		    
	}
  
  public static void just_log() {
	     Logger logger = Logger.getLogger(LogandThrowVisitor.class.getName());
	  try
	  {
		  throw new NoSuchMethodException();
	  }
	  catch (NoSuchMethodException e) {
		  System.out.println("just log");
		  
		  logger.log(Level.SEVERE, "An error occurred", e);
		  //throw e;
		  }
}
  
  public static void log_throw() throws NoSuchMethodException{
	     Logger logger = Logger.getLogger(LogandThrowVisitor.class.getName());
	  try
	  {
		  throw new NoSuchMethodException();
	  }
	  catch (NoSuchMethodException e) {
		  System.out.println("log");
		  
		  logger.log(Level.SEVERE, "An error occurred", e);
		  throw e;
		  }
}
  public static void log_throw1() throws NoSuchMethodException, Exception{
	     Logger logger = Logger.getLogger(LogandThrowVisitor.class.getName());
	  try
	  {
		  throw new NoSuchMethodException();
	  }
	  catch (NoSuchMethodException e) {
		  System.out.println("1st log");
		  
		  logger.log(Level.SEVERE, "An error occurred", e);
		  throw new Exception();
		  }
  }
  
  public static void log_throw2() throws NoSuchMethodException, Exception{
	     Logger logger = Logger.getLogger(LogandThrowVisitor.class.getName());
	  try
	  {
		  throw new NoSuchMethodException();
	  }
	  catch (NoSuchMethodException e) {
		  System.out.println("2nd log");
		  
		  e.printStackTrace();
		  throw new Exception();
		  }
}
    public static String read(String filename) throws IOException {
        Path path = Paths.get(filename);

        String source = Files.lines(path).collect(Collectors.joining("\n"));

        return source;
      }
}
class MyException extends Exception {
    public MyException(String message, Throwable cause) {
        super(message, cause);
    }
}
