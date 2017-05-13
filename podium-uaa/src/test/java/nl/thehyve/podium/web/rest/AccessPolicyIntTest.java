/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.UserAuthenticationToken;
import nl.thehyve.podium.common.service.dto.RoleRepresentation;
import nl.thehyve.podium.common.test.AbstractAccessPolicyIntTest;
import nl.thehyve.podium.common.test.Action;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.domain.Role;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.exceptions.UserAccountException;
import nl.thehyve.podium.service.OrganisationService;
import nl.thehyve.podium.service.RoleService;
import nl.thehyve.podium.service.UserService;
import nl.thehyve.podium.service.mapper.RoleMapper;
import nl.thehyve.podium.web.rest.vm.ManagedUserVM;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static nl.thehyve.podium.common.test.Action.format;
import static nl.thehyve.podium.common.test.Action.newAction;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Integration test for the access policy on controller methods.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = PodiumUaaApp.class)
public class AccessPolicyIntTest extends AbstractAccessPolicyIntTest {

    private Logger log = LoggerFactory.getLogger(AccessPolicyIntTest.class);

    @Autowired
    private UserService userService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private WebApplicationContext context;

    @PersistenceContext
    private EntityManager entityManager;

    private MockMvc mockMvc;

    @Override
    protected MockMvc getMockMvc() {
        return mockMvc;
    }

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    private Organisation organisationA;
    private Organisation organisationB;

    private Organisation createOrganisation(String organisationName) {
        Set<RequestType> requestTypes = new HashSet<>();
        requestTypes.add(RequestType.Data);

        Organisation organisation = new Organisation();
        organisation.setName(organisationName);
        organisation.setShortName(organisationName);
        organisation.setRequestTypes(requestTypes);
        organisation = organisationService.save(organisation);
        entityManager.persist(organisation);
        for(Role role: organisation.getRoles()) {
            entityManager.persist(role);
        }
        entityManager.flush();
        return organisation;
    }

    private void createOrganisations() {
        organisationA = createOrganisation("A");
        organisationB = createOrganisation("B");
    }

    private User podiumAdmin;
    private User bbmriAdmin;
    private User adminOrganisationA;
    private User adminOrganisationB;
    private User adminOrganisationAandB;
    private User coordinatorOrganisationA;
    private User coordinatorOrganisationB;
    private User coordinatorOrganisationAandB;
    private User reviewerAandB;
    private User reviewerA;
    private User researcher;
    private User testUser1;
    private User testUser2;
    private User anonymous;
    private Set<AuthenticatedUser> allUsers = new LinkedHashSet<>();

    private User createUser(String name, String authority, Organisation ... organisations) throws UserAccountException {
        log.info("Creating user {}", name);
        ManagedUserVM userVM = new ManagedUserVM();
        userVM.setLogin("test_" + name);
        userVM.setEmail("test_" + name + "@localhost");
        userVM.setFirstName("test_firstname_"+name);
        userVM.setLastName("test_lastname_"+name);
        userVM.setPassword("Password123!");
        User user = userService.createUser(userVM);
        if (organisations.length > 0) {
            for (Organisation organisation: organisations) {
                log.info("Assigning role {} for organisation {}", authority, organisation.getName());
                Role role = roleService.findRoleByOrganisationAndAuthorityName(organisation, authority);
                assert (role != null);
                user.getRoles().add(role);
                user = userService.save(user);
            }
        } else if (authority != null) {
            log.info("Assigning role {}", authority);
            Role role = roleService.findRoleByAuthorityName(authority);
            assert (role != null);
            user.getRoles().add(role);
            user = userService.save(user);
        }
        entityManager.persist(user);
        entityManager.flush();
        {
            log.info("Checking user {}", name);
            // some sanity checks
            User user1 = entityManager.find(User.class, user.getId());
            assert (user1 != null);
            if (authority != null) {
                assert (!user1.getAuthorities().isEmpty());
                UserAuthenticationToken token = new UserAuthenticationToken(user1);
                assert (!token.getAuthorities().isEmpty());
            }
        }
        return user;
    }

    private void createUsers() throws UserAccountException {
        podiumAdmin = createUser("podiumAdmin", AuthorityConstants.PODIUM_ADMIN);
        bbmriAdmin = createUser("bbmriAdmin", AuthorityConstants.BBMRI_ADMIN);
        adminOrganisationA = createUser("adminOrganisationA", AuthorityConstants.ORGANISATION_ADMIN, organisationA);
        adminOrganisationB = createUser("adminOrganisationB", AuthorityConstants.ORGANISATION_ADMIN, organisationB);
        adminOrganisationAandB = createUser("adminOrganisationAandB", AuthorityConstants.ORGANISATION_ADMIN, organisationA, organisationB);
        coordinatorOrganisationA = createUser("coordinatorOrganisationA", AuthorityConstants.ORGANISATION_COORDINATOR, organisationA);
        coordinatorOrganisationB = createUser("coordinatorOrganisationB", AuthorityConstants.ORGANISATION_COORDINATOR, organisationB);
        coordinatorOrganisationAandB= createUser("coordinatorOrganisationAandB", AuthorityConstants.ORGANISATION_COORDINATOR, organisationA, organisationB);
        reviewerAandB = createUser("reviewerAandB", AuthorityConstants.REVIEWER, organisationA, organisationB);
        reviewerA = createUser("reviewerA", AuthorityConstants.REVIEWER, organisationA);
        researcher = createUser("researcher", AuthorityConstants.RESEARCHER);
        testUser1 = createUser("testUser1", AuthorityConstants.RESEARCHER);
        testUser2 = createUser("testUser2", AuthorityConstants.RESEARCHER);
        anonymous = null;
        allUsers.addAll(Arrays.asList(
            podiumAdmin,
            bbmriAdmin,
            adminOrganisationA,
            adminOrganisationB,
            adminOrganisationAandB,
            coordinatorOrganisationA,
            coordinatorOrganisationB,
            coordinatorOrganisationAandB,
            reviewerAandB,
            reviewerA,
            researcher,
            testUser1,
            testUser2,
            anonymous
        ));
    }

