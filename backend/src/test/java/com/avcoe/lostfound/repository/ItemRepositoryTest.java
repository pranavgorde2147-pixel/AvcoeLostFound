package com.avcoe.lostfound.repository;

import com.avcoe.lostfound.entity.CampusLocation;
import com.avcoe.lostfound.entity.Item;
import com.avcoe.lostfound.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CampusLocationRepository locationRepository;

    @Test
    void findMatchingItemsAppliesTypeCategorySearchAndLocationTogether() {
        User reporter = userRepository.save(User.builder()
                .email("reporter@example.com")
                .password("encoded-password")
                .fullName("Test Reporter")
                .role(User.Role.STUDENT)
                .enabled(true)
                .build());
        CampusLocation library = locationRepository.save(CampusLocation.builder()
                .name("Library")
                .active(true)
                .build());
        CampusLocation canteen = locationRepository.save(CampusLocation.builder()
                .name("Canteen")
                .active(true)
                .build());

        saveItem(reporter, library, "Black Backpack", Item.ItemType.FOUND, Item.ItemCategory.BAGS);
        saveItem(reporter, canteen, "Black Backpack", Item.ItemType.FOUND, Item.ItemCategory.BAGS);
        saveItem(reporter, library, "Black Backpack", Item.ItemType.LOST, Item.ItemCategory.BAGS);
        saveItem(reporter, library, "Black Backpack", Item.ItemType.FOUND, Item.ItemCategory.OTHER);

        Page<Item> results = itemRepository.findMatchingItems(
                Item.ItemType.FOUND,
                Item.ItemCategory.BAGS,
                Item.ItemStatus.ACTIVE,
                "backpack",
                "library",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));

        assertThat(results.getTotalElements()).isEqualTo(1);
        assertThat(results.getContent()).extracting(Item::getItemName).containsExactly("Black Backpack");
    }

    private void saveItem(User reporter, CampusLocation location, String name,
                          Item.ItemType type, Item.ItemCategory category) {
        itemRepository.save(Item.builder()
                .itemName(name)
                .itemType(type)
                .category(category)
                .dateOfEvent(LocalDateTime.now())
                .location(location)
                .status(Item.ItemStatus.ACTIVE)
                .reporter(reporter)
                .build());
    }
}
