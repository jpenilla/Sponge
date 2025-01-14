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
package org.spongepowered.common.command.brigadier.context;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.tree.CommandNode;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.command.parameter.SpongeParameterKey;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import net.minecraft.commands.CommandSourceStack;

public final class SpongeCommandContextBuilderTransaction implements CommandContext.Builder.Transaction {

    private static final LinkedList<SpongeCommandContextBuilderTransaction> TRANSACTION_POOL = new LinkedList<>();

    public static SpongeCommandContextBuilderTransaction getTransactionFromPool(final SpongeCommandContextBuilder builder) {
        SpongeCommandContextBuilderTransaction chosenTransaction = null;
        for (final SpongeCommandContextBuilderTransaction transaction : SpongeCommandContextBuilderTransaction.TRANSACTION_POOL) {
            // isActive does GC checks.
            if (!transaction.isActive() && chosenTransaction == null) {
                chosenTransaction = transaction.activateTransaction(builder);
            }
        }

        if (chosenTransaction != null) {
            return chosenTransaction;
        }

        final SpongeCommandContextBuilderTransaction transaction = new SpongeCommandContextBuilderTransaction().activateTransaction(builder);
        SpongeCommandContextBuilderTransaction.TRANSACTION_POOL.add(transaction);
        return transaction;
    }

    // weak in case someone keeps the transaction to hand... for some reason.
    private WeakReference<SpongeCommandContextBuilder> builder;
    private SpongeCommandContextBuilder copyBuilder;

    private final Object2IntOpenHashMap<String> flagCapture = new Object2IntOpenHashMap<>();
    private final LinkedList<ArgumentCapture> withArgumentCapture = new LinkedList<>();
    private final LinkedList<CommandSourceStack> withSourceCapture = new LinkedList<>();
    private final LinkedList<Tuple<CommandNode<CommandSourceStack>, StringRange>> withNodeCapture = new LinkedList<>();
    private final LinkedList<Tuple<Parameter.@NonNull Key<?>, ?>> putEntryCapture = new LinkedList<>();
    private final LinkedList<CommandContextBuilder<CommandSourceStack>> withChildCapture = new LinkedList<>();
    private final LinkedList<Command<CommandSourceStack>> withCommandCapture = new LinkedList<>();
    private org.spongepowered.api.command.Command.Parameterized currentTargetCommandCapture = null;

    private boolean isActive = false;

    private SpongeCommandContextBuilderTransaction() {
        // no-op
    }

    private SpongeCommandContextBuilderTransaction activateTransaction(final SpongeCommandContextBuilder builder) {
        if (this.isActive()) {
            throw new IllegalStateException("Transaction is already active.");
        }

        this.builder = new WeakReference<>(builder);
        this.copyBuilder = builder.copy();
        return this;
    }

    private SpongeCommandContextBuilder getReference() {
        if (this.isActive) {
            final SpongeCommandContextBuilder builder = this.builder.get();
            if (builder != null) {
                return builder;
            }
            this.rollback();
        }
        throw new IllegalStateException("Transaction is not active.");
    }

    public SpongeCommandContextBuilder withArgument(final String name, final ParsedArgument<CommandSourceStack, ?> argument, final boolean addToSpongeMap) {
        final SpongeCommandContextBuilder builder = this.getReference();
        this.withArgumentCapture.add(new ArgumentCapture(name, argument, addToSpongeMap));
        this.copyBuilder.withArgumentInternal(name, argument, addToSpongeMap);
        return builder;
    }

    public SpongeCommandContextBuilder withSource(final CommandSourceStack source) {
        final SpongeCommandContextBuilder builder = this.getReference();
        this.withSourceCapture.add(source);
        this.copyBuilder.withSource(source);
        return builder;
    }

    public SpongeCommandContextBuilder withNode(final CommandNode<CommandSourceStack> node, final StringRange range) {
        final SpongeCommandContextBuilder builder = this.getReference();
        this.withNodeCapture.add(Tuple.of(node, range));
        this.copyBuilder.withNode(node, range);
        return builder;
    }

    public SpongeCommandContextBuilder withChild(final CommandContextBuilder<CommandSourceStack> child) {
        final SpongeCommandContextBuilder builder = this.getReference();
        this.withChildCapture.add(child);
        this.copyBuilder.withChild(child);
        return builder;
    }

    public CommandContextBuilder<CommandSourceStack> withCommand(final Command<CommandSourceStack> command) {
        final SpongeCommandContextBuilder builder = this.getReference();
        this.withCommandCapture.add(command);
        this.copyBuilder.withCommand(command);
        return builder;
    }

    public <T> void putEntry(final Parameter.@NonNull Key<? super T> key, @NonNull final T object) {
        this.getReference();
        this.putEntryCapture.add(Tuple.of(key, object));
        this.copyBuilder.putEntry(key, object);
    }

    @SuppressWarnings("unchecked")
    private <T> void putEntryAbusingGenerics(@NonNull final SpongeCommandContextBuilder builderRef,
            final Parameter.@NonNull Key<?> key,
            @NonNull final T object) {
        builderRef.putEntry((SpongeParameterKey<? super T>) key, object);
    }

    public void addFlagInvocation(final Flag flag) {
        flag.unprefixedAliases().forEach(x -> this.flagCapture.addTo(x, 1));
        this.copyBuilder.addFlagInvocation(flag);
    }

    public SpongeCommandContextBuilder getCopyBuilder() {
        return this.copyBuilder;
    }

    public void setCurrentTargetCommand(final org.spongepowered.api.command.Command.Parameterized command) {
        this.currentTargetCommandCapture = command;
    }

    public boolean isActive() {
        if (this.isActive) {
            if (this.builder.get() != null) {
                return true;
            }
            this.rollback();
        }
        return false;
    }

    public void commit() {
        final SpongeCommandContextBuilder builderRef = this.builder.get();
        if (builderRef != null) {
            this.withArgumentCapture.forEach(x -> builderRef.withArgumentInternal(x.name, x.parsedArgument, x.addToSponge));
            this.withSourceCapture.forEach(builderRef::withSource);
            this.withNodeCapture.forEach(x -> builderRef.withNode(x.first(), x.second()));
            this.withChildCapture.forEach(builderRef::withChild);
            this.withCommandCapture.forEach(builderRef::withCommand);
            this.putEntryCapture.forEach(x -> this.putEntryAbusingGenerics(builderRef, x.first(), x.second()));
            this.flagCapture.forEach(builderRef::addFlagInvocation);
            if (this.currentTargetCommandCapture != null) {
                builderRef.setCurrentTargetCommand(this.currentTargetCommandCapture);
            }
        }

        // we're clearing anyway!
        this.rollback();
    }

    public void rollback() {
        this.isActive = false;
        this.withArgumentCapture.clear();
        this.withNodeCapture.clear();
        this.withCommandCapture.clear();
        this.withChildCapture.clear();
        this.putEntryCapture.clear();
        this.flagCapture.clear();
        this.currentTargetCommandCapture = null;
        this.copyBuilder = null;
        this.builder = null;
    }

    static final class ArgumentCapture {

        public final String name;
        public final ParsedArgument<CommandSourceStack, ?> parsedArgument;
        public final boolean addToSponge;

        ArgumentCapture(final String name, final ParsedArgument<CommandSourceStack, ?> parsedArgument, final boolean addToSponge) {
            this.name = name;
            this.parsedArgument = parsedArgument;
            this.addToSponge = addToSponge;
        }

    }

}
