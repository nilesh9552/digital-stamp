package com.digitalstamp.config;

import com.digitalstamp.entity.LoyaltyCard;
import com.digitalstamp.entity.LoyaltyCardStatus;
import com.digitalstamp.entity.LoyaltyProgram;
import com.digitalstamp.entity.RedemptionStatus;
import com.digitalstamp.entity.Reward;
import com.digitalstamp.entity.RewardRedemption;
import com.digitalstamp.entity.Role;
import com.digitalstamp.entity.Shop;
import com.digitalstamp.entity.StampTransaction;
import com.digitalstamp.entity.StampTransactionType;
import com.digitalstamp.entity.SystemSetting;
import com.digitalstamp.entity.User;
import com.digitalstamp.repository.LoyaltyCardRepository;
import com.digitalstamp.repository.LoyaltyProgramRepository;
import com.digitalstamp.repository.RewardRedemptionRepository;
import com.digitalstamp.repository.RewardRepository;
import com.digitalstamp.repository.ShopRepository;
import com.digitalstamp.repository.StampTransactionRepository;
import com.digitalstamp.repository.SystemSettingRepository;
import com.digitalstamp.repository.UserRepository;
import com.digitalstamp.util.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final LoyaltyProgramRepository programRepository;
    private final LoyaltyCardRepository cardRepository;
    private final RewardRepository rewardRepository;
    private final StampTransactionRepository txRepository;
    private final RewardRedemptionRepository redemptionRepository;
    private final SystemSettingRepository settingRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AppProperties appProperties,
                      UserRepository userRepository,
                      ShopRepository shopRepository,
                      LoyaltyProgramRepository programRepository,
                      LoyaltyCardRepository cardRepository,
                      RewardRepository rewardRepository,
                      StampTransactionRepository txRepository,
                      RewardRedemptionRepository redemptionRepository,
                      SystemSettingRepository settingRepository,
                      PasswordEncoder passwordEncoder) {
        this.appProperties = appProperties;
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.programRepository = programRepository;
        this.cardRepository = cardRepository;
        this.rewardRepository = rewardRepository;
        this.txRepository = txRepository;
        this.redemptionRepository = redemptionRepository;
        this.settingRepository = settingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!appProperties.isSeed() || userRepository.count() > 0) {
            return;
        }
        log.info("Seeding development data");

        User admin = saveUser("Platform Admin", "admin@digitalstamp.com", "Admin@123", "9990000001", Role.SUPER_ADMIN);
        User owner1 = saveUser("Asha Sharma", "owner@starbucks-pune.com", "Owner@123", "9990001001", Role.SHOP_OWNER);
        User owner2 = saveUser("Rahul Mehta", "owner@cafe-corner.com", "Owner@123", "9990001002", Role.SHOP_OWNER);
        User owner3 = saveUser("Priya Nair", "owner@xyz-cafe.com", "Owner@123", "9990001003", Role.SHOP_OWNER);

        User c1 = saveUser("Neha Patil", "neha@example.com", "Customer@123", "9876500001", Role.CUSTOMER);
        User c2 = saveUser("Amit Joshi", "amit@example.com", "Customer@123", "9876500002", Role.CUSTOMER);
        User c3 = saveUser("Sara Khan", "sara@example.com", "Customer@123", "9876500003", Role.CUSTOMER);
        User c4 = saveUser("Vikram Rao", "vikram@example.com", "Customer@123", "9876500004", Role.CUSTOMER);

        Shop s1 = saveShop("Starbucks Pune", "starbucks-pune", "Flagship coffee lounge in Koregaon Park.",
                "Koregaon Park, Pune", "020-11112222", "hello@starbucks-pune.example", owner1,
                "Mon-Sun 08:00-22:00");
        Shop s2 = saveShop("Cafe Corner", "cafe-corner", "Neighborhood cafe with homemade pastries.",
                "FC Road, Pune", "020-22223333", "hello@cafe-corner.example", owner2,
                "Mon-Sat 09:00-21:00");
        Shop s3 = saveShop("XYZ Cafe", "xyz-cafe", "Specialty pour-over and board games.",
                "Baner, Pune", "020-33334444", "hello@xyz-cafe.example", owner3,
                "Tue-Sun 10:00-23:00");

        LoyaltyProgram p1 = saveProgram(s1, "Starbucks Gold Stamps", "Collect 10 stamps for a free drink.", 10);
        LoyaltyProgram p2 = saveProgram(s2, "Corner Club", "8 stamps unlock a free pastry.", 8);
        LoyaltyProgram p3 = saveProgram(s3, "XYZ Rewards", "12 stamps for a free tasting flight.", 12);

        Reward r1 = saveReward(s1, p1, "Free Grande Latte", "Redeem after 10 stamps", 10);
        saveReward(s1, p1, "Free Cookie", "Redeem after 5 stamps", 5);
        Reward r2 = saveReward(s2, p2, "Free Croissant", "Redeem after 8 stamps", 8);
        saveReward(s3, p3, "Free Tasting Flight", "Redeem after 12 stamps", 12);

        LoyaltyCard card1 = saveCard(c1, s1, p1, 7, 7);
        LoyaltyCard card2 = saveCard(c1, s2, p2, 8, 16);
        saveCard(c2, s1, p1, 3, 3);
        saveCard(c2, s3, p3, 11, 11);
        saveCard(c3, s2, p2, 2, 2);
        saveCard(c4, s1, p1, 10, 20);

        addTx(card1, c1, s1, owner1, 7, StampTransactionType.ADD, "Visit stamps");
        addTx(card2, c1, s2, owner2, 8, StampTransactionType.ADD, "Pastry club stamps");
        addTx(card2, c1, s2, owner2, -8, StampTransactionType.REDEEM_RESET, "Reward redeemed");
        addTx(card2, c1, s2, owner2, 8, StampTransactionType.ADD, "Second cycle");

        RewardRedemption redemption = RewardRedemption.builder()
                .reward(r2)
                .customer(c1)
                .shop(s2)
                .loyaltyCard(card2)
                .status(RedemptionStatus.COMPLETED)
                .redemptionCode("RWD-SEED01")
                .stampsConsumed(8)
                .build();
        redemptionRepository.save(redemption);

        settingRepository.save(SystemSetting.builder()
                .settingKey("platform_name")
                .settingValue("Digital Stamp")
                .description("Public platform name")
                .build());
        settingRepository.save(SystemSetting.builder()
                .settingKey("allow_stamp_reverse")
                .settingValue("true")
                .description("Allow shop owners to reverse stamps")
                .build());

        log.info("Seeded admin={}, owners=3, shops=3, customers=4", admin.getEmail());
    }

    private User saveUser(String name, String email, String password, String mobile, Role role) {
        return userRepository.save(User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .mobile(mobile)
                .role(role)
                .active(true)
                .qrToken(TokenUtil.uuid())
                .build());
    }

    private Shop saveShop(String name, String slug, String description, String address, String phone, String email, User owner, String hours) {
        return shopRepository.save(Shop.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .address(address)
                .phone(phone)
                .email(email)
                .owner(owner)
                .openingHours(hours)
                .active(true)
                .logo(null)
                .build());
    }

    private LoyaltyProgram saveProgram(Shop shop, String name, String description, int required) {
        return programRepository.save(LoyaltyProgram.builder()
                .shop(shop)
                .name(name)
                .description(description)
                .requiredStamps(required)
                .active(true)
                .build());
    }

    private Reward saveReward(Shop shop, LoyaltyProgram program, String name, String description, int required) {
        return rewardRepository.save(Reward.builder()
                .shop(shop)
                .loyaltyProgram(program)
                .name(name)
                .description(description)
                .requiredStamps(required)
                .active(true)
                .build());
    }

    private LoyaltyCard saveCard(User customer, Shop shop, LoyaltyProgram program, int current, int total) {
        LoyaltyCardStatus status = current >= program.getRequiredStamps() ? LoyaltyCardStatus.COMPLETED : LoyaltyCardStatus.ACTIVE;
        return cardRepository.save(LoyaltyCard.builder()
                .customer(customer)
                .shop(shop)
                .loyaltyProgram(program)
                .currentStamps(current)
                .totalStamps(total)
                .status(status)
                .build());
    }

    private void addTx(LoyaltyCard card, User customer, Shop shop, User owner, int stamps, StampTransactionType type, String desc) {
        txRepository.save(StampTransaction.builder()
                .loyaltyCard(card)
                .customer(customer)
                .shop(shop)
                .shopOwner(owner)
                .stampsAdded(stamps)
                .transactionType(type)
                .description(desc)
                .build());
    }
}
