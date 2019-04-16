package com.yorma.common.utils.generator.exception;

public class NoSuchElementException extends RuntimeException {
    public NoSuchElementException() {
        super("xml内容中找不到指定的节点");
    }

    public NoSuchElementException(String elementName) {
        super("xml内容中找不到指定的节点:" + elementName);
    }
}
