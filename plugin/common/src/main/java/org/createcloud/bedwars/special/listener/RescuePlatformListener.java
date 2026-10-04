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

import org.createcloud.bedwars.api.game.GameStatus;
import org.createcloud.bedwars.events.ApplyPropertyToBoughtItemEventImpl;
import org.createcloud.bedwars.lang.LangKeys;
import org.createcloud.bedwars.player.PlayerManagerImpl;
import org.createcloud.bedwars.special.RescuePlatformImpl;
import org.createcloud.bedwars.utils.DelayFactoryImpl;
import org.createcloud.bedwars.utils.ItemUtils;
import org.createcloud.bedwars.utils.MiscUtils;
import org.createcloud.lib.event.OnEvent;
import org.createcloud.lib.event.entity.EntityDamageEvent;
import org.createcloud.lib.event.player.PlayerBlockBreakEvent;
import org.createcloud.lib.event.player.PlayerInteractEvent;
import org.createcloud.lib.lang.Message;
import org.createcloud.lib.player.Player;
import org.createcloud.lib.utils.BlockFace;
import org.createcloud.lib.utils.annotations.Service;

@Service
public class RescuePlatformListener {
    public static final String RESCUE_PLATFORM_PREFIX = "Module:RescuePlatform:";

    @OnEvent
    public void onRescuePlatformRegistered(ApplyPropertyToBoughtItemEventImpl event) {
        if (event.getPropertyName().equalsIgnoreCase("rescueplatform")) {
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
            if (game != null && game.getStatus() == GameStatus.RUNNING && !gPlayer.isSpectator() && event.item() != null) {
                var stack = event.item();
                var unhidden = ItemUtils.getIfStartsWith(stack, RESCUE_PLATFORM_PREFIX);

                if (unhidden != null) {
                    if (!game.isDelayActive(gPlayer, RescuePlatformImpl.class)) {
                        event.cancelled(true);

                        var propertiesSplit = unhidden.split(":");
                        var isBreakable = Boolean.parseBoolean(propertiesSplit[2]);
                        var delay = Integer.parseInt(propertiesSplit[3]);
                        var breakTime = Integer.parseInt(propertiesSplit[4]);
                        var distance = Integer.parseInt(propertiesSplit[5]);
                        var result = MiscUtils.getBlockTypeFromString(propertiesSplit[6], "GLASS");

                        var rescuePlatform = new RescuePlatformImpl(game, gPlayer, game.getPlayerTeam(gPlayer), stack);

                        if (!player.getLocation().add(BlockFace.DOWN).getBlock().block().isAir()) {
                            MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.SPECIALS_RESCUE_PLATFORM_NOT_IN_AIR).placeholder("time", delay));
                            return;
                        }

                        if (delay > 0) {
                            var delayFactory = new DelayFactoryImpl(delay, rescuePlatform, gPlayer, game);
                            game.registerDelay(delayFactory);
                        }

                        rescuePlatform.createPlatform(isBreakable, breakTime, distance, result);
                    } else {
                        event.cancelled(true);

                        var delay = game.getActiveDelay(gPlayer, RescuePlatformImpl.class).getRemainDelay();
                        MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.SPECIALS_ITEM_DELAY).placeholder("time", delay));
                    }
                }
            }
        }
    }

    @OnEvent
    public void onFallDamage(EntityDamageEvent event) {
        var entity = event.entity();
        if (event.cancelled() || !(entity instanceof Player)) {
            return;
        }

        var player = (Player) entity;
        if (!PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            return;
        }

        var gPlayer = PlayerManagerImpl.getInstance().getPlayer(player).orElseThrow();
        var game = gPlayer.getGame();
        if (gPlayer.isSpectator() || game == null) {
            return;
        }

        var rescuePlatform = game.getFirstActiveSpecialItemOfPlayer(gPlayer, RescuePlatformImpl.class);
        if (rescuePlatform != null && event.damageCause().is("FALL")) {
            var block = player.getLocation().add(BlockFace.DOWN).getBlock();
            if (block.block().isSameType(rescuePlatform.getMaterial())) {
                event.cancelled(true);
            }
        }
    }

    @OnEvent
    public void onBlockBreak(PlayerBlockBreakEvent event) {
        var player = event.player();
        if (!PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            return;
        }

        var gPlayer = PlayerManagerImpl.getInstance().getPlayer(player).orElseThrow();
        var game = gPlayer.getGame();
        if (gPlayer.isSpectator() || game == null) {
            return;
        }

        var block = event.block();
        for (var checkedPlatform : game.getActiveSpecialItemsOfPlayer(gPlayer, RescuePlatformImpl.class)) {
            if (checkedPlatform != null) {
                for (var platformBlock : checkedPlatform.getPlatformBlocks()) {
                    if (platformBlock.equals(block) && !checkedPlatform.isBreakable()) {
                        event.cancelled(true);
                    }
                }
            }
        }
    }

    private String applyProperty(ApplyPropertyToBoughtItemEventImpl event) {
        return RESCUE_PLATFORM_PREFIX
                + MiscUtils.getBooleanFromProperty(
                "is-breakable", "specials.rescue-platform.is-breakable", event) + ":"
                + MiscUtils.getIntFromProperty(
                "delay", "specials.rescue-platform.delay", event) + ":"
                + MiscUtils.getIntFromProperty(
                "break-time", "specials.rescue-platform.break-time", event) + ":"
                + MiscUtils.getIntFromProperty(
                "distance", "specials.rescue-platform.distance", event) + ":"
                + MiscUtils.getMaterialFromProperty(
                "material", "specials.rescue-platform.material", event);
    }
}
