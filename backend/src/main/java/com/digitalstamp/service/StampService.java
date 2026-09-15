package com.digitalstamp.service;

import com.digitalstamp.dto.PagedResponse;
import com.digitalstamp.dto.stamp.ScanResultResponse;
import com.digitalstamp.dto.stamp.StampActionRequest;
import com.digitalstamp.dto.stamp.StampTransactionResponse;
import com.digitalstamp.dto.user.OwnerCustomerRow;
import com.digitalstamp.entity.LoyaltyCard;
import com.digitalstamp.entity.LoyaltyCardStatus;
import com.digitalstamp.entity.Role;
import com.digitalstamp.entity.Shop;
import com.digitalstamp.entity.StampTransaction;
import com.digitalstamp.entity.StampTransactionType;
import com.digitalstamp.entity.User;
import com.digitalstamp.exception.ApiException;
import com.digitalstamp.mapper.EntityMapper;
import com.digitalstamp.repository.LoyaltyCardRepository;
import com.digitalstamp.repository.StampTransactionRepository;
import com.digitalstamp.repository.UserRepository;
import com.digitalstamp.util.QrUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@Transactional(readOnly = true)
public class StampService {

    private final UserRepository userRepository;
    private final LoyaltyCardRepository loyaltyCardRepository;
    private final StampTransactionRepository stampTransactionRepository;
    private final ShopService shopService;
    private final EntityMapper mapper;

    public StampService(UserRepository userRepository,
                        LoyaltyCardRepository loyaltyCardRepository,
                        StampTransactionRepository stampTransactionRepository,
                        ShopService shopService,
                        EntityMapper mapper) {
        this.userRepository = userRepository;
        this.loyaltyCardRepository = loyaltyCardRepository;
        this.stampTransactionRepository = stampTransactionRepository;
        this.shopService = shopService;
        this.mapper = mapper;
    }

    public ScanResultResponse scan(User owner, String qrPayload) {
        Shop shop = requireActiveOwnerShop(owner);
        User customer = resolveCustomerFromQr(qrPayload);
        LoyaltyCard card = loyaltyCardRepository.findByCustomerAndShop(customer, shop)
                .orElseThrow(() -> ApiException.badRequest("Customer is not registered with this shop"));
        int required = card.getLoyaltyProgram().getRequiredStamps();
        return ScanResultResponse.builder()
                .customerId(customer.getId())
                .customerName(customer.getName())
                .email(customer.getEmail())
                .mobile(customer.getMobile())
                .loyaltyCardId(card.getId())
                .currentStamps(card.getCurrentStamps())
                .requiredStamps(required)
                .totalStamps(card.getTotalStamps())
                .rewardEligible(card.getCurrentStamps() >= required)
                .shopName(shop.getName())
                .build();
    }

    @Transactional
    public StampTransactionResponse addStamps(User owner, StampActionRequest request) {
        Shop shop = requireActiveOwnerShop(owner);
        LoyaltyCard card = loadCardForOwner(request.getCustomerId(), shop);
        int stamps = request.getStamps() <= 0 ? 1 : request.getStamps();
        card.setCurrentStamps(card.getCurrentStamps() + stamps);
        card.setTotalStamps(card.getTotalStamps() + stamps);
        if (card.getCurrentStamps() >= card.getLoyaltyProgram().getRequiredStamps()) {
            card.setStatus(LoyaltyCardStatus.COMPLETED);
        } else {
            card.setStatus(LoyaltyCardStatus.ACTIVE);
        }
        loyaltyCardRepository.save(card);
        StampTransaction tx = StampTransaction.builder()
                .loyaltyCard(card)
                .customer(card.getCustomer())
                .shop(shop)
                .shopOwner(owner)
                .stampsAdded(stamps)
                .transactionType(StampTransactionType.ADD)
                .description(request.getDescription() != null ? request.getDescription() : "Stamp added")
                .build();
        return mapper.toTx(stampTransactionRepository.save(tx));
    }

