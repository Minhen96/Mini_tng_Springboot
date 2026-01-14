package com.example.mhpractice.features.notification.service.request;

import lombok.Data;

@Data
public class SendMailRequest {

    private String to;
    private String subject;
    private String body;

}
