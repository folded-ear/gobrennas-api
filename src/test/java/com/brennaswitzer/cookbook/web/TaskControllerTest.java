package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.payload.SubtaskIds;
import com.brennaswitzer.cookbook.payload.TaskInfo;
import com.brennaswitzer.cookbook.payload.TaskName;
import com.brennaswitzer.cookbook.repositories.TaskListRepository;
import com.brennaswitzer.cookbook.repositories.TaskRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static com.brennaswitzer.cookbook.util.TaskTestUtils.renderTree;
import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
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

    private User johann;

    @Before
    public void setUp() {
        johann = userRepository.save(new User("Johann", "johann", "johann@example.com", "<HIDDEN>"));
    }

    @Test
    public void theWholeShebang() throws Exception {
        TaskInfo ti;

        // sanity check before we start ...
        assertEquals(0, forInfoList(
                get("/api/tasks"),
                status().isOk())
                .size());

        ti = forInfo(
                makeJson(
                        post("/api/tasks"),
                        new TaskName("Groceries")),
                status().isCreated());
        Long groceryId = ti.getId();
        assertNotNull(ti.getId());
        assertEquals("Groceries", ti.getName());

        ti = forInfo(
                makeJson(
                        post("/api/tasks/{id}/subtasks", groceryId),
                        new TaskName("apple")),
                status().isCreated());
        Long appleId = ti.getId();
        assertNotNull(ti.getId());
        assertEquals("apple", ti.getName());

        // did it assemble right?
        ti = forInfo(get("/api/tasks/{id}", groceryId), status().isOk());
        assertArrayEquals(new long[] {
                appleId,
        }, ti.getSubtaskIds());

        ti = forInfo(
                makeJson(
                        post("/api/tasks/{id}/subtasks", groceryId),
                        new TaskName("OJ")), status().isCreated());
        Long ojId = ti.getId();
        assertNotNull(ti.getId());
        assertEquals(groceryId, ti.getParentId());
        assertEquals("OJ", ti.getName());

        // did it assemble right?
        ti = forInfo(get("/api/tasks/{id}", groceryId), status().isOk());
        assertArrayEquals(new long[] {
                ojId,
                appleId,
        }, ti.getSubtaskIds());

        ti = forInfo(
                makeJson(
                        post("/api/tasks/{id}/subtasks?after={afterId}", groceryId, ojId),
                        new TaskName("bagel")),
                status().isCreated());
        Long bagelId = ti.getId();
        assertNotNull(ti.getId());
        assertEquals("bagel", ti.getName());

        // did it assemble right?
        ti = forInfo(get("/api/tasks/{id}", groceryId), status().isOk());
        assertArrayEquals(new long[] {
                ojId,
                bagelId,
                appleId,
        }, ti.getSubtaskIds());

        perform(
                makeJson(put("/api/tasks/{id}/name", appleId),
                        new TaskName("apples")))
                .andExpect(status().isNoContent());

        ti = forInfo(
                get("/api/tasks/{id}", appleId),
                status().isOk());
        assertEquals(appleId, ti.getId());
        assertEquals("apples", ti.getName());

        treeView("First Version");

        perform(
                makeJson(put("/api/tasks/{id}/subtaskIds", groceryId),
                        new SubtaskIds(appleId, ojId, bagelId)))
                .andExpect(status().isNoContent());

        ti = forInfo(get("/api/tasks/{id}", groceryId), status().isOk());
        assertArrayEquals(new long[] {
                appleId,
                ojId,
                bagelId,
        }, ti.getSubtaskIds());

        treeView("Second Version");

        perform(delete("/api/tasks/{id}", ojId))
                .andExpect(status().isNoContent());

        ti = forInfo(get("/api/tasks/{id}", groceryId), status().isOk());
        assertArrayEquals(new long[] {
                appleId,
                bagelId,
        }, ti.getSubtaskIds());

        treeView("Third Version");
    }

    @Test
    public void subtasksCollection() throws Exception {
        Task root = taskRepo.save(new TaskList(johann, "Root"));
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

    private RequestPostProcessor johann() {
        return user(UserPrincipal.create(johann));
    }

    private ResultActions perform(MockHttpServletRequestBuilder req) throws Exception {
        ResultActions ra = mockMvc.perform(req.with(johann()));
        sync();
        return ra;
    }

    private void sync() {
        taskRepo.flush();
        entityManager.clear();
    }

    private MockHttpServletRequestBuilder makeJson(MockHttpServletRequestBuilder req, Object body) throws Exception {
        return req.contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    private TaskInfo forInfo(MockHttpServletRequestBuilder req, ResultMatcher expect) throws Exception {
        return objectMapper.readValue(
                forJson(req, expect),
                TaskInfo.class);
    }

    private List<TaskInfo> forInfoList(MockHttpServletRequestBuilder req, ResultMatcher expect) throws Exception {
        return objectMapper.readValue(
                forJson(req, expect),
                objectMapper.getTypeFactory().constructCollectionType(
                        List.class,
                        TaskInfo.class));
    }

    private String forJson(MockHttpServletRequestBuilder req, ResultMatcher expect) throws Exception {
        String content = perform(req)
                .andExpect(expect)
                .andReturn()
                .getResponse()
                .getContentAsString();
        System.out.println(content);
        return content;
    }

    private void treeView(String header) {
        sync();
        System.out.println(renderTree(header, listRepo.findByOwner(johann)));
    }

}