package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.RecommendationRequests;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestsRepository;

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
import org.springframework.web.bind.annotation.PathVariable;

import javax.validation.Valid;

import java.time.LocalDateTime;

@Tag(name = "RecommendationRequests")
@RequestMapping("/api/recommendationrequests")
@RestController
@Slf4j
public class RecommendationRequestsController extends ApiController {

    @Autowired
    RecommendationRequestsRepository recReqRep;

    @Operation(summary = "Get all recommendation requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<RecommendationRequests> allRequests() {
        Iterable<RecommendationRequests> recommendationRequests = recReqRep.findAll();
        return recommendationRequests;
    }

    @Operation(summary = "Create a single new recommendation request")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@PostMapping("/post")
public RecommendationRequests postRequest(
        @Parameter(description="requesterEmail") @RequestParam String requesterEmail,
        @Parameter(description="professorEmail") @RequestParam String professorEmail,
        @Parameter(description="Explanation") @RequestParam String Explanation,
        @Parameter(description="dateRequested") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateRequested,
        @Parameter(description="dateNeeded") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateNeeded,
        @Parameter(description="done") @RequestParam boolean done) {

    RecommendationRequests request = new RecommendationRequests();
    request.setRequesterEmail(requesterEmail);
    request.setProfessorEmail(professorEmail);
    request.setExplanation(Explanation);
    request.setDateRequested(dateRequested);
    request.setDateNeeded(dateNeeded);
    request.setDone(done);

    RecommendationRequests savedRequest = recReqRep.save(request);
    return savedRequest;
}


   @Operation(summary = "Get one single recommendation request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public RecommendationRequests getById(
        @Parameter(name="id") @RequestParam Long id) { 
    RecommendationRequests recommendationRequests = recReqRep.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequests.class, id));

            return recommendationRequests;
    } 
    @Operation(summary = "Delete a single recommendation request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteRequest(@Parameter(name="id") @RequestParam Long id) {
        RecommendationRequests request = recReqRep.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequests.class, id));
                recReqRep.delete(request);
        return genericMessage("RecommendationRequests with id %s deleted".formatted(id));
    }

    @Operation(summary = "Update one single recommendation request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public RecommendationRequests updateRequest(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid RecommendationRequests incoming) {

        RecommendationRequests request = recReqRep.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequests.class, id));

        request.setRequesterEmail(incoming.getRequesterEmail());
        request.setProfessorEmail(incoming.getProfessorEmail());
        request.setExplanation(incoming.getExplanation());
        request.setDateRequested(incoming.getDateRequested());
        request.setDateNeeded(incoming.getDateNeeded());
        request.setDone(incoming.getDone());

        recReqRep.save(request);
        return request;
    }

}