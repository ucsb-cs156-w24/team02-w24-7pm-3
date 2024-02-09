package edu.ucsb.cs156.example.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "ucsborganizations")
public class UCSBOrganizations {
    @Id

    String orgCode;
    String orgTranslationShort;
    String orgTranslation;
    boolean inactive;

}
