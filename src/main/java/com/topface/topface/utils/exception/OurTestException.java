package com.topface.topface.utils.exception;

public class OurTestException extends Exception {
    public OurTestException(String message) {
        super("Its not crash. For logs only " + message);
    }
}
