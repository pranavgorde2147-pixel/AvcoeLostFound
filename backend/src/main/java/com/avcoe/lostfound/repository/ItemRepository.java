package com.avcoe.lostfound.repository;

import com.avcoe.lostfound.entity.Item;
import com.avcoe.lostfound.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("""
           SELECT i FROM Item i
           LEFT JOIN i.location location
           WHERE (:type IS NULL OR i.itemType = :type)
             AND (:category IS NULL OR i.category = :category)
             AND (:status IS NULL OR i.status = :status)
             AND (:search IS NULL OR
                  LOWER(i.itemName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                  LOWER(COALESCE(i.description, '')) LIKE LOWER(CONCAT('%', :search, '%')))
             AND (:location IS NULL OR
                  LOWER(COALESCE(location.name, i.locationDescription, '')) LIKE
                  LOWER(CONCAT('%', :location, '%')))
           """)
    Page<Item> findMatchingItems(
            @Param("type") Item.ItemType type,
            @Param("category") Item.ItemCategory category,
            @Param("status") Item.ItemStatus status,
            @Param("search") String search,
            @Param("location") String location,
            Pageable pageable);

    Page<Item> findByItemTypeAndStatus(Item.ItemType itemType, Item.ItemStatus status, Pageable pageable);
    Page<Item> findByReporter(User reporter, Pageable pageable);
    Page<Item> findByItemType(Item.ItemType itemType, Pageable pageable);
    Page<Item> findByStatus(Item.ItemStatus status, Pageable pageable);
    Page<Item> findByCategory(Item.ItemCategory category, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.status = 'ACTIVE' AND " +
           "(LOWER(i.itemName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Item> searchActiveItems(@Param("query") String query, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.itemType = :type AND i.status = 'ACTIVE' AND " +
           "(LOWER(i.itemName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Item> searchActiveItemsByType(@Param("query") String query, @Param("type") Item.ItemType type, Pageable pageable);

    long countByItemTypeAndStatus(Item.ItemType itemType, Item.ItemStatus status);
    long countByReporter(User reporter);
    long countByStatus(Item.ItemStatus status);

    @Query("SELECT i FROM Item i WHERE i.itemType = 'FOUND' AND i.status = 'ACTIVE' " +
           "AND LOWER(i.itemName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Item> findPossibleMatches(@Param("name") String name);
}
