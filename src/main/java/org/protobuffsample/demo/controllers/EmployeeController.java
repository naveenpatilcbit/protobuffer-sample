package org.protobuffsample.demo.controllers;

import org.protobuffsample.demo.pojo.Employee;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class EmployeeController {



    @GetMapping(value = "/employees",produces = "application/x-protobuf")
    public ArrayList<Employee> getEmployees(){
        return new ArrayList<Employee>();
    }

}
