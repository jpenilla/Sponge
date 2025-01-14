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
package org.spongepowered.common.advancement.criterion;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.OperatorCriterion;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTrigger;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class SpongeOperatorCriterion implements OperatorCriterion, DefaultedAdvancementCriterion {

    private final String name;
    private final Collection<AdvancementCriterion> criteria;

    @Nullable private Collection<AdvancementCriterion> recursiveChildrenCriteria;
    @Nullable private Collection<AdvancementCriterion> leafChildrenCriteria;

    SpongeOperatorCriterion(final String namePrefix, final Collection<AdvancementCriterion> criteria) {
        this.name = namePrefix + Arrays.toString(criteria.stream().map(AdvancementCriterion::name).toArray(String[]::new));
        this.criteria = ImmutableSet.copyOf(criteria);
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Optional<FilteredTrigger<?>> trigger() {
        return Optional.empty();
    }

    private Collection<AdvancementCriterion> getAllChildrenCriteria0(final boolean onlyLeaves) {
        final ImmutableSet.Builder<AdvancementCriterion> criteria = ImmutableSet.builder();
        if (!onlyLeaves) {
            criteria.add(this);
        }
        for (final AdvancementCriterion criterion : this.criteria) {
            if (criterion instanceof OperatorCriterion) {
                criteria.addAll(((SpongeOperatorCriterion) criterion).getAllChildrenCriteria0(onlyLeaves));
            }
        }
        return criteria.build();
    }

    private Collection<AdvancementCriterion> getRecursiveChildren() {
        if (this.recursiveChildrenCriteria == null) {
            this.recursiveChildrenCriteria = this.getAllChildrenCriteria0(false);
        }
        return this.recursiveChildrenCriteria;
    }

    @Override
    public Collection<AdvancementCriterion> criteria() {
        return this.criteria;
    }

    @Override
    public Collection<AdvancementCriterion> leafCriteria() {
        if (this.leafChildrenCriteria == null) {
            this.leafChildrenCriteria = this.getAllChildrenCriteria0(true);
        }
        return this.leafChildrenCriteria;
    }

    @Override
    public Collection<AdvancementCriterion> find(final String name) {
        return this.getRecursiveChildren().stream()
                .filter(c -> c.name().equals(name)).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Optional<AdvancementCriterion> findFirst(final String name) {
        return this.getRecursiveChildren().stream()
                .filter(c -> c.name().equals(name)).findFirst();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpongeOperatorCriterion)) {
            return false;
        }

        SpongeOperatorCriterion that = (SpongeOperatorCriterion) o;

        if (!Objects.equals(this.name, that.name)) {
            return false;
        }
        return Objects.equals(this.criteria, that.criteria);
    }

    @Override
    public int hashCode() {
        int result = this.name != null ? this.name.hashCode() : 0;
        result = 31 * result + (this.criteria != null ? this.criteria.hashCode() : 0);
        return result;
    }
}
