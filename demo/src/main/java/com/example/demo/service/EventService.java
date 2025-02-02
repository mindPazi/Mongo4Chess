package com.example.demo.service;

import com.example.demo.model.Event;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Arrays;

@Service
public class EventService {
    public List<Event> getAllEvents() {
        return Arrays.asList(new Event("Torneo di Scacchi", "2025-06-01"));
    }
}
