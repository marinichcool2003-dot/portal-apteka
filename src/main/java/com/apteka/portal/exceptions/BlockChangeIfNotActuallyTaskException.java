package com.apteka.portal.exceptions;

public class BlockChangeIfNotActuallyTaskException extends RuntimeException{
    public BlockChangeIfNotActuallyTaskException() {
        super("Нельзя изменять задачи, которые уже закрыты или отклонены!");
    }
}
