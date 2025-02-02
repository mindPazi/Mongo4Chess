package com.example.demo.controller;

import com.example.demo.model.Event;
import com.example.demo.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Event Controller", description = "Gestione eventi")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "Ottieni tutti gli eventi")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @PostMapping
    @Operation(summary = "Aggiungi un nuovo evento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Evento creato con successo"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    })
    public Event createEvent(@RequestBody Event event) {
        return eventService.addEvent(event);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna un evento esistente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento aggiornato"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    public Event updateEvent(@PathVariable String id, @RequestBody Event updatedEvent) {
        return eventService.updateEvent(id, updatedEvent);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un evento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Evento eliminato con successo"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    public void deleteEvent(@PathVariable String id) {
        eventService.deleteEvent(id);
    }
}
