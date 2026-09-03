package com.avcoe.lostfound.repository;

import com.avcoe.lostfound.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {
    List<ItemImage> findByItemIdOrderBySortOrderAsc(Long itemId);
    List<ItemImage> findByItemIdAndIsPrimaryTrue(Long itemId);
}
