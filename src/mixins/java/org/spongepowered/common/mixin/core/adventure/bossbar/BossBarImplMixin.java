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
package org.spongepowered.common.mixin.core.adventure.bossbar;

import net.kyori.adventure.bossbar.BossBar;
import net.minecraft.server.level.ServerBossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.adventure.VanillaBossBarListener;
import org.spongepowered.common.bridge.adventure.BossBarBridge;
import org.spongepowered.common.bridge.world.BossEventBridge;

@Mixin(targets = "net.kyori.adventure.bossbar.BossBarImpl")
public abstract class BossBarImplMixin implements BossBarBridge {

    private ServerBossEvent bridge$vanillaServerBar;

    @Override
    public ServerBossEvent bridge$asVanillaServerBar() {
        if (this.bridge$vanillaServerBar == null) {
            final BossBar $this = (BossBar) this;
            this.bridge$vanillaServerBar = new ServerBossEvent(SpongeAdventure.asVanilla($this.name()), SpongeAdventure.asVanilla($this.color()), SpongeAdventure.asVanilla($this.overlay()));
            final BossEventBridge bridge = (BossEventBridge) this.bridge$vanillaServerBar;
            bridge.bridge$copyAndAssign($this);
            $this.addListener(new VanillaBossBarListener(this.bridge$vanillaServerBar));
        }
        return this.bridge$vanillaServerBar;
    }
}
