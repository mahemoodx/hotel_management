package com.royalpearl.hotel.menu.repository;

import com.royalpearl.hotel.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    /**
     * Public menu — available items only.
     * Optional filters: category (exact), veg (boolean).
     * Sorted by sort_order asc nulls last, then name.
     */
    List<MenuItem> findAllByAvailableTrueOrderBySortOrderAscNameAsc();

    List<MenuItem> findAllByAvailableTrueAndCategoryOrderBySortOrderAscNameAsc(String category);

    List<MenuItem> findAllByAvailableTrueAndVegOrderBySortOrderAscNameAsc(boolean veg);

    List<MenuItem> findAllByAvailableTrueAndCategoryAndVegOrderBySortOrderAscNameAsc(String category, boolean veg);

    List<String> findDistinctCategoryBy();
}
