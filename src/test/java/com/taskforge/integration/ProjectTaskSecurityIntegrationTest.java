package com.taskforge.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.taskforge.entity.AppUser;
import com.taskforge.entity.Organization;
import com.taskforge.entity.OrganizationMembership;
import com.taskforge.entity.OrganizationRole;
import com.taskforge.entity.Project;
import com.taskforge.entity.Task;
import com.taskforge.entity.TaskPriority;
import com.taskforge.entity.TaskStatus;
import com.taskforge.repository.AppUserRepository;
import com.taskforge.repository.OrganizationMembershipRepository;
import com.taskforge.repository.OrganizationRepository;
import com.taskforge.repository.ProjectRepository;
import com.taskforge.repository.TaskRepository;
import com.taskforge.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProjectTaskSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMembershipRepository membershipRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    private AppUser user;
    private AppUser memberUser;
    private AppUser outsiderUser;

    private Organization organizationA;
    private Organization organizationB;

    private Project projectA;
    private Project projectB;

    private Task taskA;
    private Task taskB;

    private String jwtToken;
    private String memberJwtToken;

    @BeforeEach
    void setUp() {

        // Clear existing test data in foreign-key-safe order.
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        membershipRepository.deleteAll();
        organizationRepository.deleteAll();
        appUserRepository.deleteAll();

        // Create the organization owner.
        user = appUserRepository.saveAndFlush(
                new AppUser(
                        "Test Owner",
                        "testowner@example.com",
                        "not-used-for-jwt-tests"
                )
        );

        // Create a regular organization member.
        memberUser = appUserRepository.saveAndFlush(
                new AppUser(
                        "Test Member",
                        "testmember@example.com",
                        "not-used-for-jwt-tests"
                )
        );

        // Create a user who does not belong to either organization.
        outsiderUser = appUserRepository.saveAndFlush(
                new AppUser(
                        "Outside User",
                        "outsider@example.com",
                        "not-used-for-jwt-tests"
                )
        );

        // Create two organizations.
        organizationA = organizationRepository.saveAndFlush(
                new Organization(
                        "Organization A",
                        "organization-a",
                        "First test organization"
                )
        );

        organizationB = organizationRepository.saveAndFlush(
                new Organization(
                        "Organization B",
                        "organization-b",
                        "Second test organization"
                )
        );

        // The owner belongs to Organization A as OWNER.
        membershipRepository.saveAndFlush(
                new OrganizationMembership(
                        organizationA,
                        user,
                        OrganizationRole.OWNER
                )
        );

        // The second user belongs to Organization A as MEMBER.
        membershipRepository.saveAndFlush(
                new OrganizationMembership(
                        organizationA,
                        memberUser,
                        OrganizationRole.MEMBER
                )
        );

        // Create a project in each organization.
        projectA = new Project(
                "Project A",
                "Accessible project"
        );

        projectA.setOrganization(organizationA);
        projectA = projectRepository.saveAndFlush(projectA);

        projectB = new Project(
                "Project B",
                "Restricted project"
        );

        projectB.setOrganization(organizationB);
        projectB = projectRepository.saveAndFlush(projectB);

        // Create a task in each project.
        taskA = new Task();
        taskA.setTitle("Task A");
        taskA.setDescription("Accessible task");
        taskA.setStatus(TaskStatus.TODO);
        taskA.setPriority(TaskPriority.MEDIUM);
        taskA.setProject(projectA);

        taskA = taskRepository.saveAndFlush(taskA);

        taskB = new Task();
        taskB.setTitle("Task B");
        taskB.setDescription("Restricted task");
        taskB.setStatus(TaskStatus.TODO);
        taskB.setPriority(TaskPriority.MEDIUM);
        taskB.setProject(projectB);

        taskB = taskRepository.saveAndFlush(taskB);

        // Generate real JWTs for the owner and member.
        jwtToken = generateToken(user);
        memberJwtToken = generateToken(memberUser);
    }

    private String generateToken(AppUser appUser) {

        return jwtService.generateToken(
                userDetailsService.loadUserByUsername(
                        appUser.getEmail()
                )
        );
    }

    // --------------------------------------------------
    // AUTHENTICATION TESTS
    // --------------------------------------------------

    @Test
    void projectsEndpointShouldRejectUnauthenticatedRequests()
            throws Exception {

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tasksEndpointShouldRejectUnauthenticatedRequests()
            throws Exception {

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void projectsEndpointShouldRejectInvalidJwt()
            throws Exception {

        mockMvc.perform(
                get("/api/projects")
                        .header(
                                "Authorization",
                                "Bearer invalid.token.value"
                        )
        ).andExpect(status().isUnauthorized());
    }

    // --------------------------------------------------
    // ORGANIZATION ISOLATION: READ OPERATIONS
    // --------------------------------------------------

    @Test
    void projectListShouldContainOnlyAccessibleOrganizationProjects()
            throws Exception {

        mockMvc.perform(
                get("/api/projects")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].name").value("Project A"));
    }

    @Test
    void taskListShouldContainOnlyAccessibleOrganizationTasks()
            throws Exception {

        mockMvc.perform(
                get("/api/tasks")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].title").value("Task A"));
    }

    @Test
    void userShouldNotReadProjectFromAnotherOrganization()
            throws Exception {

        mockMvc.perform(
                get("/api/projects/" + projectB.getId())
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        ).andExpect(status().isForbidden());
    }

    @Test
    void userShouldNotReadTaskFromAnotherOrganization()
            throws Exception {

        mockMvc.perform(
                get("/api/tasks/" + taskB.getId())
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        ).andExpect(status().isForbidden());
    }

    // --------------------------------------------------
    // PROJECT ROLE-BASED AUTHORIZATION
    // --------------------------------------------------

    @Test
    void memberShouldNotCreateProject()
            throws Exception {

        mockMvc.perform(
                post("/api/projects")
                        .header(
                                "Authorization",
                                "Bearer " + memberJwtToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                    "name": "Member Project",
                                    "description": "Should be rejected",
                                    "organizationId": %d
                                }
                                """.formatted(organizationA.getId())
                        )
        ).andExpect(status().isForbidden());
    }

    @Test
    void memberShouldNotUpdateProject()
            throws Exception {

        mockMvc.perform(
                put("/api/projects/" + projectA.getId())
                        .header(
                                "Authorization",
                                "Bearer " + memberJwtToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                    "name": "Updated Project",
                                    "description": "Should be rejected",
                                    "organizationId": %d
                                }
                                """.formatted(organizationA.getId())
                        )
        ).andExpect(status().isForbidden());
    }

    @Test
    void memberShouldNotDeleteProject()
            throws Exception {

        mockMvc.perform(
                delete("/api/projects/" + projectA.getId())
                        .header(
                                "Authorization",
                                "Bearer " + memberJwtToken
                        )
        ).andExpect(status().isForbidden());
    }

    @Test
    void ownerShouldBeAbleToCreateProject()
            throws Exception {

        mockMvc.perform(
                post("/api/projects")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                    "name": "Owner Created Project",
                                    "description": "Created by owner",
                                    "organizationId": %d
                                }
                                """.formatted(organizationA.getId())
                        )
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name")
                .value("Owner Created Project"))
        .andExpect(jsonPath("$.organizationId")
                .value(organizationA.getId()));
    }

    // --------------------------------------------------
    // TASK CREATION AND ORGANIZATION ISOLATION
    // --------------------------------------------------

    @Test
    void memberShouldBeAbleToCreateTaskInTheirOrganization()
            throws Exception {

        mockMvc.perform(
                post("/api/tasks")
                        .header(
                                "Authorization",
                                "Bearer " + memberJwtToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                    "title": "Member Created Task",
                                    "description": "Valid task",
                                    "status": "TODO",
                                    "priority": "MEDIUM",
                                    "projectId": %d,
                                    "assigneeId": null
                                }
                                """.formatted(projectA.getId())
                        )
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title")
                .value("Member Created Task"))
        .andExpect(jsonPath("$.projectId")
                .value(projectA.getId()));
    }

    @Test
    void memberShouldNotCreateTaskInAnotherOrganization()
            throws Exception {

        mockMvc.perform(
                post("/api/tasks")
                        .header(
                                "Authorization",
                                "Bearer " + memberJwtToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                    "title": "Unauthorized Task",
                                    "description": "Should be rejected",
                                    "status": "TODO",
                                    "priority": "MEDIUM",
                                    "projectId": %d,
                                    "assigneeId": null
                                }
                                """.formatted(projectB.getId())
                        )
        ).andExpect(status().isForbidden());
    }

    // --------------------------------------------------
    // TASK UPDATE SECURITY
    // --------------------------------------------------

    @Test
    void memberShouldNotMoveTaskToAnotherOrganizationsProject()
            throws Exception {

        mockMvc.perform(
                put("/api/tasks/" + taskA.getId())
                        .header(
                                "Authorization",
                                "Bearer " + memberJwtToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                    "title": "Moved Task",
                                    "description": "Cross-organization move",
                                    "status": "TODO",
                                    "priority": "MEDIUM",
                                    "projectId": %d,
                                    "assigneeId": null
                                }
                                """.formatted(projectB.getId())
                        )
        ).andExpect(status().isBadRequest());
    }

    // --------------------------------------------------
    // TASK ASSIGNEE VALIDATION
    // --------------------------------------------------

    @Test
    void memberShouldNotAssignTaskToUserOutsideOrganization()
            throws Exception {

        mockMvc.perform(
                post("/api/tasks")
                        .header(
                                "Authorization",
                                "Bearer " + memberJwtToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                    "title": "Task With Invalid Assignee",
                                    "description": "Assignee is not a member",
                                    "status": "TODO",
                                    "priority": "MEDIUM",
                                    "projectId": %d,
                                    "assigneeId": %d
                                }
                                """.formatted(
                                        projectA.getId(),
                                        outsiderUser.getId()
                                )
                        )
        ).andExpect(status().isBadRequest());
    }

    // --------------------------------------------------
    // REQUEST VALIDATION
    // --------------------------------------------------

    @Test
    void projectCreationShouldRejectBlankProjectName()
            throws Exception {

        mockMvc.perform(
                post("/api/projects")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                    "name": "   ",
                                    "description": "Invalid project",
                                    "organizationId": %d
                                }
                                """.formatted(organizationA.getId())
                        )
        ).andExpect(status().isBadRequest());
    }

    @Test
    void taskCreationShouldRejectMissingRequiredStatus()
            throws Exception {

        mockMvc.perform(
                post("/api/tasks")
                        .header(
                                "Authorization",
                                "Bearer " + memberJwtToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                    "title": "Task Without Status",
                                    "description": "Invalid task",
                                    "status": null,
                                    "priority": "MEDIUM",
                                    "projectId": %d,
                                    "assigneeId": null
                                }
                                """.formatted(projectA.getId())
                        )
        ).andExpect(status().isBadRequest());
    }
}