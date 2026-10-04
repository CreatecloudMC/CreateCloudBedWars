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

package org.createcloud.bedwars;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.createcloud.lib.block.BlockPlacement;
import org.createcloud.lib.block.snapshot.BlockSnapshot;
import org.createcloud.lib.event.player.PlayerBlockBreakEvent;
import org.createcloud.lib.event.player.PlayerBlockPlaceEvent;
import org.createcloud.lib.item.ItemStack;
import org.createcloud.lib.player.Player;
import org.createcloud.lib.plugin.ServiceManager;
import org.createcloud.lib.sender.CommandSender;
import org.createcloud.lib.utils.annotations.AbstractService;
import org.createcloud.lib.world.Location;

@AbstractService("org.createcloud.bedwars.{platform}.{Platform}PlatformService")
public abstract class PlatformService {

    public static PlatformService getInstance() {
        return ServiceManager.get(PlatformService.class);
    }

    public abstract void reloadPlugin(@NotNull CommandSender sender);

    public abstract void spawnEffect(@NotNull Location location, @NotNull String value);

    @NotNull
    public abstract PlayerBlockPlaceEvent fireFakeBlockPlaceEvent(@NotNull BlockPlacement block, @NotNull BlockSnapshot originalState, @NotNull BlockPlacement clickedBlock, @NotNull ItemStack item, @NotNull Player player, boolean canBuild);

    @NotNull
    public abstract PlayerBlockBreakEvent fireFakeBlockBreakEvent(@NotNull BlockPlacement block, @NotNull Player player);

    public abstract @Nullable Object savePlatformScoreboard(@NotNull Player player);

    public abstract void restorePlatformScoreboard(@NotNull Player player, @NotNull Object scoreboard);
}
