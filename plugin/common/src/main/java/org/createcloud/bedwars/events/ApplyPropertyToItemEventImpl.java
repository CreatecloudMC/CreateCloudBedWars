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

package org.createcloud.bedwars.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.createcloud.bedwars.api.events.ApplyPropertyToItemEvent;
import org.createcloud.bedwars.game.GameImpl;
import org.createcloud.bedwars.player.BedWarsPlayer;
import org.createcloud.lib.api.types.server.ItemStackHolder;
import org.createcloud.lib.event.Event;
import org.createcloud.lib.item.ItemStack;

import java.util.Map;

@Data
@AllArgsConstructor
public class ApplyPropertyToItemEventImpl implements ApplyPropertyToItemEvent, Event {
    private final GameImpl game;
    private final BedWarsPlayer player;
    private final String propertyName;
    private final Map<String, Object> properties;
    private ItemStack stack;

    @Override
    public void setStack(ItemStackHolder stack) {
        this.stack = stack.as(ItemStack.class);
    }
}
