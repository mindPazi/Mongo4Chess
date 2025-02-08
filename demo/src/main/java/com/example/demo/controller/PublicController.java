package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.MatchService;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
@Tag(name = "Public", description = "Public Controller")
public class PublicController {

    private final MatchService matchService;

    @GetMapping("/openings/most-played")
    public ResponseEntity<List<Map<String, Object>>> getMostPlayedOpenings(
            @RequestParam int elomin, @RequestParam int elomax) {

        List<Document> documents = matchService.getMostPlayedOpenings(elomin, elomax);

        if (documents == null || documents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<Map<String, Object>> mostPlayedOpenings = documents.stream()
                .map(Document::entrySet)
                .map(entrySet -> entrySet.stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(mostPlayedOpenings);
    }

    @GetMapping("/matches/win-stats")
    public ResponseEntity<Integer> getNumOfWinsAndDrawsPerElo(
            @RequestParam int elomin, @RequestParam int elomax) {

        int result = matchService.getNumOfWinsAndDrawsPerElo(elomin, elomax);
        return ResponseEntity.ok(result); // Ora il tipo è Integer, non int
    }

}
