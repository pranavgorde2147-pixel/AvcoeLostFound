package com.avcoe.lostfound.config;

import com.avcoe.lostfound.entity.*;
import com.avcoe.lostfound.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CampusLocationRepository locationRepository;
    private final ItemRepository itemRepository;
    private final ClaimRepository claimRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, CampusLocationRepository locationRepository,
                      ItemRepository itemRepository, ClaimRepository claimRepository,
                      ConversationRepository conversationRepository, MessageRepository messageRepository,
                      NotificationRepository notificationRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.itemRepository = itemRepository;
        this.claimRepository = claimRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // Create users
        User finder = User.builder()
                .email("finder@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Rahul Patil")
                .role(User.Role.STUDENT)
                .department("Computer Engineering")
                .rollNumber("CE-2021-001")
                .enabled(true)
                .build();
        finder = userRepository.save(finder);

        User claimant1 = User.builder()
                .email("claimant1@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Priya Sharma")
                .role(User.Role.STUDENT)
                .department("Information Technology")
                .rollNumber("IT-2021-002")
                .enabled(true)
                .build();
        claimant1 = userRepository.save(claimant1);

        User claimant2 = User.builder()
                .email("claimant2@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Amit Kumar")
                .role(User.Role.STUDENT)
                .department("Electronics Engineering")
                .rollNumber("EC-2021-003")
                .enabled(true)
                .build();
        claimant2 = userRepository.save(claimant2);

        User admin = User.builder()
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .fullName("Dr. Suresh Deshmukh")
                .role(User.Role.ADMIN)
                .department("Administration")
                .enabled(true)
                .build();
        admin = userRepository.save(admin);

        User faculty = User.builder()
                .email("faculty@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Prof. Sunita Jadhav")
                .role(User.Role.FACULTY)
                .department("Computer Engineering")
                .enabled(true)
                .build();
        faculty = userRepository.save(faculty);

        // Create locations
        List<CampusLocation> locations = new ArrayList<>();
        String[] locationNames = {
            "Main Gate", "Library", "Canteen", "Academic Block", "Computer Lab",
            "Electronics Lab", "Mechanical Lab", "Hostel", "Sports Ground",
            "Auditorium", "Parking", "Department Office", "Seminar Hall",
            "Staff Room", "Placement Cell"
        };
        for (String name : locationNames) {
            CampusLocation loc = CampusLocation.builder().name(name).active(true).build();
            locations.add(locationRepository.save(loc));
        }

        // Create found items
        Item backpack = Item.builder()
                .itemName("Black Backpack")
                .itemType(Item.ItemType.FOUND)
                .category(Item.ItemCategory.BAGS)
                .description("A black backpack with a laptop compartment. Found near the library entrance. Contains some books and a pencil case.")
                .dateOfEvent(LocalDateTime.now().minusDays(1))
                .approximateTime("3:30 PM")
                .location(locations.get(1)) // Library
                .status(Item.ItemStatus.ACTIVE)
                .reporter(finder)
                .verificationQuestion("What is inside the front pocket?")
                .currentItemLocation("Library Reception Desk")
                .conditionDescription("Good condition, slightly worn")
                .build();
        backpack = itemRepository.save(backpack);

        Item calculator = Item.builder()
                .itemName("Casio FX-991EX Calculator")
                .itemType(Item.ItemType.FOUND)
                .category(Item.ItemCategory.ELECTRONICS)
                .description("A scientific calculator found in the computer lab. Has a small sticker on the back.")
                .dateOfEvent(LocalDateTime.now().minusHours(5))
                .approximateTime("11:00 AM")
                .location(locations.get(4)) // Computer Lab
                .status(Item.ItemStatus.ACTIVE)
                .reporter(finder)
                .verificationQuestion("What sticker is on the back of the calculator?")
                .currentItemLocation("Computer Lab Reception")
                .conditionDescription("Working condition")
                .build();
        calculator = itemRepository.save(calculator);

        Item keys = Item.builder()
                .itemName("Bunch of Keys")
                .itemType(Item.ItemType.FOUND)
                .category(Item.ItemCategory.KEYS)
                .description("A bunch of 4 keys on a blue keychain with a small teddy bear charm.")
                .dateOfEvent(LocalDateTime.now().minusDays(2))
                .approximateTime("9:00 AM")
                .location(locations.get(0)) // Main Gate
                .status(Item.ItemStatus.ACTIVE)
                .reporter(finder)
                .verificationQuestion("How many keys are on the keychain?")
                .currentItemLocation("Security Office at Main Gate")
                .conditionDescription("Good")
                .build();
        keys = itemRepository.save(keys);

        // Create lost items
        Item lostPhone = Item.builder()
                .itemName("Samsung Galaxy S23")
                .itemType(Item.ItemType.LOST)
                .category(Item.ItemCategory.ELECTRONICS)
                .description("Lost my phone somewhere between the canteen and academic block. It has a blue case with a pop socket.")
                .dateOfEvent(LocalDateTime.now().minusDays(1))
                .approximateTime("12:30 PM")
                .location(locations.get(2)) // Canteen
                .status(Item.ItemStatus.ACTIVE)
                .reporter(claimant1)
                .identifyingCharacteristics("Blue case with pop socket, screen protector")
                .build();
        lostPhone = itemRepository.save(lostPhone);

        Item lostWallet = Item.builder()
                .itemName("Brown Leather Wallet")
                .itemType(Item.ItemType.LOST)
                .category(Item.ItemCategory.WALLETS)
                .description("Lost my wallet near the seminar hall. Contains student ID and some cash.")
                .dateOfEvent(LocalDateTime.now().minusDays(3))
                .approximateTime("2:00 PM")
                .location(locations.get(12)) // Seminar Hall
                .status(Item.ItemStatus.ACTIVE)
                .reporter(claimant2)
                .identifyingCharacteristics("Brown leather with embossed initials AK")
                .build();
        lostWallet = itemRepository.save(lostWallet);

        // Create claim for backpack
        Claim claim = Claim.builder()
                .item(backpack)
                .claimant(claimant1)
                .claimMessage("I think this is my backpack! I lost it yesterday after library study session. It has my notebooks inside.")
                .status(Claim.ClaimStatus.VERIFICATION_IN_PROGRESS)
                .build();
        claim = claimRepository.save(claim);

        Conversation conversation = Conversation.builder()
                .claim(claim)
                .item(backpack)
                .participants(new HashSet<>(Arrays.asList(finder, claimant1)))
                .createdBy(claimant1)
                .title("Claim: Black Backpack")
                .active(true)
                .lastMessageAt(LocalDateTime.now().minusHours(1))
                .build();
        conversation = conversationRepository.save(conversation);
        claim.setConversation(conversation);
        claimRepository.save(claim);

        // Send verification question
        Message verificationMsg = Message.builder()
                .conversation(conversation)
                .sender(finder)
                .content("🔐 Ownership Verification\n\nTo help verify ownership, please answer:\n\n\"What is inside the front pocket?\"")
                .messageType(Message.MessageType.VERIFICATION_QUESTION)
                .read(false)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();
        messageRepository.save(verificationMsg);

        // Claimant answers
        Message answerMsg = Message.builder()
                .conversation(conversation)
                .sender(claimant1)
                .content("The front pocket has my calculator and a pen drive.")
                .messageType(Message.MessageType.TEXT)
                .read(true)
                .createdAt(LocalDateTime.now().minusMinutes(45))
                .build();
        messageRepository.save(answerMsg);

        // Finder replies
        Message replyMsg = Message.builder()
                .conversation(conversation)
                .sender(finder)
                .content("That matches! I can see a calculator and a pen drive in the front pocket. Let's arrange a handover.")
                .messageType(Message.MessageType.TEXT)
                .read(true)
                .createdAt(LocalDateTime.now().minusMinutes(30))
                .build();
        messageRepository.save(replyMsg);

        conversation.setLastMessageAt(LocalDateTime.now().minusMinutes(30));
        conversationRepository.save(conversation);

        // Create notifications
        Notification claimNotification = Notification.builder()
                .user(finder)
                .title("New Claim on Your Found Item")
                .message("Priya Sharma thinks your found \"Black Backpack\" is theirs.")
                .type(Notification.NotificationType.CLAIM_RECEIVED)
                .referenceId(claim.getId())
                .referenceType("CLAIM")
                .read(true)
                .build();
        notificationRepository.save(claimNotification);

        Notification messageNotification = Notification.builder()
                .user(claimant1)
                .title("Verification Question")
                .message("Rahul Patil sent you a verification question for \"Black Backpack\".")
                .type(Notification.NotificationType.VERIFICATION_SENT)
                .referenceId(conversation.getId())
                .referenceType("CONVERSATION")
                .read(true)
                .build();
        notificationRepository.save(messageNotification);

        System.out.println("=== Demo Data Seeded ===");
        System.out.println("Accounts created:");
        System.out.println("  finder@example.com / password123");
        System.out.println("  claimant1@example.com / password123");
        System.out.println("  claimant2@example.com / password123");
        System.out.println("  admin@example.com / admin123");
        System.out.println("  faculty@example.com / password123");
        System.out.println("========================");
    }
}
