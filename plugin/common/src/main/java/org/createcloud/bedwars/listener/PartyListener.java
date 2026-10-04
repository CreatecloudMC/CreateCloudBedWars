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

import com.alessiodp.parties.api.Parties;
import lombok.experimental.UtilityClass;
import org.createcloud.bedwars.config.MainConfig;
import org.createcloud.bedwars.events.PlayerJoinedEventImpl;
import org.createcloud.bedwars.lang.LangKeys;
import org.createcloud.bedwars.player.BedWarsPlayer;
import org.createcloud.bedwars.player.PlayerManagerImpl;
import org.createcloud.bedwars.utils.MiscUtils;
import org.createcloud.lib.event.OnEvent;
import org.createcloud.lib.lang.Message;
import org.createcloud.lib.plugin.Plugins;
import org.createcloud.lib.utils.annotations.Service;
import org.createcloud.lib.utils.annotations.ServiceDependencies;
import org.createcloud.lib.utils.annotations.methods.ShouldRunControllable;

@Service
@ServiceDependencies(dependsOn = {
    MainConfig.class
})
@UtilityClass
public class PartyListener {

    @ShouldRunControllable
    public boolean isEnabled() {
        return MainConfig.getInstance().node("party", "enabled").getBoolean() && Plugins.isEnabled("Parties");
    }

    @OnEvent
    public void onBedWarsPlayerJoined(PlayerJoinedEventImpl e) {
        if (!MainConfig.getInstance().node("party", "autojoin-members").getBoolean()) {
            return;
        }

        final var player = e.getPlayer();
        final var partyApi = Parties.getApi();
        final var partyPlayer = partyApi.getPartyPlayer(player.getUuid());

        final var game = e.getGame();

        if (partyPlayer.getPartyId() != null) {
            final var party = partyApi.getParty(partyPlayer.getPartyId());

            if (party != null) {
                final var leaderUUID = party.getLeader();

                if (leaderUUID != null) {
                    //Player who joined is party leader
                    if (leaderUUID.equals(player.getUuid())) {
                        final var players = MiscUtils.getOnlinePlayers(party.getMembers());

                        if (players.size() > 1) {
                            players.forEach(partyMember -> {
                                if (partyMember != null) {
                                    if (partyMember.getUuid().equals(player.getUuid())) {
                                        return;
                                    }
                                    player.sendMessage(Message.of(LangKeys.PARTY_INFORM_GAME_JOIN).defaultPrefix());

                                    var gameOfPlayer = PlayerManagerImpl.getInstance().getGameOfPlayer(partyMember);
                                    if (gameOfPlayer.isPresent()) {
                                        if (gameOfPlayer.get().getName().equalsIgnoreCase(game.getName())) {
                                            return;
                                        }
                                        gameOfPlayer.get().leaveFromGame(partyMember.as(BedWarsPlayer.class));
                                    }
                                    game.joinToGame(PlayerManagerImpl.getInstance().getPlayerOrCreate(partyMember));
                                }
                            });

                            if (MainConfig.getInstance().node("party", "notify-when-warped").getBoolean(true)) {
                                player.sendMessage(Message.of(LangKeys.PARTY_WARPED).defaultPrefix());
                            }
                        }
                    }
                }
            }
        }
    }
}
