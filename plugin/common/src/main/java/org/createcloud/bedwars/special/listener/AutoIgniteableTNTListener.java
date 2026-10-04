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
import org.createcloud.bedwars.events.ApplyPropertyToBoughtItemEventImpl;
import org.createcloud.bedwars.events.PlayerBuildBlockEventImpl;
import org.createcloud.bedwars.player.PlayerManagerImpl;
import org.createcloud.bedwars.utils.MiscUtils;
import org.createcloud.bedwars.special.AutoIgniteableTNTImpl;
import org.createcloud.lib.block.Block;
import org.createcloud.lib.event.OnEvent;
import org.createcloud.lib.event.entity.EntityDamageByEntityEvent;
import org.createcloud.lib.player.Player;
import org.createcloud.lib.utils.annotations.Service;

@Service
public class AutoIgniteableTNTListener {
    private static final String AUTO_IGNITEABLE_TNT_PREFIX = "Module:AutoIgniteableTnt:";

    @OnEvent
    public void onAutoIgniteableTNTRegistered(ApplyPropertyToBoughtItemEventImpl event) {
        if (event.getPropertyName().equalsIgnoreCase("autoigniteabletnt")) {
            event.setStack(ItemUtils.saveData(event.getStack(), applyProperty(event)));
        }
    }

    @OnEvent
    public void onPlace(PlayerBuildBlockEventImpl event) {
        var game = event.getGame();
        var block = event.getBlock();
        var stack = event.getItemInHand();
        var player = event.getPlayer();
        var unhidden = ItemUtils.getIfStartsWith(stack, AUTO_IGNITEABLE_TNT_PREFIX);
        if (unhidden != null) {
            block.block(Block.air());
            var location = block.location().add(0.5, 0.5, 0.5);
            final var propertiesSplit = unhidden.split(":");
            int explosionTime = Integer.parseInt(propertiesSplit[2]);
            boolean damagePlacer = Boolean.parseBoolean(propertiesSplit[3]);
            float damage = (float) Double.parseDouble(propertiesSplit[4]);
            AutoIgniteableTNTImpl special = new AutoIgniteableTNTImpl(game, player, game.getPlayerTeam(player), explosionTime, damagePlacer, damage);
            special.spawn(location);
        }
    }

    @OnEvent
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.entity() instanceof Player)) {
            return;
        }

        var player = (Player) event.entity();

        if (!PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            return;
        }

        var damager = event.damager();
        if (damager.getEntityType().is("minecraft:tnt")) {
            if (player.getUuid().equals(AutoIgniteableTNTImpl.PROTECTED_PLAYERS.get(damager.getEntityId()))) {
                event.cancelled(true);
            }
        }
    }

    private String applyProperty(ApplyPropertyToBoughtItemEventImpl event) {
        return AUTO_IGNITEABLE_TNT_PREFIX
                + MiscUtils.getIntFromProperty("explosion-time", "specials.auto-igniteable-tnt.explosion-time", event)
                + ":" + MiscUtils.getBooleanFromProperty("damage-placer", "specials.auto-igniteable-tnt.damage-placer", event)
                + ":" + MiscUtils.getDoubleFromProperty("damage", "specials.auto-igniteable-tnt.damage", event);
    }
}
