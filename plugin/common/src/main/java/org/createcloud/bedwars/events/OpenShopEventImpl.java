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

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.createcloud.bedwars.api.events.OpenShopEvent;
import org.createcloud.bedwars.game.GameImpl;
import org.createcloud.bedwars.game.GameStoreImpl;
import org.createcloud.bedwars.player.BedWarsPlayer;
import org.createcloud.lib.entity.Entity;
import org.createcloud.lib.event.Event;

@Data
public class OpenShopEventImpl implements OpenShopEvent, Event {
    private final GameImpl game;
    @Nullable
    private final Entity entity;
    private final BedWarsPlayer player;
    private final StoreLike gameStore;
    @NotNull
    private OpenShopEvent.Result result = OpenShopEvent.Result.ALLOW;
}
