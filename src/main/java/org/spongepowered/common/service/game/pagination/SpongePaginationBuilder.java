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
package org.spongepowered.common.service.game.pagination;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.service.pagination.PaginationList;

import javax.annotation.Nullable;

public final class SpongePaginationBuilder implements PaginationList.Builder {

    static final @NonNull Component DEFAULT_PADDING = Component.text("=");

    private final SpongePaginationService service;
    @Nullable
    private Iterable<Component> contents;
    @Nullable
    private Component title;
    @Nullable
    private Component header;
    @Nullable
    private Component footer;
    private Component paginationSpacer = SpongePaginationBuilder.DEFAULT_PADDING;
    private int linesPerPage = 20;

    @Nullable
    private PaginationList paginationList;

    public SpongePaginationBuilder(final SpongePaginationService service) {
        this.service = service;
    }

    @Override
    public PaginationList.Builder contents(final Iterable<Component> contents) {
        checkNotNull(contents, "The contents cannot be null!");
        this.contents = contents;
        this.paginationList = null;
        return this;
    }

    @Override
    public PaginationList.Builder contents(final Component... contents) {
        checkNotNull(contents, "The contents cannot be null!");
        this.contents = ImmutableList.copyOf(contents);
        this.paginationList = null;
        return this;
    }

    @Override
    public PaginationList.Builder title(@Nullable final Component title) {
        this.title = title;
        this.paginationList = null;
        return this;
    }

    @Override
    public PaginationList.Builder header(@Nullable final Component header) {
        this.header = header;
        this.paginationList = null;
        return this;
    }

    @Override
    public PaginationList.Builder footer(@Nullable final Component footer) {
        this.footer = footer;
        this.paginationList = null;
        return this;
    }

    @Override
    public PaginationList.Builder padding(final Component padding) {
        checkNotNull(padding, "The padding cannot be null!");
        this.paginationSpacer = padding;
        this.paginationList = null;
        return this;
    }

    @Override
    public PaginationList.Builder linesPerPage(final int linesPerPage) {
        this.linesPerPage = linesPerPage;
        return this;
    }

    @Override
    public PaginationList build() {
        checkState(this.contents != null, "The contents of the pagination list cannot be null!");

        if (this.paginationList == null) {
            this.paginationList = new SpongePaginationList(this.service, this.contents, this.title, this.header, this.footer, this.paginationSpacer,
                    this.linesPerPage);
        }
        return this.paginationList;
    }

    @Override
    public PaginationList.Builder from(final PaginationList list) {
        this.reset();
        this.contents = list.contents();
        this.title = list.title().orElse(null);
        this.header = list.header().orElse(null);
        this.footer = list.footer().orElse(null);
        this.paginationSpacer = list.padding();

        this.paginationList = null;
        return this;
    }

    @Override
    public PaginationList.Builder reset() {
        this.contents = null;
        this.title = null;
        this.header = null;
        this.footer = null;
        this.paginationSpacer = SpongePaginationBuilder.DEFAULT_PADDING;

        this.paginationList = null;
        return this;
    }
}
