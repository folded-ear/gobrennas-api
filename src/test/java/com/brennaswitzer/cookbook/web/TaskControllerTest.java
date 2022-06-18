package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.payload.AclInfo;
import com.brennaswitzer.cookbook.payload.GrantInfo;
import com.brennaswitzer.cookbook.payload.TaskInfo;
import com.brennaswitzer.cookbook.payload.TaskName;
import com.brennaswitzer.cookbook.repositories.TaskListRepository;
import com.brennaswitzer.cookbook.repositories.TaskRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static com.brennaswitzer.cookbook.util.TaskTestUtils.renderTree;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@WithAliceBobEve(authentication = false)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepo;

    @Autowired
    private TaskListRepository listRepo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User alice, bob, eve;

    @BeforeEach
    public void setUp() {
        alice = userRepository.getByName("Alice");
        bob = userRepository.getByName("Bob");
        eve = userRepository.getByName("Eve");
    }

    @Test
    public void subtasksCollection() throws Exception {
        TaskList root = taskRepo.save(new TaskList(alice, "Root"));
        Task one = taskRepo.save(new Task("One").of(root));
        Task oneA = taskRepo.save(new Task("A").of(one));
        Task oneB = taskRepo.save(new Task("B").of(one));
        Task two = taskRepo.save(new Task("Two").of(root));
        sync();

        TaskInfo ti;
        // sanity
        ti = forInfo(get("/api/tasks/{id}", root.getId()), status().isOk());
        assertArrayEquals(new long[] {
                one.getId(),
                two.getId(),
        }, ti.getSubtaskIds());
        ti = forInfo(get("/api/tasks/{id}", one.getId()), status().isOk());
        assertArrayEquals(new long[] {
                oneA.getId(),
                oneB.getId(),
        }, ti.getSubtaskIds());
        ti = forInfo(get("/api/tasks/{id}", oneA.getId()), status().isOk());
        assertNull(ti.getSubtaskIds());

        List<TaskInfo> tasks = forInfoList(
                get("/api/tasks/{id}/subtasks", root.getId()),
                status().isOk());
        assertEquals(2, tasks.size());
        assertEquals("One", tasks.get(0).getName());
        assertEquals("Two", tasks.get(1).getName());

        tasks = forInfoList(
                get("/api/tasks/{id}/subtasks", one.getId()),
                status().isOk());
        assertEquals(2, tasks.size());
        assertEquals("A", tasks.get(0).getName());
        assertEquals("B", tasks.get(1).getName());

        tasks = forInfoList(
                get("/api/tasks/{id}/subtasks", two.getId()),
                status().isOk());
        assertEquals(0, tasks.size());
    }

    @Test
    public void listGrants() throws Exception {
        TaskList root = taskRepo.save(new TaskList(alice, "Root"));
        AclInfo acl = forObject(
                get("/api/tasks/{id}/acl", root.getId()),
                status().isOk(),
                AclInfo.class);
        assertEquals(alice.getId(), acl.getOwnerId());
        assertNull(acl.getGrants());

        // if bob asks for lists, he gets nothing
        List<TaskInfo> ls = forInfoList(
                get("/api/tasks/"),
                status().isOk(),
                bob);
        assertEquals(0, ls.size());

        // idempotent!
        for (int i = 0; i < 2; i++) {
            GrantInfo grant = forObject(
                    makeJson(post("/api/tasks/{id}/acl/grants", root.getId()),
                            GrantInfo.fromGrant(bob, AccessLevel.VIEW)),
                    status().isCreated(),
                    GrantInfo.class);

            assertEquals(bob.getId(), grant.getUserId());
            assertEquals(AccessLevel.VIEW, grant.getAccessLevel());
        }

        root = listRepo.getOne(root.getId());
        assertEquals(AccessLevel.VIEW, root.getAcl().getGrant(bob));

        // if bob asks for lists, he gets root
        ls = forInfoList(
                get("/api/tasks/"),
                status().isOk(),
                bob);
        assertEquals(1, ls.size());
        assertEquals("Root", ls.get(0).getName());

        // alice can rename
        MockHttpServletRequestBuilder renameReq = put("/api/tasks/{id}/name", root.getId());
        perform(makeJson(renameReq, new TaskName("Root Alice")), alice) // default, but be explicit
                .andExpect(status().isOk());
        // bob and eve cannot
        perform(makeJson(renameReq, new TaskName("Root Bob")), bob)
                .andExpect(status().isForbidden());
        perform(makeJson(renameReq, new TaskName("Root Eve")), eve)
                .andExpect(status().isForbidden());

        root = listRepo.getOne(root.getId());
        assertEquals("Root Alice", root.getName());

        forJson(
                makeJson(post("/api/tasks/{id}/acl/grants", root.getId()),
                        GrantInfo.fromGrant(bob, AccessLevel.ADMINISTER)),
                status().isCreated());

        // now bob can rename too!
        perform(makeJson(renameReq, new TaskName("Root Bob")), bob)
                .andExpect(status().isOk());

        root = listRepo.getOne(root.getId());
        assertEquals("Root Bob", root.getName());

        // idempotent
        for (int i = 0; i < 2; i++) {
            perform(delete("/api/tasks/{id}/acl/grants/{userId}", root.getId(), bob.getId()))
                    .andExpect(status().isNoContent());
        }

        root = listRepo.getOne(root.getId());
        assertNull(root.getAcl().getGrant(bob));

        // if bob asks for lists, he gets nothing
        ls = forInfoList(
                get("/api/tasks/"),
                status().isOk(),
                bob);
        assertEquals(0, ls.size());
    }

    private ResultActions perform(MockHttpServletRequestBuilder req) throws Exception {
        return perform(req, null);
    }

    private ResultActions perform(MockHttpServletRequestBuilder req, User as) throws Exception {
        ResultActions ra = mockMvc.perform(req.with(user(UserPrincipal.create(as == null ? alice : as))));
        sync();
        return ra;
    }

    private void sync() {
        entityManager.flush();
        entityManager.clear();
    }

    private MockHttpServletRequestBuilder makeJson(MockHttpServletRequestBuilder req, Object body) throws Exception {
        return req.contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    private <T> T forObject(MockHttpServletRequestBuilder req, ResultMatcher expect, JavaType type) throws Exception {
        return forObject(req, expect, type, null);
    }

    private <T> T forObject(MockHttpServletRequestBuilder req, ResultMatcher expect, JavaType type, User as) throws Exception {
        return objectMapper.readValue(forJson(req, expect, as), type);
    }

    private <T> T forObject(MockHttpServletRequestBuilder req, ResultMatcher expect, Class<T> clazz) throws Exception {
        return forObject(req, expect, clazz, null);
    }

    private <T> T forObject(MockHttpServletRequestBuilder req, ResultMatcher expect, Class<T> clazz, User as) throws Exception {
        return forObject(req, expect,
                objectMapper.getTypeFactory().constructType(clazz), as);
    }

    private TaskInfo forInfo(MockHttpServletRequestBuilder req, ResultMatcher expect) throws Exception {
        return forObject(req, expect, TaskInfo.class);
    }

    private List<TaskInfo> forInfoList(MockHttpServletRequestBuilder req, ResultMatcher expect) throws Exception {
        return forInfoList(req, expect, null);
    }

    private List<TaskInfo> forInfoList(MockHttpServletRequestBuilder req, ResultMatcher expect, User as) throws Exception {
        return forObject(req, expect,
                objectMapper.getTypeFactory().constructCollectionType(
                        List.class,
                        TaskInfo.class), as);
    }

    private String forJson(MockHttpServletRequestBuilder req, ResultMatcher expect) throws Exception {
        return forJson(req, expect, null);
    }

    private String forJson(MockHttpServletRequestBuilder req, ResultMatcher expect, User as) throws Exception {
        String content = perform(req, as)
                .andExpect(expect)
                .andReturn()
                .getResponse()
                .getContentAsString();
        System.out.println(content);
        return content;
    }

    private void treeView(String header) {
        sync();
        System.out.println(renderTree(header, listRepo.findByOwner(alice)));
    }

}
