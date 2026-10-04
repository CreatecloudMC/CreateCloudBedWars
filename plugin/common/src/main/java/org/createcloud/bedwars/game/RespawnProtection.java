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

package org.createcloud.bedwars.game;

import lombok.Data;
import org.createcloud.bedwars.api.config.GameConfigurationContainer;
import org.createcloud.bedwars.lang.LangKeys;
import org.createcloud.bedwars.player.BedWarsPlayer;
import org.createcloud.bedwars.utils.MiscUtils;
import org.createcloud.lib.lang.Message;
import org.createcloud.lib.tasker.Tasker;
import org.createcloud.lib.tasker.TaskerTime;
import org.createcloud.lib.tasker.task.Task;

@Data
public class RespawnProtection implements Runnable {
    private final GameImpl game;
    private final BedWarsPlayer player;
    private final int seconds;
    private int length;
    private boolean running;
    private Task task;

    @Override
    public void run() {
    	if (!running) return;
        if (length > 0) {
            if (game.getConfigurationContainer().getOrDefault(GameConfigurationContainer.RESPAWN_SHOW_MESSAGES, false)) {
                MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.IN_GAME_RESPAWN_PROTECTION_REMAINING).placeholder("time", this.length));
            }
        }
        if (length <= 0) {
            if (game.getConfigurationContainer().getOrDefault(GameConfigurationContainer.RESPAWN_SHOW_MESSAGES, false)) {
                MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.IN_GAME_RESPAWN_PROTECTION_END));
            }
            game.removeProtectedPlayer(player);
            running = false;
        }
        length--;
    }

    public void runProtection() {
        if (!running) {
            running = true;
            this.task = Tasker.runAsyncDelayedAndRepeatedly(this, 5, TaskerTime.TICKS, 20, TaskerTime.TICKS);
        }
    }
}

