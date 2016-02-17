package com.topface.topface.utils.exception;

/**
 * Created by tiberal on 17.02.16.
 */
public class OurTestException extends Exception {
    public OurTestException(String message) {
        super("Its not crush. For logs only " + message);
    }
}
