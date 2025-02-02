package com.example.demo.service;

import com.example.demo.model.Event;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EventService {

    private final Map<String, Event> events = new HashMap<>();

    public List<Event> getAllEvents() {
        return new ArrayList<>(events.values());
    }

    public Event addEvent(Event event) {
        String id = UUID.randomUUID().toString();
        event.setId(id);
        events.put(id, event);
        return event;
    }

    public Event updateEvent(String id, Event updatedEvent) {
        if (!events.containsKey(id)) {
            throw new NoSuchElementException("Evento non trovato");
        }
        updatedEvent.setId(id);
        events.put(id, updatedEvent);
        return updatedEvent;
    }

    public void deleteEvent(String id) {
        if (!events.containsKey(id)) {
            throw new NoSuchElementException("Evento non trovato");
        }
        events.remove(id);
    }
}
