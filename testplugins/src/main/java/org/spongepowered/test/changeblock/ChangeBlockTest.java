/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.test.changeblock;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.MatterStates;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.HarvestEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

@Plugin("changeblocktest")
public final class ChangeBlockTest implements LoadableModule {

    static final Marker marker = MarkerManager.getMarker("CHANGEBLOCK");

    final PluginContainer plugin;
    boolean cancelAll = false;
    boolean waterProofRedstone = false;
    boolean printEntityHarvests = false;
    boolean printEntitySpawns = false;
    boolean printEntityDeaths = false;

    @Inject
    public ChangeBlockTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable(final CommandContext ctx) {
        Sponge.getEventManager().registerListeners(this.plugin, new ChangeBlockListener());
        Sponge.getEventManager().registerListeners(this.plugin, new HarvestEntityListener());
        Sponge.getEventManager().registerListeners(this.plugin, new SpawnEntityListener());
        Sponge.getEventManager().registerListeners(this.plugin, new EntityDeathPrinter());
    }

    @Listener
    public void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.plugin, Command.builder()
            .setExecutor(context -> {
                this.cancelAll = !this.cancelAll;
                final Component newState = Component.text(this.cancelAll ? "OFF" : "ON", this.cancelAll ? NamedTextColor.GREEN : NamedTextColor.RED);
                context.sendMessage(Identity.nil(), Component.text("Turning Block Changes: ").append(newState));
                return CommandResult.success();
            })
            .build(), "toggleBlockChanges"
        );
        event.register(this.plugin, Command.builder()
            .setExecutor(context -> {
                this.waterProofRedstone = !this.waterProofRedstone;
                final Component newState = Component.text(this.waterProofRedstone ? "ON" : "OFF", this.waterProofRedstone ? NamedTextColor.GREEN : NamedTextColor.RED);
                context.sendMessage(Identity.nil(), Component.text("Waterproof Redstone : ").append(newState));
                return CommandResult.success();
            })
            .build(), "toggleRedstoneWaterProofing"
        );
        event.register(this.plugin, Command.builder()
            .setExecutor(context -> {
                this.printEntityHarvests = !this.printEntityHarvests;
                final Component newState = Component.text(this.printEntityHarvests ? "ON" : "OFF", this.printEntityHarvests ? NamedTextColor.GREEN : NamedTextColor.RED);
                context.sendMessage(Identity.nil(), Component.text("Logging Entity Harvests : ").append(newState));
                return CommandResult.success();
            })
            .build(), "toggleEntityHarvestPrinting"
        );
        event.register(this.plugin, Command.builder()
            .setExecutor(context -> {
                this.printEntityDeaths = !this.printEntityDeaths;
                final Component newState = Component.text(this.printEntityDeaths ? "ON" : "OFF", this.printEntityDeaths ? NamedTextColor.GREEN : NamedTextColor.RED);
                context.sendMessage(Identity.nil(), Component.text("Logging Entity Harvests : ").append(newState));
                return CommandResult.success();
            })
            .build(), "toggleEntityDeathPrinting"
        );
        event.register(this.plugin, Command.builder()
            .setExecutor(context -> {
                this.printEntitySpawns = !this.printEntitySpawns;
                final Component newState = Component.text(this.printEntitySpawns ? "ON" : "OFF", this.printEntitySpawns ? NamedTextColor.GREEN : NamedTextColor.RED);
                context.sendMessage(Identity.nil(), Component.text("Logging Entity Spawns : ").append(newState));
                return CommandResult.success();
            })
            .build(), "toggleEntitySpawnPrinting"
        );
    }

    public class HarvestEntityListener {

        @Listener
        public void onEntityHarvest(final HarvestEntityEvent event) {
            if (!ChangeBlockTest.this.printEntityHarvests) {
                return;
            }
            final Logger pluginLogger = ChangeBlockTest.this.plugin.getLogger();
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/*************");
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/* HarvestEntityEvent");
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/");
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/ Cause:");
            for (final Object o : event.getCause()) {
                pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/ - " + o);
            }
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/");
        }
    }

    public class SpawnEntityListener {
        @Listener
        public void onEntitySpawn(final SpawnEntityEvent event) {
            if (!ChangeBlockTest.this.printEntitySpawns) {
                return;
            }
            final Logger pluginLogger = ChangeBlockTest.this.plugin.getLogger();
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/*************");
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/* SpawnEntityEvent");
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/");
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/ Cause:");
            for (final Object o : event.getCause()) {
                pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/ - " + o);
            }
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/");
        }
    }

    public class EntityDeathPrinter {
        @Listener
        public void onEntitySpawn(final DestructEntityEvent.Death event) {
            if (!ChangeBlockTest.this.printEntityDeaths) {
                return;
            }
            final Logger pluginLogger = ChangeBlockTest.this.plugin.getLogger();
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/*************");
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/* DestructEntityEvent.Death");
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/");
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/ Cause:");
            for (final Object o : event.getCause()) {
                pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/ - " + o);
            }
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/");
        }
    }

    public class ChangeBlockListener {
        @Listener
        public void onChangeBlock(final ChangeBlockEvent.All post) {
            final Logger pluginLogger = ChangeBlockTest.this.plugin.getLogger();
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/*************");
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/* ChangeBlockEvent");
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/");
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/ Cause:");
            for (final Object o : post.getCause()) {
                pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/ - " + o);
            }
            pluginLogger.log(Level.INFO, ChangeBlockTest.marker, "/");
            if (post.getCause().root() instanceof LocatableBlock) {
                // Leaves are the bane of all existence... they just spam so many events....
                final BlockType type = ((LocatableBlock) post.getCause().root()).getBlockState().getType();
                if (type == BlockTypes.ACACIA_LEAVES.get()
                   || type == BlockTypes.BIRCH_LEAVES.get()
                   || type == BlockTypes.OAK_LEAVES.get()
                   || type == BlockTypes.DARK_OAK_LEAVES.get()
                   || type == BlockTypes.JUNGLE_LEAVES.get()
                   || type == BlockTypes.SPRUCE_LEAVES.get()) {
                    return;
                }
            }
            if (ChangeBlockTest.this.cancelAll) {
                post.setCancelled(true);
            }
            if (ChangeBlockTest.this.waterProofRedstone) {
                for (final BlockTransaction transaction : post.getTransactions()) {
                    final boolean wasRedstone = transaction.getOriginal().getState().getType() == BlockTypes.REDSTONE_WIRE.get();
                    final boolean becomesLiquid = transaction.getFinal().getState().get(Keys.MATTER_STATE).get() == MatterStates.LIQUID.get();
                    if (wasRedstone && becomesLiquid) {
                        post.setCancelled(true);
                        return;
                    }
                }

            }
        }
    }
}
