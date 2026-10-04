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
import org.createcloud.bedwars.player.BedWarsPlayer;
import org.createcloud.bedwars.player.PlayerManagerImpl;
import org.createcloud.bedwars.special.WarpPowderImpl;
import org.createcloud.bedwars.utils.DelayFactoryImpl;
import org.createcloud.bedwars.utils.MiscUtils;
import org.createcloud.lib.event.OnEvent;
import org.createcloud.lib.event.entity.EntityDamageEvent;
import org.createcloud.lib.event.player.PlayerDropItemEvent;
import org.createcloud.lib.event.player.PlayerInteractEvent;
import org.createcloud.lib.event.player.PlayerInventoryClickEvent;
import org.createcloud.lib.event.player.PlayerInventoryDragEvent;
import org.createcloud.lib.event.player.PlayerMoveEvent;
import org.createcloud.lib.lang.Message;
import org.createcloud.lib.player.Player;
import org.createcloud.lib.utils.BlockFace;
import org.createcloud.lib.utils.annotations.Service;

@Service
public class WarpPowderListener {
    private static final String WARP_POWDER_PREFIX = "Module:WarpPowder:";

    @OnEvent
    public void onPowderItemRegister(ApplyPropertyToBoughtItemEventImpl event) {
        if (event.getPropertyName().equalsIgnoreCase("warppowder")) {
            event.setStack(ItemUtils.saveData(event.getStack(), applyProperty(event)));
        }
    }

    @OnEvent
    public void onPlayerUseItem(PlayerInteractEvent event) {
        var player = event.player();
        if (!PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            return;
        }

        var gPlayer = PlayerManagerImpl.getInstance().getPlayer(player).orElseThrow();
        var game = gPlayer.getGame();
        if (event.action() == PlayerInteractEvent.Action.RIGHT_CLICK_AIR || event.action() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            if (game != null && game.getStatus() == GameStatus.RUNNING && !gPlayer.isSpectator()) {
                if (event.item() != null) {
                    var stack = event.item();
                    var unhidden = ItemUtils.getIfStartsWith(stack, WARP_POWDER_PREFIX);

                    if (unhidden != null) {
                        event.cancelled(true);
                        if (!game.isDelayActive(gPlayer, WarpPowderImpl.class)) {
                            var propertiesSplit = unhidden.split(":");
                            int teleportTime = Integer.parseInt(propertiesSplit[2]);
                            int delay = Integer.parseInt(propertiesSplit[3]);
                            var warpPowder = new WarpPowderImpl(game, gPlayer, game.getPlayerTeam(gPlayer), stack, teleportTime);

                            if (event.player().getLocation().add(BlockFace.DOWN).getBlock().block().isAir()) {
                                return;
                            }

                            if (delay > 0) {
                                var delayFactory = new DelayFactoryImpl(delay, warpPowder, gPlayer, game);
                                game.registerDelay(delayFactory);
                            }

                            warpPowder.runTask();
                        } else {
                            int delay = game.getActiveDelay(gPlayer, WarpPowderImpl.class).getRemainDelay();
                            MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.SPECIALS_ITEM_DELAY).placeholder("time", delay));
                        }
                    }
                }
            }
        }
    }


    @OnEvent
    public void onDamage(EntityDamageEvent event) {
        if (event.cancelled() || !(event.entity() instanceof Player)) {
            return;
        }

        var player = (Player) event.entity();

        if (!PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            return;
        }

        var gPlayer = PlayerManagerImpl.getInstance().getPlayer(player).orElseThrow();
        var game = gPlayer.getGame();

        if (gPlayer.isSpectator() || game == null) {
            return;
        }

        var warpPowder = game.getFirstActiveSpecialItemOfPlayer(gPlayer, WarpPowderImpl.class);
        if (warpPowder != null) {
            warpPowder.cancelTeleport(true);
        }
    }

    @OnEvent
    public void onMove(PlayerMoveEvent event) {
        var player = event.player();
        if (event.cancelled() || !PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            return;
        }

        if (event.currentLocation().getX() == event.newLocation().getX()
                && event.currentLocation().getY() == event.newLocation().getY()
                && event.currentLocation().getZ() == event.newLocation().getZ()) {
            return;
        }

        var gPlayer = player.as(BedWarsPlayer.class);
        var game = gPlayer.getGame();
        if (gPlayer.isSpectator() || game == null) {
            return;
        }

        var warpPowder = game.getFirstActiveSpecialItemOfPlayer(gPlayer, WarpPowderImpl.class);
        if (warpPowder != null) {
            warpPowder.cancelTeleport(true);
        }
    }

    @OnEvent
    public void onWarpPowderDrop(PlayerDropItemEvent event) {
        if (!PlayerManagerImpl.getInstance().isPlayerInGame(event.player())) {
            return;
        }

        var unhidden = ItemUtils.getIfStartsWith(event.itemDrop().getItem(), WARP_POWDER_PREFIX);
        if (unhidden != null) {
            var bwPlayer = event.player().as(BedWarsPlayer.class);
            var warpPowder = bwPlayer.getGame().getFirstActiveSpecialItemOfPlayer(bwPlayer, WarpPowderImpl.class);
            if (warpPowder != null) {
                warpPowder.cancelTeleport(true);
            }
        }
    }

    @OnEvent
    public void onWarpPowderMove(PlayerInventoryClickEvent event) {
        if (!PlayerManagerImpl.getInstance().isPlayerInGame(event.player())) {
            return;
        }

        var item = event.currentItem();

        if (item == null) {
            return;
        }

        var unhidden = ItemUtils.getIfStartsWith(item, WARP_POWDER_PREFIX);
        if (unhidden != null) {
            var bwPlayer = event.player().as(BedWarsPlayer.class);
            var warpPowder = bwPlayer.getGame().getFirstActiveSpecialItemOfPlayer(bwPlayer, WarpPowderImpl.class);
            if (warpPowder != null) {
                warpPowder.cancelTeleport(true);
            }
        }
    }

    @OnEvent
    public void onWarpPowderDrag(PlayerInventoryDragEvent event) {
        if (!PlayerManagerImpl.getInstance().isPlayerInGame(event.player())) {
            return;
        }

        var item = event.oldCursorItem();

        var unhidden = ItemUtils.getIfStartsWith(item, WARP_POWDER_PREFIX);
        if (unhidden != null) {
            var bwPlayer = event.player().as(BedWarsPlayer.class);
            var warpPowder = bwPlayer.getGame().getFirstActiveSpecialItemOfPlayer(bwPlayer, WarpPowderImpl.class);
            if (warpPowder != null) {
                warpPowder.cancelTeleport(true);
            }
        }
    }

    private String applyProperty(ApplyPropertyToBoughtItemEventImpl event) {
        return WARP_POWDER_PREFIX
                + MiscUtils.getIntFromProperty(
                "teleport-time", "specials.warp-powder.teleport-time", event) + ":"
                + MiscUtils.getIntFromProperty(
                "delay", "specials.warp-powder.delay", event);
    }
}
