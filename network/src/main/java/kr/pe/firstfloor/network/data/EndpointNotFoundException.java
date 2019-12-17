package kr.pe.firstfloor.network.data;

public class EndpointNotFoundException extends Exception {
    public EndpointNotFoundException() { super("Endpoint not found"); }
}
