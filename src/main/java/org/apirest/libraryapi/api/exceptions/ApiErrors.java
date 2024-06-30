package org.apirest.libraryapi.api.exceptions;

import org.apirest.libraryapi.exception.BusinessException;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiErrors {

    private List<String> errors;

    public ApiErrors(BindingResult bindingResult){
        this.errors = new ArrayList<>();
        bindingResult.getAllErrors().forEach(error -> this.errors.add(error.getDefaultMessage()));
    }

    public List<String> getErrors(){
        return errors;
    }

    public ApiErrors(BusinessException e){
        this.errors = Arrays.asList(e.getMessage());
    }
}
