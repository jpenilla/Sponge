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
package org.spongepowered.common.mixin.api.mcp.world.chunk;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkPrimer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.gen.GenerationRegion;
import org.spongepowered.api.world.gen.PrimitiveChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@Mixin(ChunkPrimer.class)
public abstract class ChunkPrimer_API implements PrimitiveChunk {

    @Shadow @Final private Map<BlockPos, TileEntity> tileEntities;

    // TODO: There is no link to a world in a ChunkPrimer - so we will need to rejig the
    // PrimitiveChunk and ProtoChunk<?> interfaces.
    @Override
    public GenerationRegion getWorld() {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<? extends BlockEntity> getBlockEntities() {
        // We make a defensive copy here - the consumer shouldn't hold this but
        // it might well happen.
        return (Collection<? extends BlockEntity>) new ArrayList<>(this.tileEntities.values());
    }

    @Override
    public Collection<? extends Player> getPlayers() {
        return Collections.emptyList();
    }

    @Override
    public Optional<Entity> getEntity(final UUID uuid) {
        return Optional.empty();
    }

    @Override
    public <T extends Entity> Collection<? extends T> getEntities(final Class<? extends T> entityClass, final AABB box,
            @Nullable final Predicate<? super T> predicate) {
        return Collections.emptyList();
    }

    @Override
    public Collection<? extends Entity> getEntities(final AABB box, final Predicate<? super Entity> filter) {
        return this.getEntities(Entity.class, box, filter);
    }

}
