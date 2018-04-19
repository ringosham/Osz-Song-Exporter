package com.ringosham.controllers;

import javafx.scene.control.TextArea;

import java.io.OutputStream;

public class Console extends OutputStream {
    private TextArea console;

    Console(TextArea console) {
        this.console = console;
    }

    @Override
    public void write(int b) {
        console.appendText(String.valueOf((char) b));
    }
}
