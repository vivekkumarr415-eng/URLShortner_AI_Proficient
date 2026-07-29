package com.example.urlshortener.service;
import com.example.urlshortener.entity.WorkflowContextEntry;
import com.example.urlshortener.entity.WorkflowExecution;
import com.example.urlshortener.repository.WorkflowContextRepository;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.stream.Collectors;
@Service public class WorkflowContextStore { private final WorkflowContextRepository repository; public WorkflowContextStore(WorkflowContextRepository repository){this.repository=repository;} public void putAll(WorkflowExecution workflow, Map<String,String> values){if(values!=null) values.forEach((key,value)->repository.save(new WorkflowContextEntry(workflow,key,value)));} public Map<String,String> getAll(WorkflowExecution workflow){return repository.findByWorkflowExecutionId(workflow.getId()).stream().collect(Collectors.toMap(WorkflowContextEntry::getKey,WorkflowContextEntry::getValue));} }
