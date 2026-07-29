package com.example.urlshortener.exception; public class WorkflowNotFoundException extends RuntimeException { public WorkflowNotFoundException(String id) { super("Workflow not found: " + id); } }
