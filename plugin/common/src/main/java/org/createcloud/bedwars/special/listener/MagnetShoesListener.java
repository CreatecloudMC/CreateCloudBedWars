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
import org.createcloud.bedwars.player.PlayerManagerImpl;
import org.createcloud.bedwars.utils.MiscUtils;
import org.createcloud.lib.event.OnEvent;
import org.createcloud.lib.event.entity.EntityDamageEvent;
import org.createcloud.lib.player.Player;
import org.createcloud.lib.utils.annotations.Service;

@Service
public class MagnetShoesListener {
    // Class for special item is not needed in this case (so this special item is not registered in game)
    private static final String MAGNET_SHOES_PREFIX = "Module:MagnetShoes:";

    @OnEvent
    public void onMagnetShoesRegistered(ApplyPropertyToBoughtItemEventImpl event) {
        if (event.getPropertyName().equalsIgnoreCase("magnetshoes")) {
            int probability = MiscUtils.getIntFromProperty("probability", "specials.magnet-shoes.probability", event);

            event.setStack(ItemUtils.saveData(event.getStack(), MAGNET_SHOES_PREFIX + probability));
        }
    }

    @OnEvent
    public void onDamage(EntityDamageEvent event) {
        if (event.cancelled() || !(event.entity() instanceof Player)) {
            return;
        }

        var player = (Player) event.entity();
        if (PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            var boots = player.getPlayerInventory().getBoots();
            if (boots != null) {
                String magnetShoes = ItemUtils.getIfStartsWith(boots, MAGNET_SHOES_PREFIX);
                if (magnetShoes != null) {
                    int probability = Integer.parseInt(magnetShoes.split(":")[2]);
                    int randInt = MiscUtils.randInt(0, 100);
                    if (randInt <= probability) {
                        event.cancelled(true);
                        player.damage(event.damage());
                    }
                }
            }
        }
    }
}
