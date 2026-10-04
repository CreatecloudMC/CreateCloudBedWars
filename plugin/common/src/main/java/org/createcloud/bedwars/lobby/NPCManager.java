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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.createcloud.bedwars.commands.NPCCommand;
import org.createcloud.bedwars.lang.LangKeys;
import org.createcloud.lib.event.OnEvent;
import org.createcloud.lib.event.player.PlayerJoinEvent;
import org.createcloud.lib.event.player.PlayerLeaveEvent;
import org.createcloud.lib.lang.Message;
import org.createcloud.lib.npc.event.NPCInteractEvent;
import org.createcloud.lib.plugin.ServiceManager;
import org.createcloud.lib.tasker.DefaultThreads;
import org.createcloud.lib.tasker.Tasker;
import org.createcloud.lib.tasker.TaskerTime;
import org.createcloud.lib.utils.InteractType;
import org.createcloud.lib.utils.annotations.Service;
import org.createcloud.lib.utils.annotations.ServiceDependencies;
import org.createcloud.lib.utils.annotations.methods.OnPostEnable;
import org.createcloud.lib.utils.annotations.methods.OnPreDisable;
import org.createcloud.lib.utils.annotations.parameters.ConfigFile;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

@Service
@ServiceDependencies(dependsOn = {
        org.createcloud.lib.npc.NPCManager.class
})
@RequiredArgsConstructor
public class NPCManager {
    @ConfigFile(value = "database/npcdb.json")
    private final GsonConfigurationLoader loader;
    @Getter
    @Setter
    private boolean modified;

    @Getter
    private final List<BedWarsNPC> npcs = new ArrayList<>();

    public static NPCManager getInstance() {
        return ServiceManager.get(NPCManager.class);
    }

    @OnPostEnable
    public void onPostEnable() {
        try {
            var node = loader.load();

            node.childrenList().forEach(npcNode -> {
                try {
                    var npc = npcNode.get(BedWarsNPC.class);
                    if (npc == null) {
                        return;
                    }
                    npc.spawn();
                    npcs.add(npc);
                } catch (ConfigurateException e) {
                    e.printStackTrace();
                }
            });
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }

    }

    @OnPreDisable
    public void onPreDisable() {
        if (!modified) {
            return;
        }

        var node = loader.createNode();

        npcs.forEach(bedWarsNPC -> {
            try {
                bedWarsNPC.destroy();
                node.appendListNode().set(bedWarsNPC);
            } catch (SerializationException e) {
                e.printStackTrace();
            }
        });

        try {
            loader.save(node);
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }

        npcs.clear();
    }

    @OnEvent
    public void onPlayerJoin(PlayerJoinEvent event) {
        npcs.forEach(bedWarsNPC -> {
            var npc = bedWarsNPC.getNpc();
            if (npc != null) {
                Tasker.runDelayed(DefaultThreads.GLOBAL_THREAD, () -> {
                            final var player = event.player();
                            if (player.isOnline()) {
                                npc.addViewer(player);
                            }
                        }, 10, TaskerTime.TICKS);
            }
        });
    }

    @OnEvent
    public void onPlayerLeave(PlayerLeaveEvent event) {
        npcs.forEach(bedWarsNPC -> {
            var npc = bedWarsNPC.getNpc();
            if (npc != null) {
                npc.removeViewer(event.player());
            }
        });
    }

    @OnEvent
    public void onNPCInteract(NPCInteractEvent event) {
        npcs.stream()
                .filter(bedWarsNPC1 -> bedWarsNPC1.getNpc().equals(event.visual()))
                .findFirst()
                .ifPresent(npc -> {
                    if (event.interactType() == InteractType.RIGHT_CLICK && NPCCommand.SELECTING_NPC.contains(event.player().getUuid())) {
                        NPCCommand.SELECTING_NPC.remove(event.player().getUuid());
                        NPCCommand.NPCS_IN_HAND.put(event.player().getUuid(), npc);
                        event.player().sendMessage(Message.of(LangKeys.ADMIN_NPC_EDITING)
                                .defaultPrefix()
                                .placeholder("x", npc.getLocation().getX(), 2)
                                .placeholder("y", npc.getLocation().getY(), 2)
                                .placeholder("z", npc.getLocation().getZ(), 2)
                                .placeholder("yaw", npc.getLocation().getYaw(), 5)
                                .placeholder("pitch", npc.getLocation().getPitch(), 5));
                    } else {
                        npc.handleClick(event.player(), event.interactType());
                    }
                });
    }
}
