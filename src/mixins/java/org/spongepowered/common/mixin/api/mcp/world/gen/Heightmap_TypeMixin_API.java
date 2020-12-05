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
package org.spongepowered.common.mixin.api.mcp.world.gen;

import net.minecraft.world.gen.Heightmap;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.BlockReaderAwareMatcher;
import org.spongepowered.api.world.HeightType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Locale;
import java.util.function.Predicate;

@Mixin(Heightmap.Type.class)
public abstract class Heightmap_TypeMixin_API implements HeightType {

    @Shadow @Final private String id;
    @Shadow @Final private Predicate<net.minecraft.block.BlockState> heightLimitPredicate;
    @MonotonicNonNull private ResourceKey api$key;

    @Override
    public ResourceKey getKey() {
        if (this.api$key == null) {
            // Assign the Sponge ID because Mojang has not assigned this a
            // namespace
            this.api$key = ResourceKey.sponge(this.id.toLowerCase(Locale.ROOT));
        }
        return this.api$key;
    }

    @Override
    public BlockReaderAwareMatcher<BlockState> getMatcher() {
        return (value, volume, position) -> this.heightLimitPredicate.test((net.minecraft.block.BlockState) value);
    }

}
