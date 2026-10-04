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

import org.createcloud.bedwars.config.MainConfig;
import org.createcloud.bedwars.events.PlayerJoinedEventImpl;
import org.createcloud.bedwars.events.PlayerLeaveEventImpl;
import org.createcloud.bedwars.player.PlayerManagerImpl;
import org.createcloud.bedwars.statistics.PlayerStatisticManager;
import org.createcloud.lib.Server;
import org.createcloud.lib.event.OnEvent;
import org.createcloud.lib.event.player.PlayerJoinEvent;
import org.createcloud.lib.event.player.PlayerLeaveEvent;
import org.createcloud.lib.event.player.PlayerWorldChangeEvent;
import org.createcloud.lib.lang.Message;
import org.createcloud.lib.sidebar.Sidebar;
import org.createcloud.lib.sidebar.SidebarManager;
import org.createcloud.lib.tasker.DefaultThreads;
import org.createcloud.lib.tasker.Tasker;
import org.createcloud.lib.tasker.TaskerTime;
import org.createcloud.lib.utils.annotations.Service;
import org.createcloud.lib.utils.annotations.ServiceDependencies;
import org.createcloud.lib.utils.annotations.methods.OnEnable;
import org.createcloud.lib.utils.annotations.methods.ShouldRunControllable;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

@Service
@ServiceDependencies(dependsOn = {
        SidebarManager.class,
        MainConfig.class,
        PlayerStatisticManager.class
})
public class LobbySidebarManager {
    private Sidebar sidebar;
    private String world;

    @ShouldRunControllable
    public static boolean isEnabled() {
        return MainConfig.getInstance().node("main-lobby", "sidebar", "enabled").getBoolean() && MainConfig.getInstance().node("main-lobby", "enabled").getBoolean();
    }

    @OnEnable
    public void onEnable(MainConfig mainConfig) {
        var title = mainConfig.node("main-lobby", "sidebar", "title").getString("<yellow><bold>BED WARS");
        List<String> content;
        try {
            content = mainConfig.node("main-lobby", "sidebar", "content").getList(String.class, List.of());
        } catch (SerializationException e) {
            e.printStackTrace();
            content = List.of();
        }
        sidebar = Sidebar.of()
                .title(Message.ofRichText(title));

        content.forEach(s -> sidebar.bottomLine(LobbyUtils.setupPlaceholders(Message.ofRichText(s))));

        world = mainConfig.node("main-lobby", "world").getString("");

        if (world.isEmpty()) {
            return; // :(
        }

        Server.getConnectedPlayers().forEach(player -> {
            if (player.getLocation().getWorld().getName().equals(world) && !PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
                sidebar.addViewer(player);
            }
        });

        sidebar.show();

        Tasker.runAsyncRepeatedly(() -> sidebar.update(), 20, TaskerTime.TICKS);
    }

    @OnEvent
    public void onJoin(PlayerJoinEvent event) {
        var player = event.player();

        if (world.isEmpty()) {
            return; // :(
        }

        if (player.getLocation().getWorld().getName().equals(world) && !PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            sidebar.addViewer(player);
        }
    }

    @OnEvent
    public void onLeave(PlayerLeaveEvent event) {
        sidebar.removeViewer(event.player());
    }

    @OnEvent
    public void onWorldChange(PlayerWorldChangeEvent event) {
        var player = event.player();

        if (world.isEmpty()) {
            return; // :(
        }

        if (player.getLocation().getWorld().getName().equals(world)) {
            sidebar.addViewer(player);
        } else {
            sidebar.removeViewer(player);
        }
    }

    @OnEvent
    public void onBedWarsJoin(PlayerJoinedEventImpl event) {
        sidebar.removeViewer(event.getPlayer());
    }

    @OnEvent
    public void onBedWarsLeave(PlayerLeaveEventImpl event) {
        var player = event.getPlayer();

        if (world.isEmpty()) {
            return; // :(
        }

        Tasker.runDelayed(DefaultThreads.GLOBAL_THREAD, () -> {
            if (player.isOnline() && player.getLocation().getWorld().getName().equals(world)) {
                sidebar.addViewer(player);
            }
        }, 20, TaskerTime.TICKS);
    }
}
