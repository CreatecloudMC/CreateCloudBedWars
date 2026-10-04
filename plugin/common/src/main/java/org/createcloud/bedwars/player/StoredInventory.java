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

package org.createcloud.bedwars.player;

import lombok.Getter;
import lombok.Setter;
import org.createcloud.lib.item.ItemStack;
import org.createcloud.lib.item.meta.PotionEffect;
import org.createcloud.lib.player.gamemode.GameMode;
import org.createcloud.lib.spectator.Component;
import org.createcloud.lib.world.Location;

import java.util.Collection;

@Getter
@Setter
public class StoredInventory {
    private ItemStack[] armor;
    private Component displayName;
    private Collection<PotionEffect> effects;
    private int foodLevel = 0;
    private ItemStack[] inventory;
    private Location leftLocation;
    private int level = 0;
    private Component listName;
    private GameMode mode;
    private float xp;
    private Object platformScoreboard;
    private double health = 20.0D;
    private double maxHealth = 20.0D;
}
