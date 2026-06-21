package com.ahmed.Secure.Task.Management.System.system.exceptions;

import java.util.UUID;

public class ObjectNotFoundException extends RuntimeException{

    public ObjectNotFoundException(String objectName, int objectId) {
        super("could not find " + objectName + " with id: " + objectId);
    }
    public ObjectNotFoundException(String objectName, UUID objectId) {
        super("could not find " + objectName + " with id: " + objectId);
    }
}
