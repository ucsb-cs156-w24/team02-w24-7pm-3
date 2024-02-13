package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.RecommendationRequests;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestsRepository;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.time.LocalDateTime;

@Tag(name = "RecommendationRequests")
@RequestMapping("/api/")
@RestController
@Slf4j
public class RecommendationRequestsController extends ApiController {

    @Autowired
    RecommendationRequestsRepository RecommendationRequestRepository;

    @Operation(summary= "List all Recommendation Requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<RecommendationRequests> allRecommendationRequests() {
        Iterable<RecommendationRequests> requests = RecommendationRequestRepository.findAll();
        return requests;
    }

    @Operation(summary= "Create a new recommendation request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public RecommendationRequests postRecommendationRequest(
        @Parameter(name="requesterEmail") @RequestParam String requesterEmail,
        @Parameter(name="professorEmail") @RequestParam String professorEmail,
        @Parameter(name="explanation") @RequestParam String explanation,
        @Parameter(name="dateRequested") @RequestParam("dateRequested") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateRequested,
        @Parameter(name="dateNeeded") @RequestParam("dateNeeded") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateNeeded,
        @Parameter(name="done") @RequestParam boolean done)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        log.info("dateRequested={}", dateRequested);
        log.info("dateNeeded={}", dateNeeded);

        RecommendationRequests RecommendationRequest = new RecommendationRequests();
        RecommendationRequest.setRequesterEmail(requesterEmail);
        RecommendationRequest.setProfessorEmail(professorEmail);
        RecommendationRequest.setExplanation(explanation);
        RecommendationRequest.setDateRequested(dateRequested);
        RecommendationRequest.setDateNeeded(dateNeeded);
        RecommendationRequest.setDone(done);

        RecommendationRequests savedRecommendationRequest = RecommendationRequestRepository.save(RecommendationRequest);

        return savedRecommendationRequest;
    }

}