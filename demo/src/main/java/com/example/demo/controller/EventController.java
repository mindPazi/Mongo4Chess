package com.example.demo.controller;

import com.example.demo.model.EventMongoDB;
import com.example.demo.model.EventNeo4j;
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
@Tag(name = "Event Controller", description = "Gestione eventi su MongoDB e Neo4j")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/mongo")
    @Operation(summary = "Ottieni tutti gli eventi da MongoDB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public List<EventMongoDB> getAllEventsMongo() {
        return eventService.getAllEventsFromMongo();
    }

    @PostMapping("/mongo")
    @Operation(summary = "Aggiungi un nuovo evento su MongoDB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Evento creato con successo"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    })
    public EventMongoDB createEventMongo(@RequestBody EventMongoDB event) {
        return eventService.addEventToMongo(event);
    }

    @PutMapping("/mongo/{id}")
    @Operation(summary = "Aggiorna un evento su MongoDB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento aggiornato"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    public EventMongoDB updateEventMongo(@PathVariable String id, @RequestBody EventMongoDB updatedEvent) {
        return eventService.updateEventInMongo(id, updatedEvent);
    }

    @DeleteMapping("/mongo/{id}")
    @Operation(summary = "Elimina un evento da MongoDB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Evento eliminato con successo"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    public void deleteEventMongo(@PathVariable String id) {
        eventService.deleteEventFromMongo(id);
    }

    @GetMapping("/neo4j")
    @Operation(summary = "Ottieni tutti gli eventi da Neo4j")
    public List<EventNeo4j> getAllEventsNeo4j() {
        return eventService.getAllEventsFromNeo4j();
    }

    @PostMapping("/neo4j")
    @Operation(summary = "Aggiungi un nuovo evento su Neo4j")
    public EventNeo4j createEventNeo4j(@RequestBody EventNeo4j event) {
        return eventService.addEventToNeo4j(event);
    }

    @PutMapping("/neo4j/{id}")
    @Operation(summary = "Aggiorna un evento su Neo4j")
    public EventNeo4j updateEventNeo4j(@PathVariable Long id, @RequestBody EventNeo4j updatedEvent) {
        return eventService.updateEventInNeo4j(id, updatedEvent);
    }

    @DeleteMapping("/neo4j/{id}")
    @Operation(summary = "Elimina un evento da Neo4j")
    public void deleteEventNeo4j(@PathVariable Long id) {
        eventService.deleteEventFromNeo4j(id);
    }
}