    @Transactional
    public StampTransactionResponse reverseStamps(User owner, StampActionRequest request) {
        Shop shop = requireActiveOwnerShop(owner);
        LoyaltyCard card = loadCardForOwner(request.getCustomerId(), shop);
        int stamps = request.getStamps() <= 0 ? 1 : request.getStamps();
        if (card.getCurrentStamps() < stamps) {
            throw ApiException.badRequest("Cannot reverse more stamps than the customer currently has");
        }
        card.setCurrentStamps(card.getCurrentStamps() - stamps);
        card.setTotalStamps(Math.max(0, card.getTotalStamps() - stamps));
        card.setStatus(card.getCurrentStamps() >= card.getLoyaltyProgram().getRequiredStamps()
                ? LoyaltyCardStatus.COMPLETED : LoyaltyCardStatus.ACTIVE);
        loyaltyCardRepository.save(card);
        StampTransaction tx = StampTransaction.builder()
                .loyaltyCard(card)
                .customer(card.getCustomer())
                .shop(shop)
                .shopOwner(owner)
                .stampsAdded(-stamps)
                .transactionType(StampTransactionType.REVERSE)
                .description(request.getDescription() != null ? request.getDescription() : "Stamp reversed")
                .build();
        return mapper.toTx(stampTransactionRepository.save(tx));
    }

    public PagedResponse<StampTransactionResponse> historyForCustomer(Long customerId, Pageable pageable) {
        Page<StampTransaction> page = stampTransactionRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId, pageable);
        return toPage(page);
    }

    public PagedResponse<StampTransactionResponse> historyForShop(Long shopId, Pageable pageable) {
        Page<StampTransaction> page = stampTransactionRepository.findByShop_IdOrderByCreatedAtDesc(shopId, pageable);
        return toPage(page);
    }

    public PagedResponse<StampTransactionResponse> historyAll(Pageable pageable) {
        return toPage(stampTransactionRepository.findAllByOrderByCreatedAtDesc(pageable));
    }

    public PagedResponse<OwnerCustomerRow> shopCustomers(Shop shop, String q, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        Page<LoyaltyCard> page = loyaltyCardRepository.searchShopCustomers(shop.getId(), query, pageable);
        var items = page.getContent().stream().map(card -> {
            OwnerCustomerRow row = new OwnerCustomerRow();
            row.setCustomerId(card.getCustomer().getId());
            row.setName(card.getCustomer().getName());
            row.setEmail(card.getCustomer().getEmail());
            row.setMobile(card.getCustomer().getMobile());
            row.setCurrentStamps(card.getCurrentStamps());
            row.setTotalStamps(card.getTotalStamps());
            row.setStatus(card.getStatus().name());
            row.setLoyaltyCardId(card.getId());
            row.setLastActivity(card.getUpdatedAt() != null
                    ? DateTimeFormatter.ISO_INSTANT.format(card.getUpdatedAt())
                    : null);
            return row;
        }).toList();
        return new PagedResponse<>(items, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    public User resolveCustomerFromQr(String qrPayload) {
        String token = QrUtil.parseToken(qrPayload);
        if (token == null || token.isBlank()) {
            throw ApiException.badRequest("Invalid QR");
        }
        User customer = userRepository.findByQrToken(token)
                .orElseThrow(() -> ApiException.badRequest("Invalid QR"));
        if (customer.getRole() != Role.CUSTOMER) {
            throw ApiException.badRequest("Invalid QR");
        }
        if (!customer.isActive()) {
            throw ApiException.badRequest("Customer account is inactive");
        }
        return customer;
    }

    private LoyaltyCard loadCardForOwner(Long customerId, Shop shop) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> ApiException.notFound("Customer not found"));
        LoyaltyCard card = loyaltyCardRepository.findByCustomerAndShop(customer, shop)
                .orElseThrow(() -> ApiException.badRequest("Customer is not registered with this shop"));
        if (card.getStatus() == LoyaltyCardStatus.INACTIVE) {
            throw ApiException.badRequest("Loyalty card is inactive");
        }
        return card;
    }

    private Shop requireActiveOwnerShop(User owner) {
        if (owner.getRole() != Role.SHOP_OWNER) {
            throw ApiException.forbidden("Only shop owners can issue stamps");
        }
        Shop shop = shopService.requireOwnerShop(owner);
        if (!shop.isActive()) {
            throw ApiException.badRequest("Inactive shop cannot issue new stamps");
        }
        return shop;
    }

    private PagedResponse<StampTransactionResponse> toPage(Page<StampTransaction> page) {
        var items = page.getContent().stream().map(mapper::toTx).toList();
        return new PagedResponse<>(items, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }
}
