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
package org.spongepowered.common.mixin.invalid.entityactivation.entity.item;

import net.minecraft.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.level.storage.WorldInfoBridge;
import org.spongepowered.common.mixin.entityactivation.entity.EntityMixin_EntityActivation;
import org.spongepowered.common.util.Constants;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin_EntityActivation extends EntityMixin_EntityActivation {

    @Shadow private int pickupDelay;
    @Shadow private int age;

    @Override
    public void activation$inactiveTick() {
        if (this.pickupDelay > 0 && this.pickupDelay != Constants.Entity.Item.INFINITE_PICKUP_DELAY) {
            --this.pickupDelay;
        }

        if (!this.shadow$getEntityWorld().isRemote() && this.age >= ((WorldInfoBridge) this.shadow$getEntityWorld().getWorldInfo())
                .bridge$getConfigAdapter().getConfig().getEntity().getItemDespawnRate()) {
            this.shadow$remove();
        }
    }
}
