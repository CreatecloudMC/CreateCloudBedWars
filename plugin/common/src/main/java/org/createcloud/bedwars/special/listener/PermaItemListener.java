/*
 * Copyright (C) 2025 CreateCloud
 *
 * This file is part of Screaming BedWars.
 *
 * Screaming BedWars is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Screaming BedWars is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Screaming BedWars. If not, see <https://www.gnu.org/licenses/>.
 */

package org.createcloud.bedwars.special.listener;

import org.createcloud.bedwars.utils.ItemUtils;
import org.createcloud.bedwars.events.ApplyPropertyToBoughtItemEventImpl;
import org.createcloud.bedwars.lib.debug.Debug;
import org.createcloud.bedwars.player.PlayerManagerImpl;
import org.createcloud.lib.container.PlayerContainer;
import org.createcloud.lib.event.OnEvent;
import org.createcloud.lib.event.player.PlayerDropItemEvent;
import org.createcloud.lib.event.player.PlayerInventoryClickEvent;
import org.createcloud.lib.utils.InventoryAction;
import org.createcloud.lib.utils.annotations.Service;

import java.util.List;

@Service
public class PermaItemListener {
    private static final String PERMA_ITEM_PREFIX = "Module:PermaItem:";
    private static final String PERMA_ITEM_PROPERTY_KEY = "lose-upon-death";
    private static final List<InventoryAction> blockedInventoryActions = List.of(
            InventoryAction.DROP_ALL_CURSOR,
            InventoryAction.DROP_ALL_SLOT,
            InventoryAction.DROP_ONE_SLOT,
            InventoryAction.DROP_ONE_CURSOR,
            InventoryAction.MOVE_TO_OTHER_INVENTORY
    );

    @OnEvent
    public void onItemRegister(ApplyPropertyToBoughtItemEventImpl event) {
        var gamePlayer = event.getPlayer();
        if (event.hasProperty(PERMA_ITEM_PROPERTY_KEY)) {
            if (!event.getBooleanProperty(PERMA_ITEM_PROPERTY_KEY)) {
                var stack = event.getStack();

                if (stack.getMaterial().maxStackSize() > 1) {
                    Debug.warn(String.format("Item [%s] can be stacked and players will lose this item upon dying. Remove the 'lose-upon-death' flag for this item", stack), true);
                    return;
                }

                event.setStack(ItemUtils.saveData(stack, PERMA_ITEM_PREFIX));
                gamePlayer.addPermanentItem(event.getStack());
                event.setStack(stack);
            }
        }
    }

    @OnEvent
    public void onItemRemoval(PlayerInventoryClickEvent event) {
        var player = event.player();
        if (!PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            return;
        }

        if (event.inventory() != null) { // TODO: what were these two ifs supposed to do? (inventory() is never null)
            if (!(event.inventory() instanceof PlayerContainer)) {
                return;
            }
        }

        var cursorItem = event.cursorItem();
        var slotItem = event.currentItem();
        var action = event.action();

        String cursorItemUnhashedProp = null;
        String slotItemUnhashedProp = null;

        if (cursorItem != null) {
            cursorItemUnhashedProp = ItemUtils.getIfStartsWith(cursorItem, PERMA_ITEM_PREFIX);
        }

        if (slotItem != null) {
            slotItemUnhashedProp = ItemUtils.getIfStartsWith(slotItem, PERMA_ITEM_PREFIX);
        }

        if ((cursorItemUnhashedProp != null || slotItemUnhashedProp != null) && blockedInventoryActions.contains(action)) {
            event.cancelled(true);
        }
    }

    @OnEvent
    public void onItemDrop(PlayerDropItemEvent event) {
        if (!PlayerManagerImpl.getInstance().isPlayerInGame(event.player())) {
            return;
        }

        if (ItemUtils.getIfStartsWith(event.itemDrop().getItem(), PERMA_ITEM_PREFIX) != null) {
            event.cancelled(true);
        }
    }

    public static String getPermItemPropKey() {
        return PERMA_ITEM_PROPERTY_KEY;
    }
}
