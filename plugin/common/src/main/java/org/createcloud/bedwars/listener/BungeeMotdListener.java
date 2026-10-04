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

package org.createcloud.bedwars.listener;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.createcloud.bedwars.api.game.Game;
import org.createcloud.bedwars.config.MainConfig;
import org.createcloud.bedwars.game.GameImpl;
import org.createcloud.bedwars.game.GameManagerImpl;
import org.createcloud.lib.event.OnEvent;
import org.createcloud.lib.event.server.ServerListPingEvent;
import org.createcloud.lib.spectator.Component;
import org.createcloud.lib.utils.annotations.Service;
import org.createcloud.lib.utils.annotations.methods.ShouldRunControllable;

@Service
@RequiredArgsConstructor
public class BungeeMotdListener {
    private final GameManagerImpl gameManager;
    private final MainConfig mainConfig;

    @ShouldRunControllable
    public boolean isEnabled() {
        return mainConfig.node("bungee", "enabled").getBoolean() && mainConfig.node("bungee", "motd", "enabled").getBoolean();
    }

    @OnEvent
    public void onServerListPing(@NotNull ServerListPingEvent slpe) {
        var games = gameManager.getLocalGames();
        if (games.isEmpty()) {
            return;
        }

        Game gameA = null;
        if (gameManager.isDoGamePreselection()) {
            gameA = gameManager.getPreselectedGame();
        }
        if (gameA == null) {
            if (mainConfig.node("bungee", "random-game-selection", "enabled").getBoolean()) {
                gameA = gameManager.getGameWithHighestPlayers().orElse(null);

                if (gameA == null) {
                    gameA = games.get(0); // seems like there are no waiting games, let's just show one of the game in running state
                }
            } else {
                gameA = games.get(0);
            }
        }

        if (!(gameA instanceof GameImpl)) {
            return;
        }

        var game = (GameImpl) gameA;

        String string = null;

        switch (game.getStatus()) {
            case DISABLED:
                string = mainConfig.node("bungee", "motd", "disabled").getString();
                break;
            case GAME_END_CELEBRATING:
            case RUNNING:
                string = mainConfig.node("bungee", "motd", "running").getString();
                break;
            case REBUILDING:
                string = mainConfig.node("bungee", "motd", "rebuilding").getString();
                break;
            case WAITING:
                if (game.countPlayers() >= game.getMaxPlayers()) {
                    string = mainConfig.node("bungee", "motd", "waiting_full").getString();
                } else {
                    string = mainConfig.node("bungee", "motd", "waiting").getString();
                }
                break;
        }

        if (string == null) {
            return; // WTF??
        }

        // TODO: migrate the config to minimessage
        slpe.description(Component.fromLegacy(
                string.replace("%name%", game.getName()).replace("%displayName%", game.getDisplayName() != null ? Component.fromMiniMessage(game.getDisplayName()).toLegacy() : game.getName()).replace("%current%", Integer.toString(game.countPlayers())).replace("%max%", Integer.toString(game.getMaxPlayers()))
        ));
    }
}
