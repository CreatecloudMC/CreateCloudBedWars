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

package org.createcloud.bedwars.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.createcloud.bedwars.api.entities.EntitiesManager;
import org.createcloud.bedwars.api.game.GameManager;
import org.createcloud.bedwars.api.game.GroupManager;
import org.createcloud.bedwars.api.game.ItemSpawnerType;
import org.createcloud.bedwars.api.game.StoreManager;
import org.createcloud.bedwars.api.player.PlayerManager;
import org.createcloud.bedwars.api.statistics.PlayerStatisticsManager;
import org.createcloud.bedwars.api.utils.ColorChanger;
import org.createcloud.bedwars.api.utils.EventUtils;
import org.createcloud.bedwars.api.variants.VariantManager;

import java.util.List;

/**
 * <p>The BedWars API main entry point.</p>
 *
 * @author CreateCloud
 */
@ApiStatus.NonExtendable
public interface BedwarsAPI {
    /**
     * <p>Retrieves the game manager instance.</p>
     *
     * @return the game manager instance
     */
    GameManager getGameManager();

    /**
     * <p>Retrieves the group manager instance.</p>
     *
     * @return the group manager instance
     */
    GroupManager getGroupManager();

    /**
     * <p>Retrieves the variant manager instance.</p>
     *
     * @return the variant manager instance
     */
    VariantManager getVariantManager();

    /**
     * <p>Retrieves the player manager instance.</p>
     *
     * @return the player manager instance
     */
    PlayerManager getPlayerManager();

    /**
     * <p>Retrieves the entities manager instance.</p>
     *
     * @return the entities manager instance
     */
    EntitiesManager getEntitiesManager();

    /**
     * <p>Retrieves the store manager instance.</p>
     *
     * @return the store manager instance
     */
    @NotNull StoreManager getStoreManager();

    /**
     * @return Event utils used for registering handlers for bedwars' events
     */
    EventUtils getEventUtils();

    /**
     * <p>Retrieves a {@link List} of available item spawner types.</p>
     *
     * @return a {@link List} of item spawner types
     */
    List<? extends ItemSpawnerType> getItemSpawnerTypes();

    /**
     * @param name Name of item spawner type
     * @return boolean Is spawner type registered
     */
    boolean isItemSpawnerTypeRegistered(String name);

    /**
     * @param name Name of item spawner type
     * @return ItemSpawnerType by name or null if type isn't exists
     */
    ItemSpawnerType getItemSpawnerTypeByName(String name);

    /**
     * @return String of Bedwars Version
     */
    String getPluginVersion();

    /**
     * @return Color changer for coloring ItemStacks
     */
    ColorChanger getColorChanger();

    /**
     *
     * @return hub server name from config
     */
    String getHubServerName();

    /**
     *
     * @return PlayerStatisticsManager if statistics are enabled; otherwise null
     */
    PlayerStatisticsManager getStatisticsManager();

    /**
     * @return Bedwars instance
     */
    static BedwarsAPI getInstance() {
        return Internal.bedwarsAPI;
    }

    /**
     * @hidden
     */
    @ApiStatus.Internal
    class Internal {
        protected static BedwarsAPI bedwarsAPI;

        public static void setBedWarsAPI(BedwarsAPI bedwarsAPI) {
            if (Internal.bedwarsAPI != null) {
                throw new UnsupportedOperationException("Already initialized!");
            }
            Internal.bedwarsAPI = bedwarsAPI;
        }
    }
}
