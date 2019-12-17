package kr.pe.firstfloor.network.data;

public class UnsupportedInClientException extends Exception {
    public UnsupportedInClientException() { super("This function is not supported in client-side!"); }
}
