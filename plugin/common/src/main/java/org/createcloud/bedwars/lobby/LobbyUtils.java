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

package org.createcloud.bedwars.lobby;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.createcloud.bedwars.VersionInfo;
import org.createcloud.bedwars.config.MainConfig;
import org.createcloud.bedwars.statistics.PlayerStatisticManager;
import org.createcloud.bedwars.utils.MiscUtils;
import org.createcloud.lib.Server;
import org.createcloud.lib.lang.Message;
import org.createcloud.lib.player.Player;
import org.createcloud.lib.spectator.Color;
import org.createcloud.lib.spectator.Component;

@UtilityClass
public class LobbyUtils {
    @Contract("_ -> param1")
    @NotNull
    public Message setupPlaceholders(@NotNull Message message) {
        message
                .placeholder("date", MiscUtils.getFormattedDate(MainConfig.getInstance().node("main-lobby", "date-format").getString("MM/dd/yy")))
                .placeholder("players", () -> Server.getConnectedPlayers().size())
                .placeholder("name", sender -> sender instanceof Player ? ((Player) sender).getDisplayName() : Component.text(sender.getName()))
                .placeholder("version", VersionInfo.VERSION);

        if (PlayerStatisticManager.isEnabled()) {
            var playerStatisticManager = PlayerStatisticManager.getInstance();

            message.placeholder("goal", sender -> {
                if (sender instanceof Player) {
                    return Component.text(MiscUtils.roundForMainLobbySidebar(playerStatisticManager.getStatistic((Player) sender).getNeededScoreToNextLevel()));
                }
                return Component.text(0); // how
            }).placeholder("current-progress", sender -> {
                if (sender instanceof Player) {
                    return Component.text(MiscUtils.roundForMainLobbySidebar(playerStatisticManager.getStatistic((Player) sender).getScoreSincePreviousLevel()));
                }
                return Component.text(0); // how
            }).placeholder("level", sender -> {
                if (sender instanceof Player) {
                    return Component.text(playerStatisticManager.getStatistic((Player) sender).getLevel() + "\u272B", Color.GRAY); // TODO: use prestiges when they are implemented
                }
                return Component.text(0); // how
            }).placeholder("level-number", sender -> {
                if (sender instanceof Player) {
                    return Component.text(playerStatisticManager.getStatistic((Player) sender).getLevel());
                }
                return Component.text(0); // how
            }).placeholder("progress-bar", sender -> {
                if (sender instanceof Player) {
                    var statistic = playerStatisticManager.getStatistic((Player) sender);
                    var progress = statistic.getScoreSincePreviousLevel();
                    var needed = statistic.getNeededScoreToNextLevel();
                    if (needed <= 0) { // invalid
                        return Component.text().content("[").append(Component.text("\u25A0".repeat(10) + "]", Color.GRAY)).color(Color.DARK_GRAY).append("]").build();
                    }
                    int numberOfBoxesFilled = (int) (((double) progress / needed) * 10);
                    return Component.text()
                            .content("[")
                            .color(Color.DARK_GRAY)
                            .append(Component.text("\u25A0".repeat(numberOfBoxesFilled), Color.AQUA))
                            .append(Component.text("\u25A0".repeat(10 - numberOfBoxesFilled), Color.GRAY))
                            .append("]")
                            .build();
                }
                return Component.text(0); // how
            }).placeholder("kills", sender -> {
                if (sender instanceof Player) {
                    return Component.text(playerStatisticManager.getStatistic((Player) sender).getKills());
                }
                return Component.text(0); // how
            }).placeholder("deaths", sender -> {
                if (sender instanceof Player) {
                    return Component.text(playerStatisticManager.getStatistic((Player) sender).getDeaths());
                }
                return Component.text(0); // how
            }).placeholder("target-blocks-destroyed", sender -> {
                if (sender instanceof Player) {
                    return Component.text(playerStatisticManager.getStatistic((Player) sender).getDestroyedBeds());
                }
                return Component.text(0); // how
            }).placeholder("loses", sender -> {
                if (sender instanceof Player) {
                    return Component.text(playerStatisticManager.getStatistic((Player) sender).getLoses());
                }
                return Component.text(0); // how
            }).placeholder("wins", sender -> {
                if (sender instanceof Player) {
                    return Component.text(playerStatisticManager.getStatistic((Player) sender).getWins());
                }
                return Component.text(0); // how
            }).placeholder("score", sender -> {
                if (sender instanceof Player) {
                    return Component.text(playerStatisticManager.getStatistic((Player) sender).getScore());
                }
                return Component.text(0); // how
            }).placeholder("kd-ratio", sender -> {
                if (sender instanceof Player) {
                    return Component.text(playerStatisticManager.getStatistic((Player) sender).getKD());
                }
                return Component.text(0); // how
            }).placeholder("played-games", sender -> {
                if (sender instanceof Player) {
                    return Component.text(playerStatisticManager.getStatistic((Player) sender).getGames());
                }
                return Component.text(0); // how
            });
        }

        return message;
    }
}
