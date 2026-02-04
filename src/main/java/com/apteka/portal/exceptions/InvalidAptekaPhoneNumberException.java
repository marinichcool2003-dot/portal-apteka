package com.apteka.portal.exceptions;

public class InvalidAptekaPhoneNumberException extends RuntimeException{
    public InvalidAptekaPhoneNumberException(){
        super("Некорректно введён ноиер телефона. Необходимо ввести номер в формате \"9ХХХХХХХХХ\"");
    }
}
