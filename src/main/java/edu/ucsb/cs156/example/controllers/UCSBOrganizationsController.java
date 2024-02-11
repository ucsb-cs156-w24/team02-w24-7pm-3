package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDiningCommons;
import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "UCSBOrganizations")
@RequestMapping("/api/ucsborganizations")
@RestController
@Slf4j
public class UCSBOrganizationsController extends ApiController {
    @Autowired
    UCSBOrganizationsRepository ucsbOrgsRepository;

    @Operation(summary= "List all active ucsb organizations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    @PostMapping("/post")
    public Iterable<UCSBOrganizations> allUCSBOrgs() {
        Iterable<UCSBOrganizations> orgs = ucsbOrgsRepository.findAll();
        return orgs;
    }

    @Operation(summary= "Create a new organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBOrganizations postUCSBOrganization(
            @Parameter(name="orgCode") @RequestParam String orgCode,
            @Parameter(name="orgTranslationShort") @RequestParam String orgTranslationShort,
            @Parameter(name="orgTranslation") @RequestParam String orgTranslation,
            @Parameter(name="inactive") @RequestParam boolean inactive)
            throws JsonProcessingException {

        UCSBOrganizations ucsbOrg = new UCSBOrganizations();
        ucsbOrg.setOrgCode(orgCode);
        ucsbOrg.setOrgTranslationShort(orgTranslationShort);
        ucsbOrg.setOrgTranslation(orgTranslation);
        ucsbOrg.setInactive(inactive);

        UCSBOrganizations savedOrg = ucsbOrgsRepository.save(ucsbOrg);

        return savedOrg;
    }

    @Operation(summary= "Get a single organization")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBOrganizations getByorgCode(
            @Parameter(name="orgCode") @RequestParam String orgCode) {
        UCSBOrganizations ucsbOrg = ucsbOrgsRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganizations.class, orgCode));

        return ucsbOrg;
    }

    @Operation(summary= "Delete a UCSBOrganization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteUCSBOrg(
            @Parameter(name="orgCode") @RequestParam String orgCode) {
        UCSBOrganizations ucsbOrg = ucsbOrgsRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganizations.class, orgCode));

        ucsbOrgsRepository.delete(ucsbOrg);
        return genericMessage("UCSBOrg with orgCode %s deleted".formatted(orgCode));
    }

    @Operation(summary= "Update organization status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBOrganizations updateUCSBOrgStatus(
            @Parameter(name="orgCode") @RequestParam String orgCode,
            @RequestBody @Valid UCSBOrganizations incoming) { 

        UCSBOrganizations ucsbOrg = ucsbOrgsRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganizations.class, orgCode));

        ucsbOrg.setInactive(incoming.getInactive());
        ucsbOrg.setOrgCode(incoming.getOrgCode());
        ucsbOrg.setOrgTranslation(incoming.getOrgTranslation());
        ucsbOrg.setOrgTranslationShort(incoming.getOrgTranslationShort());
        ucsbOrgsRepository.save(ucsbOrg);

        return ucsbOrg;
    }


}

