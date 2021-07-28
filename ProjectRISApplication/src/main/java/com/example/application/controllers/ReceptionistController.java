package com.example.application.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;



import org.springframework.web.bind.annotation.ModelAttribute;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailSendException;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.example.application.persistence.Appointment;
import com.example.application.repositories.AppointmentRepository;
import com.example.application.repositories.ModalityRepository;
import com.example.application.repositories.OrderRepository;
import com.example.application.repositories.UserRepository;
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

    @Autowired UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private BillingService billingService;

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
        
        Appointment thisAppointment = null;

        /*
        loop through all appointments to match appointment from model to appointment java object
        This is because the appointment object passed from the model contains just the id
        */

        Iterable<Appointment> allAppointments = appointmentRepository.findAll();
        for (Appointment find_appointment : allAppointments){
            if(find_appointment.getId() == appointment.getId()){
                thisAppointment = find_appointment;
            }
        }
        
        //getting the date and time in a better format
    
      
        
        try{
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            Date date = formatter.parse(thisAppointment.getDatetime());
            
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(date);
            Locale locale = Locale.getDefault();
            

            Iterable<Modality> modalList = modalityRepository.findAll();

            Modality appModality = new Modality();
            for (Modality modality : modalList){
                if (modality.getId() == thisAppointment.getModality()){
                    appModality = modality;
                }
            }
        
        SimpleMailMessage message = new SimpleMailMessage(); 
        message.setFrom("radiologyinfosystem@gmail.com");
        message.setTo(thisAppointment.getEmailaddress()); 
        message.setSubject("Radiology Billing Statement: " + thisAppointment.getDatetime()); 
        message.setText(
            "Thank you for choosing our Radiology team! We hope you enjoyed your visit, here is a summary of your recent visit: \n" +
            "Appointment Date: " + cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale) + ", " + cal.getDisplayName(Calendar.MONTH, Calendar.LONG, locale) + " " + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR) + "\n" +
            "Appointment Time: " + cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE) + " " + cal.getDisplayName(Calendar.AM_PM, Calendar.LONG, locale) + "\n" + 
            "Imaging type: " + appModality.getName() + "\n" +
            "Total cost of visit: "  + appModality.getPrice() + "\n\n" +
            "Insurance Info Used: \n" +
            "Enrollee Name: " + thisAppointment.getEnrolleename() + "\n" +
            "Enrollee ID: " + thisAppointment.getEnrolleeid() + "\n" + 
            "Issuer: " + thisAppointment.getIssuer()
        );
        billingService.send(message);
        } catch (MailSendException exception) {
            System.out.println(exception.getMessage());
        } catch(ParseException e){
            System.out.println(e.getMessage());
        }
        appointmentRepository.setCheckedInForAppointment(appointment.getId());
        return "redirect:/home";
    }
}