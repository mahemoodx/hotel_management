package com.royalpearl.hotel.menu.service;

import com.royalpearl.hotel.menu.dto.MenuItemDto;
import com.royalpearl.hotel.menu.dto.MenuItemMapper;
import com.royalpearl.hotel.menu.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final MenuItemMapper     menuItemMapper;

    /**
     * Returns available menu items.
     *
     * @param category filter by category (exact, case-sensitive) – null means all
     * @param veg      filter by veg flag – null means all
     */
    public List<MenuItemDto> getMenu(String category, Boolean veg) {
        if (category != null && veg != null) {
            return menuItemMapper.toDtoList(
                    menuItemRepository
                            .findAllByAvailableTrueAndCategoryAndVegOrderBySortOrderAscNameAsc(category, veg));
        }
        if (category != null) {
            return menuItemMapper.toDtoList(
                    menuItemRepository
                            .findAllByAvailableTrueAndCategoryOrderBySortOrderAscNameAsc(category));
        }
        if (veg != null) {
            return menuItemMapper.toDtoList(
                    menuItemRepository
                            .findAllByAvailableTrueAndVegOrderBySortOrderAscNameAsc(veg));
        }
        return menuItemMapper.toDtoList(
                menuItemRepository
                        .findAllByAvailableTrueOrderBySortOrderAscNameAsc());
    }
}
