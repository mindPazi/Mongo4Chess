@RestController
@RequestMapping("/api/events")
@Tag(name = "Event Controller", description = "Gestione eventi")
public class EventController {

    @GetMapping
    @Operation(summary = "Ottieni tutti gli eventi")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

}
