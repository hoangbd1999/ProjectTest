/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.raw.data.exception;

/**
 *
 * @author Admin
 */
public class ResourceNotFoundException extends RuntimeException {

    private String id;
    private String resourceName;

    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(Class clazz, long id) {
        super(clazz.getSimpleName() + " " + id + " not found");
        this.resourceName = clazz.getSimpleName();
        this.id = String.valueOf(id);
    }

    public ResourceNotFoundException(Class clazz, String id) {
        super(clazz.getSimpleName() + " " + id + " not found");
        this.resourceName = clazz.getSimpleName();
        this.id = id;
    }

    public ResourceNotFoundException(String resourceName, long id) {
        super(resourceName + " " + id + " not found");
        this.resourceName = resourceName;
        this.id = String.valueOf(id);
    }

    public ResourceNotFoundException(String resourceName, String id) {
        super(resourceName + " " + id + " not found");
        this.resourceName = resourceName;
        this.id = id;
    }

    public ResourceNotFoundException(long id) {
        super("Object [" + id + "] does not exist.");
    }

    public ResourceNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ResourceNotFoundException(final String message) {
        super(message);
    }

    public ResourceNotFoundException(final Throwable cause) {
        super(cause);
    }

    public String getId() {
        return id == null ? "" : id;
    }

    public String getResourceName() {
        return resourceName == null ? "" : resourceName;
    }
}
