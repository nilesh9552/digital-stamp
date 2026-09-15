package com.digitalstamp.service;

import com.digitalstamp.dto.PagedResponse;
import com.digitalstamp.dto.reward.RedemptionResponse;
import com.digitalstamp.dto.reward.RewardRequest;
import com.digitalstamp.dto.reward.RewardResponse;
import com.digitalstamp.entity.LoyaltyCard;
import com.digitalstamp.entity.LoyaltyCardStatus;
import com.digitalstamp.entity.RedemptionStatus;
import com.digitalstamp.entity.Reward;
import com.digitalstamp.entity.RewardRedemption;
import com.digitalstamp.entity.Shop;
import com.digitalstamp.entity.StampTransaction;
import com.digitalstamp.entity.StampTransactionType;
import com.digitalstamp.entity.User;
import com.digitalstamp.exception.ApiException;
import com.digitalstamp.mapper.EntityMapper;
import com.digitalstamp.entity.LoyaltyProgram;
import com.digitalstamp.repository.LoyaltyCardRepository;
import com.digitalstamp.repository.LoyaltyProgramRepository;
import com.digitalstamp.repository.RewardRedemptionRepository;
import com.digitalstamp.repository.RewardRepository;
import com.digitalstamp.repository.StampTransactionRepository;
import com.digitalstamp.util.TokenUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RewardService {

    private final RewardRepository rewardRepository;
    private final RewardRedemptionRepository redemptionRepository;
    private final LoyaltyCardRepository loyaltyCardRepository;
    private final StampTransactionRepository stampTransactionRepository;
    private final LoyaltyProgramRepository loyaltyProgramRepository;
    private final EntityMapper mapper;

    public RewardService(RewardRepository rewardRepository,
                         RewardRedemptionRepository redemptionRepository,
                         LoyaltyCardRepository loyaltyCardRepository,
                         StampTransactionRepository stampTransactionRepository,
                         LoyaltyProgramRepository loyaltyProgramRepository,
                         EntityMapper mapper) {
        this.rewardRepository = rewardRepository;
        this.redemptionRepository = redemptionRepository;
        this.loyaltyCardRepository = loyaltyCardRepository;
        this.stampTransactionRepository = stampTransactionRepository;
        this.loyaltyProgramRepository = loyaltyProgramRepository;
        this.mapper = mapper;
    }

    public List<RewardResponse> listForShop(Shop shop) {
        return rewardRepository.findByShop_IdOrderByCreatedAtDesc(shop.getId()).stream()
                .map(r -> mapper.toReward(r, false))
                .toList();
    }

    public List<RewardResponse> publicForShop(Long shopId) {
        return rewardRepository.findByShop_IdAndActiveTrue(shopId).stream()
                .map(r -> mapper.toReward(r, false))
                .toList();
    }

    public List<RewardResponse> customerRewards(User customer) {
        return loyaltyCardRepository.findDetailedByCustomerId(customer.getId()).stream()
                .flatMap(card -> rewardRepository.findByShop_IdAndActiveTrue(card.getShop().getId()).stream()
                        .map(reward -> mapper.toReward(reward, card.getCurrentStamps() >= reward.getRequiredStamps())))
                .toList();
    }

    @Transactional
    public RewardResponse create(Shop shop, RewardRequest request) {
        LoyaltyProgram program = loyaltyProgramRepository.findByShop(shop)
                .orElseThrow(() -> ApiException.notFound("Loyalty program not found"));
        Reward reward = Reward.builder()
                .shop(shop)
                .loyaltyProgram(program)
                .name(request.getName())
                .description(request.getDescription())
                .requiredStamps(request.getRequiredStamps())
                .active(request.getActive() == null || request.getActive())
                .build();
        return mapper.toReward(rewardRepository.save(reward), false);
    }

    @Transactional
    public RewardResponse update(Shop shop, Long rewardId, RewardRequest request) {
        Reward reward = ownedReward(shop, rewardId);
        reward.setName(request.getName());
        reward.setDescription(request.getDescription());
        reward.setRequiredStamps(request.getRequiredStamps());
        if (request.getActive() != null) {
            reward.setActive(request.getActive());
        }
        return mapper.toReward(rewardRepository.save(reward), false);
    }

    @Transactional
    public void delete(Shop shop, Long rewardId) {
        Reward reward = ownedReward(shop, rewardId);
        rewardRepository.delete(reward);
    }

    @Transactional
    public RedemptionResponse redeem(User customer, Long rewardId) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> ApiException.notFound("Reward not available"));
        if (!reward.isActive()) {
            throw ApiException.badRequest("Reward not available");
        }
        Shop shop = reward.getShop();
        if (!shop.isActive()) {
            throw ApiException.badRequest("Inactive shop");
        }
        LoyaltyCard card = loyaltyCardRepository.findByCustomerAndShop(customer, shop)
                .orElseThrow(() -> ApiException.badRequest("Customer is not registered with this shop"));
        if (card.getCurrentStamps() < reward.getRequiredStamps()) {
            throw ApiException.badRequest("Insufficient stamps");
        }
        int consumed = reward.getRequiredStamps();
        card.setCurrentStamps(card.getCurrentStamps() - consumed);
        card.setStatus(card.getCurrentStamps() >= card.getLoyaltyProgram().getRequiredStamps()
                ? LoyaltyCardStatus.COMPLETED : LoyaltyCardStatus.ACTIVE);
        loyaltyCardRepository.save(card);

        String code;
        do {
            code = TokenUtil.redemptionCode();
        } while (redemptionRepository.existsByRedemptionCode(code));

        RewardRedemption redemption = RewardRedemption.builder()
                .reward(reward)
                .customer(customer)
                .shop(shop)
                .loyaltyCard(card)
                .status(RedemptionStatus.COMPLETED)
                .redemptionCode(code)
                .stampsConsumed(consumed)
                .build();
        redemption = redemptionRepository.save(redemption);

        StampTransaction tx = StampTransaction.builder()
                .loyaltyCard(card)
                .customer(customer)
                .shop(shop)
                .shopOwner(shop.getOwner())
                .stampsAdded(-consumed)
                .transactionType(StampTransactionType.REDEEM_RESET)
                .description("Reward redeemed: " + reward.getName() + " (" + code + ")")
                .build();
        stampTransactionRepository.save(tx);
        return mapper.toRedemption(redemption);
    }

    public PagedResponse<RedemptionResponse> customerHistory(Long customerId, Pageable pageable) {
        return toPage(redemptionRepository.findByCustomer_IdOrderByRedeemedAtDesc(customerId, pageable));
    }

    public PagedResponse<RedemptionResponse> shopHistory(Long shopId, Pageable pageable) {
        return toPage(redemptionRepository.findByShop_IdOrderByRedeemedAtDesc(shopId, pageable));
    }

    public PagedResponse<RedemptionResponse> allHistory(Pageable pageable) {
        return toPage(redemptionRepository.findAllByOrderByRedeemedAtDesc(pageable));
    }

    private Reward ownedReward(Shop shop, Long rewardId) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> ApiException.notFound("Reward not available"));
        if (!reward.getShop().getId().equals(shop.getId())) {
            throw ApiException.forbidden("Unauthorized shop access");
        }
        return reward;
    }

    private PagedResponse<RedemptionResponse> toPage(Page<RewardRedemption> page) {
        var items = page.getContent().stream().map(mapper::toRedemption).toList();
        return new PagedResponse<>(items, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }
}
