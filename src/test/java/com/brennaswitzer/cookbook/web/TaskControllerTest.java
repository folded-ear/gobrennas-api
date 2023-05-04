package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.message.RenamePlanTreeItem;
import com.brennaswitzer.cookbook.payload.AclInfo;
import com.brennaswitzer.cookbook.payload.GrantInfo;
import com.brennaswitzer.cookbook.payload.PlanItemInfo;
import com.brennaswitzer.cookbook.repositories.PlanItemRepository;
import com.brennaswitzer.cookbook.repositories.PlanRepository;
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
import java.util.Iterator;
import java.util.List;

import static com.brennaswitzer.cookbook.util.PlanTestUtils.renderTree;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    private PlanItemRepository itemRepo;

    @Autowired
    private PlanRepository planRepo;

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
        Plan root = itemRepo.save(new Plan(alice, "Root"));
        PlanItem one = itemRepo.save(new PlanItem("One").of(root));
        PlanItem oneA = itemRepo.save(new PlanItem("A").of(one));
        PlanItem oneB = itemRepo.save(new PlanItem("B").of(one));
        PlanItem two = itemRepo.save(new PlanItem("Two").of(root));
        sync();

        PlanItemInfo ti;
        // sanity
        ti = forInfo(get("/api/tasks/{id}", root.getId()), status().isOk());
        assertArrayEquals(new long[]{
                one.getId(),
                two.getId(),
        }, ti.getSubtaskIds());
        ti = forInfo(get("/api/tasks/{id}", one.getId()), status().isOk());
        assertArrayEquals(new long[]{
                oneA.getId(),
                oneB.getId(),
        }, ti.getSubtaskIds());
        ti = forInfo(get("/api/tasks/{id}", oneA.getId()), status().isOk());
        assertNull(ti.getSubtaskIds());

        List<PlanItemInfo> tasks = forInfoList(
                get("/api/plan/{id}/self-and-descendants", root.getId()),
                status().isOk());
        Iterator<PlanItemInfo> itr = tasks.iterator();
        assertEquals("Root", itr.next().getName());
        assertEquals("One", itr.next().getName());
        assertEquals("A", itr.next().getName());
        assertEquals("B", itr.next().getName());
        assertEquals("Two", itr.next().getName());
        assertFalse(itr.hasNext());
    }

    @Test
    public void listGrants() throws Exception {
        Plan root = itemRepo.save(new Plan(alice, "Root"));
        AclInfo acl = forObject(
                get("/api/tasks/{id}/acl", root.getId()),
                status().isOk(),
                AclInfo.class);
        assertEquals(alice.getId(), acl.getOwnerId());
        assertNull(acl.getGrants());

        // if bob asks for lists, he gets nothing
        List<PlanItemInfo> ls = forInfoList(
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

        root = planRepo.getReferenceById(root.getId());
        assertEquals(AccessLevel.VIEW, root.getAcl().getGrant(bob));

        // if bob asks for lists, he gets root
        ls = forInfoList(
                get("/api/tasks/"),
                status().isOk(),
                bob);
        assertEquals(1, ls.size());
        assertEquals("Root", ls.get(0).getName());

        // alice can rename
        MockHttpServletRequestBuilder renameReq = put("/api/plan/{id}/rename", root.getId());
        perform(makeJson(renameReq, new RenamePlanTreeItem(root.getId(), "Root Alice")),
                alice) // default, but be explicit
                .andExpect(status().isOk());
        // bob and eve cannot
        perform(makeJson(renameReq, new RenamePlanTreeItem(root.getId(), "Root Bob")), bob)
                .andExpect(status().isForbidden());
        perform(makeJson(renameReq, new RenamePlanTreeItem(root.getId(), "Root Eve")), eve)
                .andExpect(status().isForbidden());

        root = planRepo.getReferenceById(root.getId());
        assertEquals("Root Alice", root.getName());

        forJson(
                makeJson(post("/api/tasks/{id}/acl/grants", root.getId()),
                        GrantInfo.fromGrant(bob, AccessLevel.ADMINISTER)),
                status().isCreated());

        // now bob can rename too!
        perform(makeJson(renameReq, new RenamePlanTreeItem(root.getId(), "Root Bob")), bob)
                .andExpect(status().isOk());

        root = planRepo.getReferenceById(root.getId());
        assertEquals("Root Bob", root.getName());

        // idempotent
        for (int i = 0; i < 2; i++) {
            perform(delete("/api/tasks/{id}/acl/grants/{userId}", root.getId(), bob.getId()))
                    .andExpect(status().isNoContent());
        }

        root = planRepo.getReferenceById(root.getId());
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

    private <T> T forObject(MockHttpServletRequestBuilder req,
                            ResultMatcher expect,
                            JavaType type,
                            User as) throws Exception {
        return objectMapper.readValue(forJson(req, expect, as), type);
    }

    private <T> T forObject(MockHttpServletRequestBuilder req, ResultMatcher expect, Class<T> clazz) throws Exception {
        return forObject(req, expect, clazz, null);
    }

    private <T> T forObject(MockHttpServletRequestBuilder req,
                            ResultMatcher expect,
                            Class<T> clazz,
                            User as) throws Exception {
        return forObject(req, expect,
                         objectMapper.getTypeFactory().constructType(clazz), as);
    }

    private PlanItemInfo forInfo(MockHttpServletRequestBuilder req, ResultMatcher expect) throws Exception {
        return forObject(req, expect, PlanItemInfo.class);
    }

    private List<PlanItemInfo> forInfoList(MockHttpServletRequestBuilder req, ResultMatcher expect) throws Exception {
        return forInfoList(req, expect, null);
    }

    private List<PlanItemInfo> forInfoList(MockHttpServletRequestBuilder req,
                                           ResultMatcher expect,
                                           User as) throws Exception {
        return forObject(req, expect,
                         objectMapper.getTypeFactory().constructCollectionType(
                                 List.class,
                                 PlanItemInfo.class), as);
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
        System.out.println(renderTree(header, planRepo.findByOwner(alice)));
    }

}
