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
package org.spongepowered.common.config.category;

import net.minecraft.launchwrapper.Launch;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.io.IOException;

@ConfigSerializable
public class OptimizationCategory extends ConfigCategory {

    private static final String PRE_MERGE_COMMENT = "If enabled, block item drops are pre-processed to avoid \n"
                                                    + "having to spawn extra entities that will be merged post spawning.\n"
                                                    + "Usually, Sponge is smart enough to determine when to attempt an item pre-merge\n"
                                                    + "and when not to, however, in certain cases, some mods rely on items not being\n"
                                                    + "pre-merged and actually spawned, in which case, the items will flow right through\n"
                                                    + "without being merged.";

    @Setting(value = "drops-pre-merge", comment = PRE_MERGE_COMMENT)
    private boolean preItemDropMerge;

    @Setting(value = "cache-tameable-owners", comment = "Caches tameable entities owners to avoid constant lookups against data watchers. If mods cause issue, disable.")
    private boolean cacheTameableOwners = true;

    @Setting(value = "structure-saving", comment = "Handles structures that are saved to disk. Certain structures can take up large amounts\n"
            + "of disk space for very large maps and the data for these structures is only needed while the world\n"
            + "around them is generating. Disabling saving of these structures can save disk space and time during\n"
            + "saves if your world is already fully generated.")
    private StructureSaveCategory structureSaveCategory = new StructureSaveCategory();

    @Setting(value = "async-lighting", comment = "Runs lighting updates async.")
    private boolean asyncLighting = true;

    @Setting(value = "tile-entity-unload", comment = "If enabled, uses Forge's 1.12 TE unload patch to fix MC-117075.\n"
            + "See https://github.com/MinecraftForge/MinecraftForge/pull/4281 for more info.\n"
            + "Note: This may cause issues with some mods so backup before enabling.")
    private boolean tileEntityUnload = false;

    public OptimizationCategory() {
        try {
            // Enabled ny default on SpongeVanilla, disabled by default on SpongeForge.
            // Because of how early this constructor gets called, we can't use SpongeImplHooks or even Game
            this.preItemDropMerge = Launch.classLoader.getClassBytes("net.minecraftforge.common.ForgeVersion") == null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StructureSaveCategory getStructureSaveCategory() {
        return this.structureSaveCategory;
    }

    public boolean useStructureSave() {
        return this.structureSaveCategory.isEnabled();
    }

    public boolean doDropsPreMergeItemDrops() {
        return this.preItemDropMerge;
    }

    public boolean useCacheTameableOwners() {
        return this.cacheTameableOwners;
    }

    public boolean useAsyncLighting() {
        return this.asyncLighting;
    }

    public boolean useTileEntityUnload() {
        return this.tileEntityUnload;
    }
}
