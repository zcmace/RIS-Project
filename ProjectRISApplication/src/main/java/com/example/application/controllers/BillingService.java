package com.example.application.controllers;
import com.example.application.persistence.Appointment;
import org.springframework.stereotype.Service;

import java.beans.JavaBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;



@Service
public class BillingService {
    

    private JavaMailSender javaMailSender;



    @Autowired
    public BillingService(JavaMailSender javaMailSender){
        this.javaMailSender = javaMailSender;
    }

    public void send(SimpleMailMessage mail){
        javaMailSender.send(mail);
    }
}
