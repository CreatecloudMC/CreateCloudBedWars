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

package org.createcloud.bedwars.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.createcloud.bedwars.BedWarsPlugin;
import org.createcloud.bedwars.PlatformService;
import org.createcloud.bedwars.api.game.GameStatus;
import org.createcloud.bedwars.bukkit.hooks.BukkitBStatsMetrics;
import org.createcloud.bedwars.bukkit.hooks.PerWorldInventoryCompatibilityFix;
import org.createcloud.bedwars.bukkit.listener.LegacyWaterListener;
import org.createcloud.bedwars.game.GameImpl;
import org.createcloud.bedwars.game.GameManagerImpl;
import org.createcloud.bedwars.lang.LangKeys;
import org.createcloud.lib.Server;
import org.createcloud.lib.block.BlockPlacement;
import org.createcloud.lib.block.snapshot.BlockSnapshot;
import org.createcloud.lib.event.player.PlayerBlockBreakEvent;
import org.createcloud.lib.event.player.PlayerBlockPlaceEvent;
import org.createcloud.lib.impl.bukkit.event.player.BukkitPlayerBlockBreakEvent;
import org.createcloud.lib.impl.bukkit.event.player.BukkitPlayerBlockPlaceEvent;
import org.createcloud.lib.lang.Message;
import org.createcloud.lib.sender.CommandSender;
import org.createcloud.lib.spectator.Component;
import org.createcloud.lib.tasker.DefaultThreads;
import org.createcloud.lib.tasker.Tasker;
import org.createcloud.lib.tasker.TaskerTime;
import org.createcloud.lib.tasker.task.TaskBase;
import org.createcloud.lib.utils.annotations.Service;
import org.createcloud.lib.utils.annotations.ServiceDependencies;
import org.createcloud.lib.utils.annotations.methods.OnEnable;
import org.createcloud.lib.utils.reflect.Reflect;

import java.util.Locale;
import java.util.function.Consumer;

@Service
@ServiceDependencies(initAnother = {
        PerWorldInventoryCompatibilityFix.class,
        BukkitBStatsMetrics.class
})
public class BukkitPlatformService extends PlatformService {
    @OnEnable
    public void onEnable(@NotNull Plugin plugin) {
        if (!Server.isVersion(1, 12, 2)) {
            plugin.getServer().getPluginManager().registerEvents(new LegacyWaterListener(), plugin);
        }
    }

    @Override
    public void reloadPlugin(@NotNull CommandSender sender) {
        sender.sendMessage(Message.of(LangKeys.SAFE_RELOAD).defaultPrefix());

        GameManagerImpl.getInstance().getLocalGames().forEach(GameImpl::stop);

        var logger = BedWarsPlugin.getInstance().getLogger();
        var plugin = BedWarsPlugin.getInstance().getPluginDescription().as(JavaPlugin.class);

        Tasker.runRepeatedly(DefaultThreads.GLOBAL_THREAD, new Consumer<>() {
            public int timer = 60;

            @Override
            public void accept(@NotNull TaskBase taskBase) {
                boolean gameRuns = false;
                for (var game : GameManagerImpl.getInstance().getLocalGames()) {
                    if (game.getStatus() != GameStatus.DISABLED) {
                        gameRuns = true;
                        break;
                    }
                }

                if (gameRuns && timer == 0) {
                    sender.sendMessage(Message.of(LangKeys.SAFE_RELOAD_FAILED_TO_STOP_GAME).defaultPrefix());
                }

                if (!gameRuns || timer == 0) {
                    taskBase.cancel();
                    try {
                        logger.info(String.format("Disabling %s", plugin.getDescription().getFullName()));
                        Bukkit.getPluginManager().callEvent(new PluginDisableEvent(plugin));
                        Reflect.getMethod(plugin, "setEnabled", boolean.class).invoke(false);
                    } catch (Throwable ex) {
                        logger.trace("Error occurred (in the plugin loader) while disabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
                    }

                    try {
                        Bukkit.getScheduler().cancelTasks(plugin);
                    } catch (Throwable ex) {
                        logger.trace("Error occurred (in the plugin loader) while cancelling tasks for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
                    }

                    try {
                        Bukkit.getServicesManager().unregisterAll(plugin);
                    } catch (Throwable ex) {
                        logger.trace("Error occurred (in the plugin loader) while unregistering services for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
                    }

                    try {
                        HandlerList.unregisterAll(plugin);
                    } catch (Throwable ex) {
                        logger.trace("Error occurred (in the plugin loader) while unregistering events for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
                    }

                    try {
                        Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin);
                        Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin);
                    } catch (Throwable ex) {
                        logger.trace("Error occurred (in the plugin loader) while unregistering plugin channels for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
                    }

                    try {
                        for (var world : Bukkit.getWorlds()) {
                            world.removePluginChunkTickets(plugin);
                        }
                    } catch (Throwable ex) {
                        logger.trace("Error occurred (in the plugin loader) while removing chunk tickets for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
                    }
                    Bukkit.getServer().getPluginManager().enablePlugin(plugin);
                    sender.sendMessage(Component.text("Plugin reloaded! Keep in mind that restarting the server is safer!"));
                    return;
                }
                timer--;
            }
        }, 20, TaskerTime.TICKS);
    }

    // TODO: slib?
    @Override
    public void spawnEffect(@NotNull org.createcloud.lib.world.Location location, @NotNull String value) {
        var particle = Effect.valueOf(value.toUpperCase(Locale.ROOT));
        var bukkitLoc =  location.as(Location.class);
        bukkitLoc.getWorld().playEffect(bukkitLoc, particle, 1);
    }

    @Override
    @NotNull
    public PlayerBlockPlaceEvent fireFakeBlockPlaceEvent(@NotNull BlockPlacement block, @NotNull BlockSnapshot originalState, @NotNull BlockPlacement clickedBlock, @NotNull org.createcloud.lib.item.ItemStack item, @NotNull org.createcloud.lib.player.Player player, boolean canBuild) {
        var event = new BlockPlaceEvent(block.as(Block.class), originalState.as(BlockState.class),
                clickedBlock.as(Block.class), item.as(ItemStack.class), player.as(Player.class), canBuild);
        Bukkit.getPluginManager().callEvent(event);

        return new BukkitPlayerBlockPlaceEvent(event);
    }

    @Override
    @NotNull
    public PlayerBlockBreakEvent fireFakeBlockBreakEvent(@NotNull BlockPlacement block, @NotNull org.createcloud.lib.player.Player player) {
        var event = new BlockBreakEvent(block.as(Block.class), player.as(Player.class));
        Bukkit.getPluginManager().callEvent(event);

        return new BukkitPlayerBlockBreakEvent(event);
    }

    @Override
    public @Nullable Object savePlatformScoreboard(@NotNull org.createcloud.lib.player.Player player) {
        return player.as(Player.class).getScoreboard();
    }

    @Override
    public void restorePlatformScoreboard(@NotNull org.createcloud.lib.player.Player player, @NotNull Object scoreboard) {
        if (scoreboard instanceof Scoreboard) {
            player.as(Player.class).setScoreboard((Scoreboard) scoreboard);
        }
    }
}
