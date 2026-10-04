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
import org.createcloud.bedwars.api.special.LuckyBlock;
import org.createcloud.bedwars.game.GameImpl;
import org.createcloud.bedwars.game.TeamImpl;
import org.createcloud.bedwars.player.BedWarsPlayer;
import org.createcloud.lib.entity.Entities;
import org.createcloud.lib.entity.PrimedTnt;
import org.createcloud.lib.item.builder.ItemStackFactory;
import org.createcloud.lib.item.meta.PotionEffect;
import org.createcloud.lib.player.Player;
import org.createcloud.lib.spectator.Component;
import org.createcloud.lib.tasker.DefaultThreads;
import org.createcloud.lib.tasker.Tasker;
import org.createcloud.lib.tasker.TaskerTime;
import org.createcloud.lib.world.Location;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

@Getter
@EqualsAndHashCode(callSuper = true)
public class LuckyBlockImpl extends SpecialItemImpl implements LuckyBlock {
    private final List<Map<String, Object>> luckyBlockData;
    private Location blockLocation;
    private boolean placed;

    public LuckyBlockImpl(GameImpl game, BedWarsPlayer player, TeamImpl team, List<Map<String, Object>> luckyBlockData) {
        super(game, player, team);
        this.luckyBlockData = luckyBlockData;
        game.registerSpecialItem(this);
    }

    public void place(Location loc) {
        this.blockLocation = loc;
        this.placed = true;
    }

    public void process(Player broker) {
        game.unregisterSpecialItem(this);

        var rand = new Random();
        var element = rand.nextInt(luckyBlockData.size());

        var map = luckyBlockData.get(element);

        var type = (String) map.getOrDefault("type", "nothing");
        switch (type) {
            case "item":
                var stack = Objects.requireNonNull(ItemStackFactory.build(map.get("stack")));
                Entities.dropItem(stack, blockLocation);
                break;
            case "potion":
                var potionEffect = PotionEffect.of(map.get("effect"));
                broker.addPotionEffect(potionEffect);
                break;
            case "tnt":
                Tasker.runDelayed(DefaultThreads.GLOBAL_THREAD, () -> {
                    Entities.spawn("tnt", blockLocation, tnt -> {
                        ((PrimedTnt) tnt).fuseTicks(0);
                    });
                }, 10, TaskerTime.TICKS);
                break;
            case "teleport":
                broker.teleport(broker.getLocation().add(0, (int) map.get("height"), 0));
                break;
        }

        if (map.containsKey("message")) {
            broker.sendMessage(Component.fromLegacy((String) map.get("message")));
        }
    }
}
