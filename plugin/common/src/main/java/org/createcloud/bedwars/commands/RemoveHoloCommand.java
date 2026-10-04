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

package org.createcloud.bedwars.commands;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import org.createcloud.bedwars.holograms.StatisticsHolograms;
import org.createcloud.bedwars.lang.LangKeys;
import org.createcloud.lib.lang.Message;
import org.createcloud.lib.player.Player;
import org.createcloud.lib.sender.CommandSender;
import org.createcloud.lib.utils.annotations.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
public class RemoveHoloCommand extends BaseCommand {
    public static final List<UUID> PLAYERS_WITH_HOLOGRAM_REMOVER_IN_HAND = Collections.synchronizedList(new LinkedList<>());

    public RemoveHoloCommand() {
        super("removeholo", BedWarsPermission.ADMIN_PERMISSION, false);
    }

    @Override
    protected void construct(Command.Builder<CommandSender> commandSenderWrapperBuilder, CommandManager<CommandSender> manager) {
        manager.command(
                commandSenderWrapperBuilder
                    .handler(commandContext -> {
                        var player = commandContext.getSender().as(Player.class);
                        if (!StatisticsHolograms.isEnabled()) {
                            commandContext.getSender().sendMessage(Message.of(LangKeys.ADMIN_HOLO_NOT_ENABLED).defaultPrefix());
                        } else {
                            PLAYERS_WITH_HOLOGRAM_REMOVER_IN_HAND.add(player.getUuid());
                            commandContext.getSender().sendMessage(Message.of(LangKeys.ADMIN_HOLO_CLICK_TO_REMOVE).defaultPrefix());
                        }
                    })
        );
    }
}
