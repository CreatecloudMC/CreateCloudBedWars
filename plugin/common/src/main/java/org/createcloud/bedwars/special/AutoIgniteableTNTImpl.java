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
import org.createcloud.bedwars.api.special.AutoIgniteableTNT;
import org.createcloud.bedwars.entities.EntitiesManagerImpl;
import org.createcloud.bedwars.game.GameImpl;
import org.createcloud.bedwars.game.TeamImpl;
import org.createcloud.bedwars.player.BedWarsPlayer;
import org.createcloud.lib.api.types.server.LocationHolder;
import org.createcloud.lib.entity.PrimedTnt;
import org.createcloud.lib.entity.type.EntityType;
import org.createcloud.lib.tasker.DefaultThreads;
import org.createcloud.lib.tasker.Tasker;
import org.createcloud.lib.tasker.TaskerTime;
import org.createcloud.lib.world.Location;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@EqualsAndHashCode(callSuper = true)
public class AutoIgniteableTNTImpl extends SpecialItemImpl implements AutoIgniteableTNT {

    public static final Map<Integer, UUID> PROTECTED_PLAYERS = new ConcurrentHashMap<>();

    private final int explosionTime;
    private final float damage;
    private final boolean allowedDamagingPlacer;

    public AutoIgniteableTNTImpl(GameImpl game, BedWarsPlayer player, TeamImpl team, int explosionTime, boolean damagePlacer, float damage) {
        super(game, player, team);
        this.explosionTime = explosionTime;
        this.allowedDamagingPlacer = damagePlacer;
        this.damage = damage;
    }

    @Override
    public void spawn(LocationHolder location) {
        spawn(location.as(Location.class));
    }

    public void spawn(Location location) {
        var tnt = Objects.requireNonNull(EntityType.of("tnt").spawn(location, en -> {
            var tnt1 = (PrimedTnt) en;
            tnt1.yield(damage);
            tnt1.fuseTicks(explosionTime * 20);
            if (!allowedDamagingPlacer) {
                PROTECTED_PLAYERS.put(tnt1.getEntityId(), player.getUuid());
            }
        }));
        EntitiesManagerImpl.getInstance().addEntityToGame(tnt, game);
        Tasker.runDelayed(DefaultThreads.GLOBAL_THREAD, () -> {
                    EntitiesManagerImpl.getInstance().removeEntityFromGame(tnt);
                    AutoIgniteableTNTImpl.PROTECTED_PLAYERS.remove(tnt.getEntityId());
                }, explosionTime + 10, TaskerTime.TICKS);
    }

}
