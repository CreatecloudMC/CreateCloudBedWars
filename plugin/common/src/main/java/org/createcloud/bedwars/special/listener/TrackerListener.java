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
import org.createcloud.bedwars.api.game.GameStatus;
import org.createcloud.bedwars.events.ApplyPropertyToBoughtItemEventImpl;
import org.createcloud.bedwars.lang.LangKeys;
import org.createcloud.bedwars.player.PlayerManagerImpl;
import org.createcloud.bedwars.utils.MiscUtils;
import org.createcloud.lib.event.OnEvent;
import org.createcloud.lib.event.player.PlayerInteractEvent;
import org.createcloud.lib.lang.Message;
import org.createcloud.lib.tasker.DefaultThreads;
import org.createcloud.lib.tasker.Tasker;
import org.createcloud.lib.tasker.ThreadProperty;
import org.createcloud.lib.utils.annotations.Service;

@Service
public class TrackerListener {
    private static final String TRACKER_PREFIX = "Module:Tracker:";

    @OnEvent
    public void onTrackerRegistered(ApplyPropertyToBoughtItemEventImpl event) {
        if (event.getPropertyName().equalsIgnoreCase("tracker")) {
            event.setStack(ItemUtils.saveData(event.getStack(), TRACKER_PREFIX));
        }
    }

    @OnEvent
    public void onTrackerUse(PlayerInteractEvent event) {
        var player = event.player();
        if (!PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            return;
        }

        var gamePlayer = PlayerManagerImpl.getInstance().getPlayer(player).orElseThrow();
        var game = gamePlayer.getGame();
        if (event.action() == PlayerInteractEvent.Action.RIGHT_CLICK_AIR || event.action() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            if (game != null && game.getStatus() == GameStatus.RUNNING && !gamePlayer.isSpectator()) {
                if (event.item() != null) {
                    var stack = event.item();
                    var unhidden = ItemUtils.getIfStartsWith(stack, TRACKER_PREFIX);
                    if (unhidden != null) {
                        event.cancelled(true);

                        Tasker.run(DefaultThreads.GLOBAL_THREAD, () -> {
                            var target = MiscUtils.findTarget(game, player, Double.MAX_VALUE);
                            if (target != null) {
                                player.setCompassTarget(target.getLocation());

                                int distance = (int) Math.sqrt(player.getLocation().getDistanceSquared(target.getLocation()));
                                MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.SPECIALS_TRACKER_TARGET_FOUND).placeholder("target", target.getDisplayName()).placeholder("distance", distance));
                            } else {
                                MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.SPECIALS_TRACKER_NO_TARGET_FOUND));
                                player.setCompassTarget(game.getTeamOfPlayer(gamePlayer).getRandomSpawn());
                            }
                        });
                    }
                }
            }
        }
    }
}
