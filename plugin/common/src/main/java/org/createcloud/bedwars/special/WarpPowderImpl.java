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

package org.createcloud.bedwars.special;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.createcloud.bedwars.api.special.WarpPowder;
import org.createcloud.bedwars.game.GameImpl;
import org.createcloud.bedwars.game.TeamImpl;
import org.createcloud.bedwars.lang.LangKeys;
import org.createcloud.bedwars.player.BedWarsPlayer;
import org.createcloud.bedwars.utils.SpawnEffects;
import org.createcloud.lib.lang.Message;
import org.createcloud.lib.item.ItemStack;
import org.createcloud.lib.item.builder.ItemStackFactory;
import org.createcloud.lib.tasker.DefaultThreads;
import org.createcloud.lib.tasker.Tasker;
import org.createcloud.lib.tasker.TaskerTime;
import org.createcloud.lib.tasker.task.Task;

@Getter
@EqualsAndHashCode(callSuper = true)
public class WarpPowderImpl extends SpecialItemImpl implements WarpPowder {
    private final ItemStack item;
    private Task teleportingTask;
    private int teleportingTime;

    public WarpPowderImpl(GameImpl game, BedWarsPlayer player, TeamImpl team, ItemStack item, int teleportingTime) {
        super(game, player, team);
        this.item = item;
        this.teleportingTime = teleportingTime;
    }

    @Override
    public void cancelTeleport(boolean showCancelMessage) {
        try {
            teleportingTask.cancel();
        } catch (Exception ignored) {
        }

        game.unregisterSpecialItem(this);

        if (showCancelMessage) {
            Message.of(LangKeys.SPECIALS_WARP_POWDER_CANCELED)
                    .prefixOrDefault(game.getCustomPrefixComponent())
                    .send(player);
        }
    }

    @Override
    public void runTask() {
        game.registerSpecialItem(this);

        Message.of(LangKeys.SPECIALS_WARP_POWDER_STARTED)
                .prefixOrDefault(game.getCustomPrefixComponent())
                .placeholder("time", teleportingTime)
                .send(player);

        teleportingTask = Tasker.runRepeatedly(DefaultThreads.GLOBAL_THREAD, () -> {
            if (teleportingTime == 0) {
                cancelTeleport(false);
                var stack = item.withAmount(1);
                try {
                    if (player.getPlayerInventory().getItemInOffHand().equals(stack)) {
                        player.getPlayerInventory().setItemInOffHand(ItemStackFactory.getAir());
                    } else {
                        player.getPlayerInventory().removeItem(stack);
                    }
                } catch (Throwable e) {
                    player.getPlayerInventory().removeItem(stack);
                }
                player.forceUpdateInventory();
                player.teleport(team.getRandomSpawn());
            } else {
                SpawnEffects.spawnEffect(game, player, "game-effects.warppowdertick");
                teleportingTime--;
            }
        }, 20, TaskerTime.TICKS);
    }
}
