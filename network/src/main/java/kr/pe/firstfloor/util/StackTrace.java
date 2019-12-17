package kr.pe.firstfloor.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class StackTrace {
    public static final boolean STACK_TRACE = true;

    public static void print(Object caller, Throwable throwable) {
        if (STACK_TRACE)
            System.err.println("[StackTrace] Exception found!\n - Caller: "+caller.toString()+"\n - Class: "+caller.getClass().getName()+"\nStack Trace:\n"+toString(throwable));
        else
            System.err.println("[StackTrace] Exception found in "+caller.toString()+"("+caller.getClass().getName()+")!");
    }

    public static void print(Class caller, Throwable throwable) {
        if (STACK_TRACE)
            System.err.println("[StackTrace] Exception found!\n - Caller: "+caller.getName()+"\nStack Trace:\n"+toString(throwable));
        else
            System.err.println("[StackTrace] Exception found in "+caller.getName()+"!");
    }

    private static String toString(Throwable throwable) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        throwable.printStackTrace(printStream);

        return outputStream.toString();
    }
}
