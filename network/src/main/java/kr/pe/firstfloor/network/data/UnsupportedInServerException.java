package kr.pe.firstfloor.network.data;

public class UnsupportedInServerException extends Exception {
    public UnsupportedInServerException() { super("This function is not supported in server-side!"); }
}
