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

package org.createcloud.bedwars.game.remote.protocol;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.createcloud.bedwars.BedWarsPlugin;
import org.createcloud.bedwars.VersionInfo;
import org.createcloud.bedwars.config.MainConfig;
import org.createcloud.bedwars.game.GameImpl;
import org.createcloud.bedwars.game.GameManagerImpl;
import org.createcloud.bedwars.game.remote.RemoteGameStateManager;
import org.createcloud.bedwars.game.remote.ServerNameChangeEvent;
import org.createcloud.bedwars.game.remote.protocol.messaging.BungeeCordMessenger;
import org.createcloud.bedwars.game.remote.protocol.messaging.DummyMessenger;
import org.createcloud.bedwars.game.remote.protocol.messaging.Messenger;
import org.createcloud.bedwars.game.remote.protocol.messaging.ServerNameAwareMessenger;
import org.createcloud.bedwars.game.remote.protocol.messaging.SocketMessenger;
import org.createcloud.bedwars.game.remote.protocol.packets.GameListPacket;
import org.createcloud.bedwars.game.remote.protocol.packets.GameListRequestPacket;
import org.createcloud.bedwars.game.remote.protocol.packets.GameStateRequestPacket;
import org.createcloud.bedwars.game.remote.protocol.packets.MinigameServerInfoPacket;
import org.createcloud.bedwars.game.remote.protocol.packets.MinigameServerInfoRequestPacket;
import org.createcloud.bedwars.game.remote.protocol.packets.Packet;
import org.createcloud.lib.CustomPayload;
import org.createcloud.lib.Server;
import org.createcloud.lib.event.EventManager;
import org.createcloud.lib.event.OnEvent;
import org.createcloud.lib.plugin.ServiceManager;
import org.createcloud.lib.tasker.Tasker;
import org.createcloud.lib.utils.Preconditions;
import org.createcloud.lib.utils.ProxyType;
import org.createcloud.lib.utils.annotations.Service;
import org.createcloud.lib.utils.annotations.methods.OnPostEnable;
import org.createcloud.lib.utils.annotations.methods.OnPreDisable;
import org.createcloud.lib.utils.logger.Logger;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProtocolManagerImpl extends ProtocolManager {
    private final @NotNull Logger logger;

    private CustomPayload.@Nullable Registration registration;
    private @Nullable Messenger messenger;

    public static @NotNull ProtocolManagerImpl getInstance() {
        return ServiceManager.get(ProtocolManagerImpl.class);
    }

    @OnPostEnable
    public void onPostEnable() {
        var communicationType = MainConfig.getInstance().node("bungee", "communication", "type").getString("bungee");
        switch (communicationType) {
            case "bungee": {
                if (Server.getProxyType() != ProxyType.NONE) {
                    registration = CustomPayload.registerIncomingChannel("BungeeCord", (player, bytes) -> {
                        try {
                            var transformedBytes = getMessenger().incomingPacketTransformer(bytes);
                            if (transformedBytes == null) {
                                return;
                            }

                            processIncoming(transformedBytes);
                        } catch (Exception e) {
                            logger.error("Error while receiving message using BungeeCord plugin messaging channel", e);
                        }
                    });
                    messenger = new BungeeCordMessenger(payload -> CustomPayload.send("BungeeCord", payload));
                    if (Server.getProxyType() == ProxyType.VELOCITY) {
                        logger.warn(
                                "Velocity does not implement the BungeeCord plugin messaging channel the same way as BungeeCord does," +
                                    " which may result in important messages not being delivered. Consider switching to \"socket\" communication"
                        );
                    }
                }
                break;
            }
            case "socket": {
                var host = MainConfig.getInstance().node("bungee", "communication", "socket", "host").getString("localhost");
                var port = MainConfig.getInstance().node("bungee", "communication", "socket", "port").getInt(9000);
                var serverName = BedWarsPlugin.getInstance().getServerName();
                var messenger = new SocketMessenger(host, port, serverName, bytes -> {
                    try {
                        processIncoming(bytes);
                    } catch (Exception e) {
                        logger.error("Error while receiving message using standard socket", e);
                    }
                }, Tasker::runAsync);
                this.messenger = messenger;
                if (serverName != null) {
                    try {
                        messenger.startConnection();
                    } catch (IOException e) {
                        logger.error("An error occurred while starting socket messenger", e);
                    }
                }
                break;
            }
            default: {
                logger.info("Unknown communication type: {}! Disabling...", communicationType);
                messenger = DummyMessenger.INSTANCE;
                break;
            }
        }

    }

    @OnEvent
    public void onServerNameChange(@NotNull ServerNameChangeEvent event) {
        if (messenger instanceof ServerNameAwareMessenger) {
            ((ServerNameAwareMessenger) messenger).setServerName(Objects.requireNonNull(BedWarsPlugin.getInstance().getServerName()));
        }
    }

    @OnPreDisable
    public void onPreDisable() {
        if (registration != null) {
            CustomPayload.unregisterIncomingChannel(registration);
            registration = null;
        }
        messenger = null;
    }

    @Override
    protected void receivePacket0(@NotNull Packet packet) {
        EventManager.fire(new PacketReceivedEvent(packet));

        if (packet instanceof GameListRequestPacket) {
            var gameList = new GameListPacket(
                    Objects.requireNonNull(BedWarsPlugin.getInstance().getServerName(), "This server does not know its name yet!"),
                    GameManagerImpl.getInstance()
                            .getGames()
                            .stream()
                            .map(game -> new GameListPacket.GameEntry(
                                    game.getUuid(),
                                    game.getName(),
                                    game instanceof GameImpl ? ((GameImpl) game).getDisplayNameComponent().toJavaJson() : game.getName()
                            ))
                            .collect(Collectors.toList())
            );
            var requestingServer = ((GameListRequestPacket) packet).getRequestingServer();

            try {
                if (requestingServer != null) {
                    sendPacket(requestingServer, gameList);
                } else {
                    broadcastPacket(gameList);
                }
            } catch (IOException e) {
                logger.error("An error occurred while trying to send GameListRequestPacket", e);
            }
        } else if (packet instanceof GameStateRequestPacket) {
            var serverName = Objects.requireNonNull(BedWarsPlugin.getInstance().getServerName(), "This server does not know its name yet!");
            var gameId = ((GameStateRequestPacket) packet).getGameIdentifier();

            var gameOpt = GameManagerImpl.getInstance().getLocalGame(gameId);
            if (gameOpt.isEmpty()) {
                return; // cannot answer
            }
            var game = gameOpt.get();

            var gameState = RemoteGameStateManager.buildStatePacket(game, serverName);

            var requestingServer = ((GameStateRequestPacket) packet).getRequestingServer();

            try {
                if (requestingServer != null) {
                    sendPacket(requestingServer, gameState);
                } else {
                    broadcastPacket(gameState);
                }
            } catch (IOException e) {
                logger.error("An error occurred while trying to send GameStatePacket", e);
            }
        } else if (packet instanceof MinigameServerInfoRequestPacket) {
            var minigameServerInfo = new MinigameServerInfoPacket(
                    Objects.requireNonNull(BedWarsPlugin.getInstance().getServerName(), "This server does not know its name yet!"),
                    "ScreamingBedWars",
                    VersionInfo.VERSION + "/" + VersionInfo.BUILD_NUMBER
            );
            var requestingServer = ((MinigameServerInfoRequestPacket) packet).getRequestingServer();

            try {
                if (requestingServer != null) {
                    sendPacket(requestingServer, minigameServerInfo);
                } else {
                    broadcastPacket(minigameServerInfo);
                }
            } catch (IOException e) {
                logger.error("An error occurred while trying to send MinigameServerInfoPacket", e);
            }
        }
    }

    @Override
    protected @NotNull Messenger getMessenger() {
        return Preconditions.checkNotNull(messenger, "Messenger has not been constructed yet or communication is disabled!");
    }

    public @Nullable Messenger getMessengerOrNull() {
        return messenger;
    }
}
