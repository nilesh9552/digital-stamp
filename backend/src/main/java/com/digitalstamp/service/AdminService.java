package com.digitalstamp.service;

import com.digitalstamp.dto.PagedResponse;
import com.digitalstamp.dto.admin.CreateUserRequest;
import com.digitalstamp.dto.admin.DashboardStatsResponse;
import com.digitalstamp.dto.admin.ShopStatisticRow;
import com.digitalstamp.dto.user.UpdateProfileRequest;
import com.digitalstamp.dto.user.UserResponse;
import com.digitalstamp.entity.LoyaltyProgram;
import com.digitalstamp.entity.Role;
import com.digitalstamp.entity.Shop;
import com.digitalstamp.entity.StampTransactionType;
import com.digitalstamp.entity.SystemSetting;
import com.digitalstamp.entity.User;
import com.digitalstamp.exception.ApiException;
import com.digitalstamp.mapper.EntityMapper;
import com.digitalstamp.repository.LoyaltyCardRepository;
import com.digitalstamp.repository.LoyaltyProgramRepository;
import com.digitalstamp.repository.RewardRedemptionRepository;
import com.digitalstamp.repository.ShopRepository;
import com.digitalstamp.repository.StampTransactionRepository;
import com.digitalstamp.repository.SystemSettingRepository;
import com.digitalstamp.repository.UserRepository;
import com.digitalstamp.util.TokenUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final StampTransactionRepository stampTransactionRepository;
    private final RewardRedemptionRepository redemptionRepository;
    private final LoyaltyCardRepository loyaltyCardRepository;
    private final LoyaltyProgramRepository loyaltyProgramRepository;
    private final SystemSettingRepository settingRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityMapper mapper;
    private final ShopService shopService;

    public AdminService(UserRepository userRepository,
                        ShopRepository shopRepository,
                        StampTransactionRepository stampTransactionRepository,
                        RewardRedemptionRepository redemptionRepository,
                        LoyaltyCardRepository loyaltyCardRepository,
                        LoyaltyProgramRepository loyaltyProgramRepository,
                        SystemSettingRepository settingRepository,
                        PasswordEncoder passwordEncoder,
                        EntityMapper mapper,
                        ShopService shopService) {
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.stampTransactionRepository = stampTransactionRepository;
        this.redemptionRepository = redemptionRepository;
        this.loyaltyCardRepository = loyaltyCardRepository;
        this.loyaltyProgramRepository = loyaltyProgramRepository;
        this.settingRepository = settingRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.shopService = shopService;
    }

    public DashboardStatsResponse platformDashboard() {
        Instant startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        return DashboardStatsResponse.builder()
                .totalShops(shopRepository.count())
                .activeShops(shopRepository.countByActiveTrue())
                .totalCustomers(userRepository.countByRole(Role.CUSTOMER))
                .totalShopOwners(userRepository.countByRole(Role.SHOP_OWNER))
                .totalStamps(stampTransactionRepository.sumAllAddedStamps())
                .totalRewardsRedeemed(redemptionRepository.count())
                .todayStamps(stampTransactionRepository.findAll().stream()
                        .filter(t -> t.getTransactionType() == StampTransactionType.ADD && t.getCreatedAt().isAfter(startOfDay))
                        .mapToLong(t -> t.getStampsAdded()).sum())
                .todayRedemptions(redemptionRepository.countByStatus(com.digitalstamp.entity.RedemptionStatus.COMPLETED))
                .build();
    }

    public DashboardStatsResponse ownerDashboard(User owner) {
        Shop shop = shopService.requireOwnerShop(owner);
        Instant startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        return DashboardStatsResponse.builder()
                .shopName(shop.getName())
                .totalCustomersForShop(loyaltyCardRepository.countByShop_Id(shop.getId()))
                .activeLoyaltyCards(loyaltyCardRepository.countByShop_IdAndStatus(shop.getId(), com.digitalstamp.entity.LoyaltyCardStatus.ACTIVE)
                        + loyaltyCardRepository.countByShop_IdAndStatus(shop.getId(), com.digitalstamp.entity.LoyaltyCardStatus.COMPLETED))
                .stampsIssued(stampTransactionRepository.sumByShopAndType(shop.getId(), StampTransactionType.ADD))
                .rewardsRedeemed(redemptionRepository.countByShop_Id(shop.getId()))
                .todayStamps(stampTransactionRepository.sumByShopTypeSince(shop.getId(), StampTransactionType.ADD, startOfDay))
                .todayRedemptions(redemptionRepository.countByShop_IdAndRedeemedAtGreaterThanEqual(shop.getId(), startOfDay))
                .build();
    }

    public List<ShopStatisticRow> shopStatistics() {
        return shopRepository.findAll().stream().map(shop -> ShopStatisticRow.builder()
                .shopId(shop.getId())
                .shopName(shop.getName())
                .slug(shop.getSlug())
                .customers(loyaltyCardRepository.countByShop_Id(shop.getId()))
                .stamps(stampTransactionRepository.sumByShopAndType(shop.getId(), StampTransactionType.ADD))
                .redemptions(redemptionRepository.countByShop_Id(shop.getId()))
                .active(shop.isActive())
                .build()).toList();
    }

    public PagedResponse<UserResponse> usersByRole(Role role, String q, Pageable pageable) {
        Page<User> page;
        if (q == null || q.isBlank()) {
            page = userRepository.findByRole(role, pageable);
        } else {
            page = userRepository.findByRoleAndNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                    role, q, role, q, pageable);
        }
        var items = page.getContent().stream().map(mapper::toUser).toList();
        return new PagedResponse<>(items, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw ApiException.conflict("An account with this email already exists");
        }
        Role role = request.getRole() == null ? Role.SHOP_OWNER : request.getRole();
        if (role == Role.SUPER_ADMIN) {
            throw ApiException.badRequest("Cannot create another super admin from this endpoint");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .mobile(request.getMobile())
                .role(role)
                .active(request.getActive() == null || request.getActive())
                .qrToken(TokenUtil.uuid())
                .build();
        user = userRepository.save(user);
        if (role == Role.SHOP_OWNER && request.getShopId() != null) {
            Shop shop = shopRepository.findById(request.getShopId())
                    .orElseThrow(() -> ApiException.notFound("Invalid shop"));
            if (shop.getOwner() != null && !shop.getOwner().getId().equals(user.getId())) {
                throw ApiException.conflict("Shop already has an owner");
            }
            shop.setOwner(user);
            shopRepository.save(shop);
        }
        return mapper.toUser(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, CreateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> ApiException.notFound("Customer not found"));
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getMobile() != null) {
            user.setMobile(request.getMobile());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (user.getRole() == Role.SHOP_OWNER && request.getShopId() != null) {
            Shop shop = shopRepository.findById(request.getShopId())
                    .orElseThrow(() -> ApiException.notFound("Invalid shop"));
            shop.setOwner(user);
            shopRepository.save(shop);
        }
        return mapper.toUser(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateProfile(User user, UpdateProfileRequest request) {
        user.setName(request.getName());
        user.setMobile(request.getMobile());
        return mapper.toUser(userRepository.save(user));
    }

    public List<SystemSetting> settings() {
        return settingRepository.findAll();
    }

    @Transactional
    public SystemSetting upsertSetting(String key, String value, String description) {
        SystemSetting setting = settingRepository.findBySettingKey(key).orElseGet(() ->
                SystemSetting.builder().settingKey(key).build());
        setting.setSettingValue(value);
        if (description != null) {
            setting.setDescription(description);
        }
        return settingRepository.save(setting);
    }

    public LoyaltyProgram programForShop(Shop shop) {
        return loyaltyProgramRepository.findByShop(shop)
                .orElseThrow(() -> ApiException.notFound("Loyalty program not found"));
    }

    public Map<String, Object> ownerShopBundle(User owner) {
        Shop shop = shopService.requireOwnerShop(owner);
        return Map.of(
                "shop", shopService.toResponse(shop),
                "program", programForShop(shop)
        );
    }
}
