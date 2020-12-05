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
public abstract class ChunkPrimer_API implements IChunkMixin_API<PrimitiveChunk>, PrimitiveChunk {

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
