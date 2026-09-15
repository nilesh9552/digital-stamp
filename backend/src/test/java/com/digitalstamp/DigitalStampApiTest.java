package com.digitalstamp;

import com.digitalstamp.dto.auth.LoginRequest;
import com.digitalstamp.dto.auth.RegisterRequest;
import com.digitalstamp.entity.Role;
import com.digitalstamp.entity.User;
import com.digitalstamp.repository.UserRepository;
import com.digitalstamp.util.TokenUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DigitalStampApiTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String adminToken;
    private String ownerToken;
    private String customerToken;
    private String otherOwnerToken;
    private String customerQr;

    @BeforeEach
    void setUp() throws Exception {
        persist("Admin", "admin@test.com", "Admin@123", Role.SUPER_ADMIN);
        persist("Owner One", "owner1@test.com", "Owner@123", Role.SHOP_OWNER);
        persist("Owner Two", "owner2@test.com", "Owner@123", Role.SHOP_OWNER);

        adminToken = login("admin@test.com", "Admin@123");
        ownerToken = login("owner1@test.com", "Owner@123");
        otherOwnerToken = login("owner2@test.com", "Owner@123");

        mockMvc.perform(post("/api/admin/shops")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cafe One","slug":"cafe-one","description":"First cafe","address":"Pune",
                                "phone":"111","email":"one@test.com","ownerId":%d,"requiredStamps":10}
                                """.formatted(userRepository.findByEmailIgnoreCase("owner1@test.com").orElseThrow().getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/shops")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cafe Two","slug":"cafe-two","description":"Second cafe","address":"Mumbai",
                                "phone":"222","email":"two@test.com","ownerId":%d,"requiredStamps":8}
                                """.formatted(userRepository.findByEmailIgnoreCase("owner2@test.com").orElseThrow().getId())))
                .andExpect(status().isCreated());

        RegisterRequest register = new RegisterRequest();
        register.setName("Customer One");
        register.setEmail("cust@test.com");
        register.setPassword("Customer@123");
        register.setMobile("9876543210");
        register.setShopSlug("cafe-one");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        customerToken = body.get("data").get("accessToken").asText();
        customerQr = "CUSTOMER:" + userRepository.findByEmailIgnoreCase("cust@test.com").orElseThrow().getQrToken();
    }

    @Test
    void loginRejectsBadPassword() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@test.com");
        req.setPassword("wrong");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateEmailRejected() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setName("Dup");
        register.setEmail("cust@test.com");
        register.setPassword("Customer@123");
        register.setMobile("9876543211");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isConflict());
    }

    @Test
    void jwtProtectsCustomerRoutes() throws Exception {
        mockMvc.perform(get("/api/customers/profile"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/customers/profile").header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("cust@test.com"));
    }

    @Test
    void shopOwnerCannotAccessAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerCannotAddStamps() throws Exception {
        mockMvc.perform(post("/api/stamps/add")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":1,\"stamps\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void stampAddAndRewardFlow() throws Exception {
        Long customerId = userRepository.findByEmailIgnoreCase("cust@test.com").orElseThrow().getId();
        mockMvc.perform(post("/api/stamps/scan")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qrPayload\":\"" + customerQr + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerName").value("Customer One"));

        mockMvc.perform(post("/api/stamps/add")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":" + customerId + ",\"stamps\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stampsAdded").value(10));

        MvcResult rewards = mockMvc.perform(get("/api/customers/rewards")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = objectMapper.readTree(rewards.getResponse().getContentAsString()).get("data");
        // create a reward first
        mockMvc.perform(post("/api/owner/rewards")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Free Coffee\",\"description\":\"On the house\",\"requiredStamps\":10,\"active\":true}"))
                .andExpect(status().isOk());

        MvcResult after = mockMvc.perform(get("/api/customers/rewards")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode rewardsJson = objectMapper.readTree(after.getResponse().getContentAsString()).get("data");
        long rewardId = rewardsJson.get(0).get("id").asLong();

        mockMvc.perform(post("/api/rewards/" + rewardId + "/redeem")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.redemptionCode").exists());

        mockMvc.perform(post("/api/rewards/" + rewardId + "/redeem")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Insufficient stamps")));
    }

    @Test
    void invalidQrRejected() throws Exception {
        mockMvc.perform(post("/api/stamps/scan")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qrPayload\":\"CUSTOMER:not-a-real-token\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid QR")));
    }

    @Test
    void ownerCannotStampOtherShopCustomer() throws Exception {
        Long customerId = userRepository.findByEmailIgnoreCase("cust@test.com").orElseThrow().getId();
        mockMvc.perform(post("/api/stamps/add")
                        .header("Authorization", "Bearer " + otherOwnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":" + customerId + ",\"stamps\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("not registered with this shop")));
    }

    @Test
    void shopCreationRequiresAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/shops")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"slug\":\"x\"}"))
                .andExpect(status().isForbidden());
    }

    private User persist(String name, String email, String password, Role role) {
        return userRepository.save(User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .mobile("1111111111")
                .role(role)
                .active(true)
                .qrToken(TokenUtil.uuid())
                .build());
    }

    private String login(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("accessToken").asText();
    }
}
