package com.ecommerce.api_geek_store.exception;

//Excepcion personalizada, se lanza cuando no encuentra nada
//Extiende de RunTimeException
public class ResourceNotFoundException extends RuntimeException {
    //Llama al constructor de RunTimeException
    public ResourceNotFoundException(String message){
        super(message);
    }
}


