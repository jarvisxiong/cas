package com.inmobi.adserve.channels.util.Utils;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.googlecode.cqengine.resultset.ResultSet;

public final class CQEngineUtils {

    /**
     * Returns an ImmutableSet of Type T given a CQEngine Result Set of Type V and a mapping function that maps
     * Type T to Type V.
     *
     * Do note that the elements of the underlying copy can still be changed.
     */
    public static <T, V> Set<T> getImmutableCopyOfMappedResultSet(final ResultSet<V> rs, final Function<V, T> func) {
        final Iterator<T> itr = Sets.newHashSet(rs.iterator()).stream().map(func).iterator();
        return ImmutableSet.copyOf(itr);
    }
}
