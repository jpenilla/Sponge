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
package org.spongepowered.common.event.tracking.phase.packet.inventory;

import com.google.common.collect.Lists;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.util.ContainerUtil;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Hand;

public final class DropItemWithHotkeyState extends BasicInventoryPacketState {

    public DropItemWithHotkeyState() {
        super(Constants.Networking.MODE_DROP | Constants.Networking.BUTTON_PRIMARY | Constants.Networking.BUTTON_SECONDARY | Constants.Networking.CLICK_INSIDE_WINDOW);
    }

    @Override
    public boolean doesCaptureEntityDrops(final InventoryPacketContext context) {
        return true;
    }

    @Override
    public void unwind(final InventoryPacketContext context) {
        final ServerPlayerEntity player = context.getPacketPlayer();
        //final ItemStack usedStack = context.getItemUsed();
        //final ItemStackSnapshot usedSnapshot = ItemStackUtil.snapshotOf(usedStack);
        final Entity spongePlayer = (Entity) player;
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(spongePlayer);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);

            // TODO - Determine if we need to pass the supplier or perform some parameterized
            //  process if not empty method on the capture object.
            TrackingUtil.processBlockCaptures(context);
            context.getCapturedItemsSupplier()
                .acceptAndClearIfNotEmpty(items -> {

                    final ArrayList<Entity> entities = new ArrayList<>();
                    for (final ItemEntity item : items) {
                        entities.add((Entity) item);
                    }

                    final int usedButton;
                    final Slot slot;
                    if (context.getPacket() instanceof CPlayerDiggingPacket) {
                        final CPlayerDiggingPacket packetIn = context.getPacket();
                        usedButton = packetIn.getAction() == CPlayerDiggingPacket.Action.DROP_ITEM ? Constants.Networking.PACKET_BUTTON_PRIMARY_ID : 1;
                        slot = ((PlayerInventory) player.inventory).getEquipment().getSlot(EquipmentTypes.MAIN_HAND).orElse(null);
                    } else {
                        final CClickWindowPacket packetIn = context.getPacket();
                        usedButton = packetIn.getUsedButton();
                        slot = ((InventoryAdapter) player.inventory).bridge$getSlot(packetIn.getSlotId()).orElse(null);
                    }

                    final Transaction<ItemStackSnapshot> cursorTrans = new Transaction<>(ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
                    final TrackedInventoryBridge mixinContainer = (TrackedInventoryBridge) player.openContainer;
                    final List<SlotTransaction> slotTrans = mixinContainer.bridge$getCapturedSlotTransactions();
                    final ClickInventoryEvent.Drop dropItemEvent = this.createInventoryEvent(player, ContainerUtil.fromNative(player.openContainer),
                            cursorTrans, Lists.newArrayList(slotTrans), entities,  usedButton, slot);

                    SpongeImpl.postEvent(dropItemEvent);
                    if (dropItemEvent.isCancelled() || PacketPhaseUtil.allTransactionsInvalid(dropItemEvent.getTransactions())) {
                        ((ServerPlayerEntityBridge) player).bridge$restorePacketItem(Hand.MAIN_HAND);
                        PacketPhaseUtil.handleSlotRestore(player, player.openContainer, dropItemEvent.getTransactions(), true);
                    } else {
                        processSpawnedEntities(player, dropItemEvent);
                    }
                    for (Entity entity : entities) {
                        if (((EntityBridge) entity).bridge$isConstructing()) {
                            ((EntityBridge) entity).bridge$fireConstructors();
                        }
                    }
                    slotTrans.clear();
                    mixinContainer.bridge$setCaptureInventory(false);
                });
            context.getPerEntityItemDropSupplier()
                .acceptAndClearIfNotEmpty(itemMapping -> {

                });
            final TrackedInventoryBridge mixinContainer = (TrackedInventoryBridge) player.openContainer;
            mixinContainer.bridge$setCaptureInventory(false);
            mixinContainer.bridge$getCapturedSlotTransactions().clear();
        }
    }

    @Override
    public ClickInventoryEvent.Drop createInventoryEvent(final ServerPlayerEntity playerMP, final Container openContainer, final Transaction<ItemStackSnapshot> transaction,
            final List<SlotTransaction> slotTransactions, final List<Entity> capturedEntities, final int usedButton, @Nullable final Slot slot) {
        try (final StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
            for (final Entity currentEntity : capturedEntities) {
                if (currentEntity instanceof OwnershipTrackedBridge) {
                    ((OwnershipTrackedBridge) currentEntity).tracked$setOwnerReference((Player) playerMP);
                } else {
                    currentEntity.setCreator(playerMP.getUniqueID());
                }            }

            // A 'primary click' is used by the game to indicate a single drop (e.g. pressing 'q' without holding 'control')
            final ClickInventoryEvent.Drop event;
            if (usedButton == Constants.Networking.PACKET_BUTTON_PRIMARY_ID) {
                event = SpongeEventFactory.createClickInventoryEventDropSingle(Sponge.getCauseStackManager().getCurrentCause(),
                        transaction, capturedEntities, Optional.ofNullable(slot), openContainer, slotTransactions);
            } else {
                event = SpongeEventFactory.createClickInventoryEventDropFull(Sponge.getCauseStackManager().getCurrentCause(),
                        transaction, capturedEntities, Optional.ofNullable(slot), openContainer, slotTransactions);
            }
            // TODO the nature of how this event is handled prevents the cause information being preserved through
            // the event call, somehow should not release this frame until after the event is posted
            return event;
        }
    }

    @Override
    public boolean ignoresItemPreMerging() {
        return true;
    }

}
