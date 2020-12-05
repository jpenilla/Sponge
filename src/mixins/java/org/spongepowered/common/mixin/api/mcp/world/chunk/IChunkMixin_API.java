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

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.HeightType;
import org.spongepowered.api.world.ProtoWorld;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.chunk.ChunkState;
import org.spongepowered.api.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.biome.BiomeContainerAccessor;
import org.spongepowered.common.util.SpongeTicks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Mixin(IChunk.class)
public interface IChunkMixin_API<P extends ProtoChunk<P>> extends ProtoChunk<P> {

    @Shadow void shadow$addEntity(net.minecraft.entity.Entity entity);
    @Shadow void shadow$addTileEntity(BlockPos pos, TileEntity tileEntityIn);
    @Shadow void shadow$removeTileEntity(BlockPos pos);
    @Shadow ChunkStatus shadow$getStatus();
    @Shadow long shadow$getInhabitedTime();
    @Shadow void shadow$setInhabitedTime(long newInhabitedTime);
    @Shadow BiomeContainer shadow$getBiomes();
    @Shadow @Nullable BlockState shadow$setBlockState(BlockPos pos, BlockState state, boolean isMoving);
    @Shadow int shadow$getTopBlockY(Heightmap.Type type, int x, int z);
    @Shadow ChunkPos shadow$getPos();
    @Shadow Heightmap shadow$getHeightmap(Heightmap.Type typeIn);

    @Override
    default BiomeType getBiome(final int x, final int y, final int z) {
        return (BiomeType) this.shadow$getBiomes().getNoiseBiome(x, y, z);
    }

    @Override
    default boolean setBiome(final int x, final int y, final int z, final BiomeType biome) {
        final Biome[] biomes = ((BiomeContainerAccessor) this.shadow$getBiomes()).accessor$getBiomes();

        final int maskedX = x & BiomeContainer.HORIZONTAL_MASK;
        final int maskedY = MathHelper.clamp(y, 0, BiomeContainer.VERTICAL_MASK);
        final int maskedZ = z & BiomeContainer.HORIZONTAL_MASK;

        final int WIDTH_BITS = BiomeContainerAccessor.accessor$WIDTH_BITS();
        final int posKey = maskedY << WIDTH_BITS + WIDTH_BITS | maskedZ << WIDTH_BITS | maskedX;
        biomes[posKey] = (Biome) biome;

        return true;
    }

    @Override
    default Vector3i getChunkPosition() {
        return VecHelper.toVec3i(this.shadow$getPos());
    }

    @Override
    default void addEntity(final Entity entity) {
        this.shadow$addEntity((net.minecraft.entity.Entity) entity);
    }

    @Override
    default Ticks getInhabitedTime() {
        return new SpongeTicks(this.shadow$getInhabitedTime());
    }

    @Override
    default void setInhabitedTime(final Ticks ticks) {
        this.shadow$setInhabitedTime(ticks.getTicks());
    }

    default ChunkState getState() {
        return (ChunkState) this.shadow$getStatus();
    }

    @Override
    default Optional<? extends BlockEntity> getBlockEntity(final int x, final int y, final int z) {
        return Optional.ofNullable((BlockEntity) ((IBlockReader) this).getTileEntity(new BlockPos(x, y, z)));
    }

    @Override
    default void addBlockEntity(final int x, final int y, final int z, final BlockEntity blockEntity) {
        this.shadow$addTileEntity(new BlockPos(x, y, z), (TileEntity) blockEntity);
    }

    @Override
    default void removeBlockEntity(final int x, final int y, final int z) {
        this.shadow$removeTileEntity(new BlockPos(x, y, z));
    }

    @Override
    default boolean setBlock(final int x, final int y, final int z, final org.spongepowered.api.block.BlockState block) {
        return this.shadow$setBlockState(new BlockPos(x, y, z), (net.minecraft.block.BlockState) block, false) != null;
    }

    @Override
    default boolean removeBlock(final int x, final int y, final int z) {
        return this.setBlock(x, y, z, BlockTypes.AIR.get().getDefaultState());
    }

    @Override
    default org.spongepowered.api.block.BlockState getBlock(final int x, final int y, final int z) {
        return (org.spongepowered.api.block.BlockState) ((IBlockReader) this).getBlockState(new BlockPos(x, y, z));
    }

    @Override
    default FluidState getFluid(final int x, final int y, final int z) {
        return (FluidState) ((IBlockReader) this).getFluidState(new BlockPos(x, y, z));
    }

    @Override
    default int getHighestYAt(final int x, final int z) {
        return this.shadow$getTopBlockY(Heightmap.Type.WORLD_SURFACE, x, z);
    }

    @Override
    default boolean isEmpty() {
        return false;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    default int getHeight(final HeightType type, final int x, final int z) {
        return this.shadow$getHeightmap((Heightmap.Type) (Object) type).getHeight(x, z);
    }

    @Override
    default Vector3i getBlockMin() {
        return VecHelper.toVector3i(this.shadow$getPos().asBlockPos());
    }

    @Override
    default Vector3i getBlockMax() {
        final ChunkPos chunkPos = this.shadow$getPos();
        return new Vector3i(chunkPos.getXEnd(), this.getWorld().getHeight(), chunkPos.getZEnd());
    }

    @Override
    default Vector3i getBlockSize() {
        return this.getBlockMax().sub(this.getBlockMin());
    }

    @Override
    default boolean containsBlock(final int x, final int y, final int z) {
        final Vector3i min = this.getBlockMin();
        final Vector3i max = this.getBlockMax();
        return x >= min.getX() && x <= max.getX() && y >= min.getY() && y <= max.getY() && z >= min.getZ() && z <= max.getZ();
    }

    @Override
    default boolean isAreaAvailable(final int x, final int y, final int z) {
        return this.containsBlock(x, y, z);
    }

    @Override
    default Collection<? extends Player> getPlayers() {
        return Collections.emptyList();
    }

    @Override
    default ProtoWorld<?> getWorld() {
        throw new UnsupportedOperationException("Unfortunately, you've found a chunk that isn't part of the Sponge API.");
    }

}
