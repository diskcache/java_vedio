package com.example.service;

import java.io.IOException;

public interface VedioService {
    public void merge(String filename, Long total) throws IOException;

    public String shot(String filename) throws IOException, InterruptedException;

    public void clean(String filename, Long total) throws IOException;

    public String process(String filname) throws IOException, InterruptedException;
}
