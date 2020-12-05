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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.SpongeTicks;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@Mixin(net.minecraft.world.chunk.Chunk.class)
public abstract class ChunkMixin_API implements Chunk, IChunkMixin_API<Chunk> {

    @Shadow @Final private net.minecraft.world.World world;
    @Shadow @Final private ChunkPos pos;
    @Shadow @Final private Map<BlockPos, TileEntity> tileEntities;
    @Shadow private long inhabitedTime;

    @Shadow public abstract boolean shadow$isEmpty();

    @Shadow
    public abstract <T extends net.minecraft.entity.Entity> void shadow$getEntitiesOfTypeWithinAABB(Class<? extends T> entityClass,
            AxisAlignedBB aabb, List<T> listToFill, @Nullable Predicate<? super T> filter);

    @MonotonicNonNull private AxisAlignedBB api$chunkAABB;

    @Override
    public World<@NonNull ?> getWorld() {
        return (World<@NonNull ?>) this.world;
    }

    @Override
    public boolean loadChunk(final boolean generate) {
        // This is a loaded chunk. It cannot be loaded again.
        return false;
    }

    // TODO: I don't think this makes sense here - should be on World?
    @Override
    public boolean unloadChunk() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<? extends BlockEntity> getBlockEntities() {
        // We make a defensive copy here - the consumer shouldn't hold this but
        // it might well happen.
        return (Collection<? extends BlockEntity>) new ArrayList<>(this.tileEntities.values());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<? extends Player> getPlayers() {
        final List<net.minecraft.entity.player.PlayerEntity> entities = new ArrayList<>();
        this.shadow$getEntitiesOfTypeWithinAABB(net.minecraft.entity.player.PlayerEntity.class,
                this.api$getAxisAlignedBBForChunk(), entities, x -> true);
        return (Collection<? extends Player>) entities;
    }

    @Override
    public Optional<Entity> getEntity(final UUID uuid) {
        final List<net.minecraft.entity.Entity> entities = new ArrayList<>();
        this.shadow$getEntitiesOfTypeWithinAABB(net.minecraft.entity.Entity.class,
                this.api$getAxisAlignedBBForChunk(), entities, x -> ((Entity) x).getUniqueId().equals(uuid));
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of((Entity) entities.get(0));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> Collection<? extends T> getEntities(final Class<? extends T> entityClass, final AABB box,
            @Nullable final Predicate<? super T> predicate) {
        final List<net.minecraft.entity.Entity> entities = new ArrayList<>();
        this.shadow$getEntitiesOfTypeWithinAABB((Class<? extends net.minecraft.entity.Entity>) entityClass,
                VecHelper.toMinecraftAABB(box), entities, (Predicate<? super net.minecraft.entity.Entity>) predicate);
        return (Collection<? extends T>) entities;
    }

    @Override
    public Collection<? extends Entity> getEntities(final AABB box, final Predicate<? super Entity> filter) {
        return this.getEntities(Entity.class, box, filter);
    }

    @Override
    @Intrinsic
    public boolean isEmpty() {
        return this.shadow$isEmpty();
    }

    @Override
    public Ticks getInhabitedTime() {
        return new SpongeTicks(this.inhabitedTime);
    }

    @SuppressWarnings("ConstantConditions")
    private AxisAlignedBB api$getAxisAlignedBBForChunk() {
        if (this.api$chunkAABB == null) {
            this.api$chunkAABB = new AxisAlignedBB(
                    this.pos.getXStart(),
                    0.0,
                    this.pos.getZStart(),
                    this.pos.getXEnd(),
                    this.world.getActualHeight(),
                    this.pos.getZEnd()
            );
        }
        return this.api$chunkAABB;
    }

}
