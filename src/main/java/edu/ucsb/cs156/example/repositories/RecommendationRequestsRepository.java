package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.RecommendationRequests;

import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RecommendationRequestsRepository extends CrudRepository<RecommendationRequests, Long> {
    Iterable<RecommendationRequests> findByProfessorEmail(String professorEmail);
}