    private Role podiumAdminRole;
    private Role bbmriAdminRole;
    private Role researcherRole;
    private Role orgAdminARole;
    private Role orgAdminBRole;
    private Role orgCoordinatorARole;
    private Role orgCoordinatorBRole;
    private Role reviewerARole;
    private Role reviewerBRole;

    private void getRoles() {
        podiumAdminRole = roleService.findRoleByAuthorityName(AuthorityConstants.PODIUM_ADMIN);
        bbmriAdminRole = roleService.findRoleByAuthorityName(AuthorityConstants.BBMRI_ADMIN);
        researcherRole = roleService.findRoleByAuthorityName(AuthorityConstants.RESEARCHER);
        orgAdminARole = roleService.findRoleByOrganisationAndAuthorityName(organisationA, AuthorityConstants.ORGANISATION_ADMIN);
        orgAdminBRole = roleService.findRoleByOrganisationAndAuthorityName(organisationB, AuthorityConstants.ORGANISATION_ADMIN);
        orgCoordinatorARole = roleService.findRoleByOrganisationAndAuthorityName(organisationA, AuthorityConstants.ORGANISATION_COORDINATOR);
        orgCoordinatorBRole = roleService.findRoleByOrganisationAndAuthorityName(organisationB, AuthorityConstants.ORGANISATION_COORDINATOR);
        reviewerARole = roleService.findRoleByOrganisationAndAuthorityName(organisationA, AuthorityConstants.REVIEWER);
        reviewerBRole = roleService.findRoleByOrganisationAndAuthorityName(organisationB, AuthorityConstants.REVIEWER);
    }


    public static final String ROLE_ROUTE = "/api/roles";
    public static final String ROLE_SEARCH_ROUTE = "/api/_search/roles";

    private List<Action> actions = new ArrayList<>();

    private void createActions() {
        // Roles
        // POST /roles. Not allowed!
        actions.add(newAction()
            .setUrl(ROLE_ROUTE).setMethod(HttpMethod.POST)
            .expect(HttpStatus.METHOD_NOT_ALLOWED));
        // DELETE /roles/{id}. Not allowed!
        actions.add(newAction()
            .setUrl(format(ROLE_ROUTE, "/%d", researcherRole.getId()))
            .setMethod(HttpMethod.DELETE)
            .expect(HttpStatus.METHOD_NOT_ALLOWED));
        // PUT /roles (Role role).
        // Edit non-organisation specific role
        RoleRepresentation editedResearcherRole = roleMapper.roleToRoleDTO(researcherRole);
        editedResearcherRole.getUsers().add(bbmriAdmin.getUuid());
        actions.add(newAction()
            .setUrl(ROLE_ROUTE)
            .setMethod(HttpMethod.PUT)
            .body(editedResearcherRole)
            .allow(podiumAdmin, bbmriAdmin));
        // Edit organisation specific role
        RoleRepresentation editedReviewerARole = roleMapper.roleToRoleDTO(reviewerARole);
        editedReviewerARole.getUsers().add(coordinatorOrganisationA.getUuid());
        actions.add(newAction()
            .setUrl(ROLE_ROUTE)
            .setMethod(HttpMethod.PUT)
            .body(editedReviewerARole)
            .allow(podiumAdmin, bbmriAdmin, adminOrganisationA, adminOrganisationAandB));
        // GET /roles
        actions.add(newAction()
            .setUrl(ROLE_ROUTE)
            .allow(podiumAdmin, bbmriAdmin));
        // GET /roles/organisation/{uuid}
        actions.add(newAction()
            .setUrl(format(ROLE_ROUTE, "/organisation/%s", organisationA.getUuid()))
            .allow(podiumAdmin, bbmriAdmin,
                adminOrganisationA, adminOrganisationAandB,
                coordinatorOrganisationA, coordinatorOrganisationAandB,
                reviewerA, reviewerAandB));
        // GET /roles/{id}
        actions.add(newAction()
            .setUrl(format(ROLE_ROUTE, "/%d", reviewerBRole.getId()))
            .allow(podiumAdmin, bbmriAdmin));
        // GET /_search/roles
        actions.add(newAction()
            .setUrl(ROLE_SEARCH_ROUTE)
            .set("query", "admin")
            .allow(podiumAdmin, bbmriAdmin));
    }

    private void setupData() throws UserAccountException {
        createOrganisations();
        createUsers();
        getRoles();
        createActions();
    }

    @Test
    @Transactional
    public void testAccessPolicy() throws Exception {
        setupData();
        runAll(actions, allUsers);
    }

}
