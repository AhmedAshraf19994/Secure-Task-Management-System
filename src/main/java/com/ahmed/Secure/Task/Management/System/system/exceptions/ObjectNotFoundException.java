package com.ahmed.Secure.Task.Management.System.system.exceptions;

public class ObjectNotFoundException extends RuntimeException{

    public ObjectNotFoundException(String objectName, int objectId) {
        super("could not find " + objectName + " with id: " + objectId);
    }
}
