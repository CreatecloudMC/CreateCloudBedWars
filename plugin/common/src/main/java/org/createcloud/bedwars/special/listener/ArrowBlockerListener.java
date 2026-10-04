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
import org.createcloud.bedwars.special.ArrowBlockerImpl;
import org.createcloud.bedwars.utils.DelayFactoryImpl;
import org.createcloud.bedwars.utils.MiscUtils;
import org.createcloud.lib.event.EventExecutionOrder;
import org.createcloud.lib.event.OnEvent;
import org.createcloud.lib.event.entity.EntityDamageEvent;
import org.createcloud.lib.event.player.PlayerInteractEvent;
import org.createcloud.lib.lang.Message;
import org.createcloud.lib.player.Player;
import org.createcloud.lib.utils.annotations.Service;

@Service
public class ArrowBlockerListener {
    private static final String ARROW_BLOCKER_PREFIX = "Module:ArrowBlocker:";

    @OnEvent
    public void onArrowBlockerRegistered(ApplyPropertyToBoughtItemEventImpl event) {
        if (event.getPropertyName().equalsIgnoreCase("arrowblocker")) {
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
                var unhidden = ItemUtils.getIfStartsWith(stack, ARROW_BLOCKER_PREFIX);

                if (unhidden != null) {
                    if (!game.isDelayActive(gPlayer, ArrowBlockerImpl.class)) {
                        event.cancelled(true);

                        final var propertiesSplit = unhidden.split(":");
                        int protectionTime = Integer.parseInt(propertiesSplit[2]);
                        int delay = Integer.parseInt(propertiesSplit[3]);
                        var arrowBlocker = new ArrowBlockerImpl(game, gPlayer, game.getPlayerTeam(gPlayer), stack, protectionTime);

                        if (arrowBlocker.isActivated()) {
                            player.sendMessage(Message.of(LangKeys.SPECIALS_ARROW_BLOCKER_ALREADY_ACTIVATED).prefixOrDefault(game.getCustomPrefixComponent()));
                            return;
                        }

                        if (delay > 0) {
                            var delayFactory = new DelayFactoryImpl(delay, arrowBlocker, gPlayer, game);
                            game.registerDelay(delayFactory);
                        }

                        arrowBlocker.activate();
                    } else {
                        event.cancelled(true);

                        int delay = game.getActiveDelay(gPlayer, ArrowBlockerImpl.class).getRemainDelay();
                        MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.SPECIALS_ITEM_DELAY).placeholder("time", delay));
                    }
                }
            }
        }
    }

    @OnEvent(order = EventExecutionOrder.LATE)
    public void onDamage(EntityDamageEvent event) {
        var entity = event.entity();
        if (!(entity instanceof Player)) {
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

        var arrowBlocker = game.getFirstActiveSpecialItemOfPlayer(gPlayer, ArrowBlockerImpl.class);
        if (arrowBlocker != null && event.damageCause().is("PROJECTILE")) {
            event.cancelled(true);
        }
    }

    private String applyProperty(ApplyPropertyToBoughtItemEventImpl event) {
        return ARROW_BLOCKER_PREFIX
                + MiscUtils.getIntFromProperty(
                "protection-time", "specials.arrow-blocker.protection-time", event) + ":"
                + MiscUtils.getIntFromProperty(
                "delay", "specials.arrow-blocker.delay", event);
    }
}
