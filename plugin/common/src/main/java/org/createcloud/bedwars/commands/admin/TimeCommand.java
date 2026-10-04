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

package org.createcloud.bedwars.commands.admin;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.IntegerArgument;
import org.createcloud.bedwars.lang.LangKeys;
import org.createcloud.lib.lang.Message;
import org.createcloud.lib.sender.CommandSender;
import org.createcloud.lib.utils.annotations.Service;

@Service
public class TimeCommand extends BaseAdminSubCommand {
    public TimeCommand() {
        super("time");
    }

    @Override
    public void construct(CommandManager<CommandSender> manager, Command.Builder<CommandSender> commandSenderWrapperBuilder) {
        manager.command(
                commandSenderWrapperBuilder
                        .argument(IntegerArgument
                                .<CommandSender>newBuilder("time")
                                .withMin(10)
                                .withMax(3600)
                        )
                        .handler(commandContext -> editMode(commandContext, (sender, game) -> {
                            int time = commandContext.get("time");

                            if (time >= 10 && time <= 7200) {
                                game.setGameTime(time);
                                sender.sendMessage(Message.of(LangKeys.ADMIN_ARENA_EDIT_SUCCESS_GAME_TIME_SET).defaultPrefix().placeholder("time", time));
                                return;
                            }
                            sender.sendMessage(Message
                                    .of(LangKeys.ADMIN_ARENA_EDIT_ERRORS_INVALID_COUNTDOWN)
                                    .defaultPrefix()
                                    .placeholder("lowest", 10)
                                    .placeholder("highest", 7200)
                            );
                        }))
        );
    }
}
