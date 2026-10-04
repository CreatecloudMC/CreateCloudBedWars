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
import org.jetbrains.annotations.NotNull;
import org.createcloud.bedwars.api.events.UpgradeLevelChangedEvent;
import org.createcloud.bedwars.game.GameImpl;
import org.createcloud.bedwars.game.upgrade.UpgradableImpl;
import org.createcloud.bedwars.game.upgrade.UpgradeImpl;
import org.createcloud.lib.event.Event;

@Data
@AllArgsConstructor
public class UpgradeLevelChangedEventImpl implements UpgradeLevelChangedEvent, Event {
    private final @NotNull GameImpl game;
    private final @NotNull UpgradableImpl upgradable;
    private final @NotNull String name;
    private final @NotNull UpgradeImpl upgrade;
    private final double oldLevel;

    @Override
    public double getNewLevel() {
        return this.upgrade.getLevel();
    }
}
