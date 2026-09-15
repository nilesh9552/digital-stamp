package com.digitalstamp.service;

import com.digitalstamp.dto.PagedResponse;
import com.digitalstamp.dto.loyalty.LoyaltyCardResponse;
import com.digitalstamp.dto.loyalty.LoyaltyProgramRequest;
import com.digitalstamp.dto.shop.ShopRequest;
import com.digitalstamp.dto.shop.ShopResponse;
import com.digitalstamp.entity.LoyaltyCard;
import com.digitalstamp.entity.LoyaltyCardStatus;
import com.digitalstamp.entity.LoyaltyProgram;
import com.digitalstamp.entity.Role;
import com.digitalstamp.entity.Shop;
import com.digitalstamp.entity.User;
import com.digitalstamp.exception.ApiException;
import com.digitalstamp.mapper.EntityMapper;
import com.digitalstamp.repository.LoyaltyCardRepository;
import com.digitalstamp.repository.LoyaltyProgramRepository;
import com.digitalstamp.repository.ShopRepository;
import com.digitalstamp.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class ShopService {

    private final ShopRepository shopRepository;
    private final LoyaltyProgramRepository loyaltyProgramRepository;
    private final LoyaltyCardRepository loyaltyCardRepository;
    private final UserRepository userRepository;
    private final EntityMapper mapper;
    private final com.digitalstamp.repository.RewardRedemptionRepository redemptionRepository;
    private final com.digitalstamp.repository.StampTransactionRepository stampTransactionRepository;
    private final com.digitalstamp.repository.RewardRepository rewardRepository;

    public ShopService(ShopRepository shopRepository,
                       LoyaltyProgramRepository loyaltyProgramRepository,
                       LoyaltyCardRepository loyaltyCardRepository,
                       UserRepository userRepository,
                       EntityMapper mapper,
                       com.digitalstamp.repository.RewardRedemptionRepository redemptionRepository,
                       com.digitalstamp.repository.StampTransactionRepository stampTransactionRepository,
                       com.digitalstamp.repository.RewardRepository rewardRepository) {
        this.shopRepository = shopRepository;
        this.loyaltyProgramRepository = loyaltyProgramRepository;
        this.loyaltyCardRepository = loyaltyCardRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.redemptionRepository = redemptionRepository;
        this.stampTransactionRepository = stampTransactionRepository;
        this.rewardRepository = rewardRepository;
    }

    public List<ShopResponse> publicShops() {
        return shopRepository.findAll().stream()
                .filter(Shop::isActive)
                .map(this::toResponse)
                .toList();
    }

    public ShopResponse getBySlug(String slug) {
        Shop shop = shopRepository.findBySlugWithOwner(slug)
                .orElseThrow(() -> ApiException.notFound("Invalid shop"));
        return toResponse(shop);
    }

    public ShopResponse getById(Long id) {
        Shop shop = shopRepository.findByIdWithOwner(id)
                .orElseThrow(() -> ApiException.notFound("Invalid shop"));
        return toResponse(shop);
    }

    public PagedResponse<ShopResponse> adminList(String q, Pageable pageable) {
        Page<Shop> page;
        if (q == null || q.isBlank()) {
            page = shopRepository.findAll(pageable);
        } else {
            page = shopRepository.findByNameContainingIgnoreCaseOrSlugContainingIgnoreCase(q, q, pageable);
        }
        List<ShopResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return new PagedResponse<>(items, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    @Transactional
    public ShopResponse create(ShopRequest request) {
        String slug = normalizeSlug(request.getSlug());
        if (shopRepository.existsBySlug(slug)) {
            throw ApiException.conflict("A shop with this slug already exists");
        }
        Shop shop = Shop.builder()
                .name(request.getName().trim())
                .slug(slug)
                .logo(request.getLogo())
                .description(request.getDescription())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .openingHours(request.getOpeningHours())
                .active(request.getActive() == null || request.getActive())
                .build();
        if (request.getOwnerId() != null) {
            shop.setOwner(resolveOwner(request.getOwnerId(), null));
        }
        shop = shopRepository.save(shop);
        createDefaultProgram(shop, request);
        return toResponse(shop);
    }

    @Transactional
    public ShopResponse update(Long id, ShopRequest request) {
        Shop shop = shopRepository.findById(id).orElseThrow(() -> ApiException.notFound("Invalid shop"));
        String slug = normalizeSlug(request.getSlug());
        shopRepository.findBySlug(slug).filter(existing -> !existing.getId().equals(id))
                .ifPresent(s -> { throw ApiException.conflict("A shop with this slug already exists"); });
        shop.setName(request.getName().trim());
        shop.setSlug(slug);
        if (request.getLogo() != null) {
            shop.setLogo(request.getLogo());
        }
        shop.setDescription(request.getDescription());
        shop.setAddress(request.getAddress());
        shop.setPhone(request.getPhone());
        shop.setEmail(request.getEmail());
        shop.setOpeningHours(request.getOpeningHours());
        if (request.getActive() != null) {
            shop.setActive(request.getActive());
        }
        if (request.getOwnerId() != null) {
            shop.setOwner(resolveOwner(request.getOwnerId(), shop.getId()));
        }
        shopRepository.save(shop);
        loyaltyProgramRepository.findByShop(shop).ifPresent(program -> {
            if (request.getLoyaltyProgramName() != null) {
                program.setName(request.getLoyaltyProgramName());
            }
            if (request.getLoyaltyProgramDescription() != null) {
                program.setDescription(request.getLoyaltyProgramDescription());
            }
            if (request.getRequiredStamps() != null) {
                program.setRequiredStamps(request.getRequiredStamps());
            }
            loyaltyProgramRepository.save(program);
        });
        return toResponse(shop);
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        Shop shop = shopRepository.findById(id).orElseThrow(() -> ApiException.notFound("Invalid shop"));
        shop.setActive(active);
        shopRepository.save(shop);
    }

    @Transactional
    public void delete(Long id) {
        Shop shop = shopRepository.findById(id).orElseThrow(() -> ApiException.notFound("Invalid shop"));
        redemptionRepository.deleteByShop_Id(id);
        stampTransactionRepository.deleteByShop_Id(id);
        rewardRepository.deleteByShop_Id(id);
        loyaltyCardRepository.deleteByShop_Id(id);
        loyaltyProgramRepository.deleteByShop_Id(id);
        shopRepository.delete(shop);
    }

    public Shop requireOwnerShop(User owner) {
        return shopRepository.findByOwner_Id(owner.getId())
                .orElseThrow(() -> ApiException.forbidden("No shop is assigned to this owner"));
    }

    @Transactional
    public LoyaltyProgram updateProgram(Shop shop, LoyaltyProgramRequest request) {
        LoyaltyProgram program = loyaltyProgramRepository.findByShop(shop)
                .orElseThrow(() -> ApiException.notFound("Loyalty program not found"));
        program.setName(request.getName());
        program.setDescription(request.getDescription());
        program.setRequiredStamps(request.getRequiredStamps());
        if (request.getActive() != null) {
            program.setActive(request.getActive());
        }
        return loyaltyProgramRepository.save(program);
    }

    public LoyaltyCardResponse enroll(User customer, String slug) {
        return mapper.toCard(enrollInternal(customer, slug));
    }

    @Transactional
    public LoyaltyCard enrollInternal(User customer, String slug) {
        Shop shop = shopRepository.findBySlug(slug).orElseThrow(() -> ApiException.notFound("Invalid shop"));
        if (!shop.isActive()) {
            throw ApiException.badRequest("Inactive shop");
        }
        return loyaltyCardRepository.findByCustomerAndShop(customer, shop)
                .orElseGet(() -> createCard(customer, shop));
    }

    private LoyaltyCard createCard(User customer, Shop shop) {
        LoyaltyProgram program = loyaltyProgramRepository.findByShop(shop)
                .orElseThrow(() -> ApiException.badRequest("Shop has no loyalty program"));
        if (!program.isActive()) {
            throw ApiException.badRequest("Loyalty program is not active");
        }
        LoyaltyCard card = LoyaltyCard.builder()
                .customer(customer)
                .shop(shop)
                .loyaltyProgram(program)
                .currentStamps(0)
                .totalStamps(0)
                .status(LoyaltyCardStatus.ACTIVE)
                .build();
        return loyaltyCardRepository.save(card);
    }

    private void createDefaultProgram(Shop shop, ShopRequest request) {
        LoyaltyProgram program = LoyaltyProgram.builder()
                .shop(shop)
                .name(request.getLoyaltyProgramName() != null ? request.getLoyaltyProgramName() : shop.getName() + " Rewards")
                .description(request.getLoyaltyProgramDescription() != null ? request.getLoyaltyProgramDescription() : "Collect stamps and redeem rewards")
                .requiredStamps(request.getRequiredStamps() != null ? request.getRequiredStamps() : 10)
                .active(true)
                .build();
        loyaltyProgramRepository.save(program);
    }

    private User resolveOwner(Long ownerId, Long currentShopId) {
        User owner = userRepository.findById(ownerId).orElseThrow(() -> ApiException.notFound("Shop owner not found"));
        if (owner.getRole() != Role.SHOP_OWNER) {
            throw ApiException.badRequest("User is not a shop owner");
        }
        shopRepository.findByOwner_Id(ownerId).ifPresent(existing -> {
            if (currentShopId == null || !existing.getId().equals(currentShopId)) {
                throw ApiException.conflict("This shop owner is already assigned to another shop");
            }
        });
        return owner;
    }

    public ShopResponse toResponse(Shop shop) {
        var program = loyaltyProgramRepository.findByShop(shop);
        return mapper.toShop(
                shop,
                program.map(LoyaltyProgram::getName).orElse(null),
                program.map(LoyaltyProgram::getDescription).orElse(null),
                program.map(LoyaltyProgram::getRequiredStamps).orElse(null),
                program.map(LoyaltyProgram::isActive).orElse(false)
        );
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]+", "-").replaceAll("(^-|-$)", "");
    }
}
