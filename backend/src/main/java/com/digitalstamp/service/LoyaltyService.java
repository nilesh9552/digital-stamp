package com.digitalstamp.service;

import com.digitalstamp.dto.loyalty.LoyaltyCardResponse;
import com.digitalstamp.entity.LoyaltyCard;
import com.digitalstamp.entity.User;
import com.digitalstamp.mapper.EntityMapper;
import com.digitalstamp.repository.LoyaltyCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LoyaltyService {

    private final ShopService shopService;
    private final LoyaltyCardRepository loyaltyCardRepository;
    private final EntityMapper mapper;

    public LoyaltyService(ShopService shopService, LoyaltyCardRepository loyaltyCardRepository, EntityMapper mapper) {
        this.shopService = shopService;
        this.loyaltyCardRepository = loyaltyCardRepository;
        this.mapper = mapper;
    }

    public List<LoyaltyCardResponse> cardsFor(User customer) {
        return loyaltyCardRepository.findDetailedByCustomerId(customer.getId()).stream()
                .map(mapper::toCard)
                .toList();
    }

    @Transactional
    public LoyaltyCardResponse enrollCustomer(User customer, String shopSlug) {
        return mapper.toCard(shopService.enrollInternal(customer, shopSlug));
    }
}
