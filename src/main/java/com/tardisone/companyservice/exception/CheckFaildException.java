package com.tardisone.companyservice.exception;

public class CheckFaildException extends RuntimeException {

    public CheckFaildException(String fieldName){
        super("Illegal " + fieldName);
    }

    public CheckFaildException(){
        super("Illegal field");
    }

}
