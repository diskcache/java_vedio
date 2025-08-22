package com.example.model;

import lombok.Data;

@Data
public class ProcessMessage {
    private String filename;
    private String hash;
    private Long total;
}
