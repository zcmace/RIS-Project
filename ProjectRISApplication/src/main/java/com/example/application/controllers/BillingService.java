package com.example.application.controllers;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class BillingService {

    private JavaMailSender javaMailSender;

    @Autowired
    public BillingService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void send(SimpleMailMessage mail) {
        javaMailSender.send(mail);
    }
}
