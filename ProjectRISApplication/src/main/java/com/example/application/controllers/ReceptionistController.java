package com.example.application.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailSendException;

import com.example.application.persistence.Appointment;
import com.example.application.repositories.AppointmentRepository;
import com.example.application.repositories.ModalityRepository;
import com.example.application.repositories.OrderRepository;
import com.example.application.persistence.Modality;

@Controller 
@RequestMapping(path="/staff") // This means URL's start with /admin (after Application path)
public class ReceptionistController {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private ModalityRepository modalityRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender emailSender;

    @PostMapping("/updateAppointment")
    public String updateAppointment(@ModelAttribute("appointment") Appointment appointment, Model model, BindingResult result)
    {

        appointment.setDatetime(appointment.getDate() + " " + appointment.getTime().substring(0, appointment.getTime().length() - 2));      //  Cut of the -am or -pm

        Appointment newAppointment = appointmentRepository.save(appointment);
        orderRepository.setAppointmentForOrder(newAppointment.getId(), newAppointment.getOrder());

        return "redirect:/home";
    }

    @PostMapping("/checkinAppointment")
    public String checkinAppointment(@ModelAttribute("checkin_appointment") Appointment appointment, Model model, BindingResult result)
    {
        //add method here to send billing statement to patient
       
        try{
            Iterable<Modality> modalList = modalityRepository.findAll();

            Modality appModality = new Modality();
            for (Modality modality : modalList){
                if (modality.getId() == appointment.getModality()){
                    appModality = modality;
                }
            }

        SimpleMailMessage message = new SimpleMailMessage(); 
        message.setFrom("radiologyinfosystem@gmail.com");
        message.setTo(appointment.getEmailaddress()); 
        message.setSubject("Radiology Billing Statement: " + appointment.getDate()); 
        message.setText(
            "Appointment Date: " + appointment.getDate() + "\n" +
            "Appointment Time: " + appointment.getTime() + "\n" +
            "Your " + appModality.getName() + "will cost " 
            + appModality.getPrice() + "\n" +
            "Please send payment to Radiology Office"
        
        );
        emailSender.send(message);
        } catch (MailSendException exception) {
            System.out.println(exception.getMessage());
        }
        appointmentRepository.setCheckedInForAppointment(appointment.getId());
        return "redirect:/home";
    }
}