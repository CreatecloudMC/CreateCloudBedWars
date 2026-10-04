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
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.createcloud.bedwars.api.events.UpgradeLevelChangeEvent;
import org.createcloud.bedwars.game.GameImpl;
import org.createcloud.bedwars.game.upgrade.UpgradableImpl;
import org.createcloud.bedwars.game.upgrade.UpgradeImpl;
import org.createcloud.lib.event.CancellableEvent;

@Data
@RequiredArgsConstructor
public class UpgradeLevelChangeEventImpl implements UpgradeLevelChangeEvent, CancellableEvent {
    private final @NotNull GameImpl game;
    private final @NotNull UpgradableImpl upgradable;
    private final @NotNull String name;
    private final @NotNull UpgradeImpl upgrade;
    private final double oldLevel;
    private final double originallyRequestedNewLevel;
    private boolean cancelled;

    @Override
    public boolean cancelled() {
        return cancelled;
    }

    @Override
    public void cancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public double getNewLevel() {
        return this.upgrade.getLevel();
    }

    @Override
    public void setNewLevel(double newLevel) {
        this.upgrade.setLevel(newLevel);
    }
}
