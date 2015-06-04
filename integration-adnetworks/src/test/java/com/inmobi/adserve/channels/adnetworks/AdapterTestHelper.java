package com.inmobi.adserve.channels.adnetworks;

import org.powermock.api.support.membermodification.MemberModifier;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.trackers.DefaultLazyInmobiAdTrackerBuilder;
import com.inmobi.adserve.channels.api.trackers.DefaultLazyInmobiAdTrackerBuilderFactory;
import com.inmobi.adserve.channels.api.trackers.InmobiAdTrackerBuilder;
import com.inmobi.adserve.channels.api.trackers.InmobiAdTrackerBuilderFactory;

/**
 * Created by ishanbhatnagar on 19/5/15.
 */
public class AdapterTestHelper {
    public static void setInmobiAdTrackerBuilderFactoryForTest(BaseAdNetworkImpl instance) {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new FactoryModuleBuilder()
                        .implement(InmobiAdTrackerBuilder.class, DefaultLazyInmobiAdTrackerBuilder.class)
                        .build(Key.get(InmobiAdTrackerBuilderFactory.class,
                                DefaultLazyInmobiAdTrackerBuilderFactory.class)));
            }
        });
        try {
            MemberModifier.field(BaseAdNetworkImpl.class, "inmobiAdTrackerBuilderFactory")
                    .set(instance, injector.getInstance(
                            Key.get(InmobiAdTrackerBuilderFactory.class, DefaultLazyInmobiAdTrackerBuilderFactory.class)));
        } catch (IllegalAccessException ignored) {
            // Ignored
        }
    }

    public static void setBeaconAndClickStubs() {
        try {
            MemberModifier.suppress(BaseAdNetworkImpl.class.getDeclaredMethod("buildInmobiAdTracker"));
            MemberModifier.stub(BaseAdNetworkImpl.class.getDeclaredMethod("getBeaconUrl")).toReturn("beaconUrl");
            MemberModifier.stub(BaseAdNetworkImpl.class.getDeclaredMethod("getClickUrl")).toReturn("clickUrl");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

}
