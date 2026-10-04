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

package org.createcloud.bedwars.api.events;

import org.jetbrains.annotations.ApiStatus;
import org.createcloud.bedwars.api.BedwarsAPI;
import org.createcloud.bedwars.api.game.LocalGame;

import java.util.function.Consumer;

@ApiStatus.NonExtendable
public interface GameChangedStatusEvent {
    LocalGame getGame();

    static void handle(Object plugin, Consumer<GameChangedStatusEvent> consumer) {
        BedwarsAPI.getInstance().getEventUtils().handle(plugin, GameChangedStatusEvent.class, consumer);
    }
}
