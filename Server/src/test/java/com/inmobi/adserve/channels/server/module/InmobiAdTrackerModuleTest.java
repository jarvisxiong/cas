package com.inmobi.adserve.channels.server.module;

import static junit.framework.TestCase.fail;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.trackers.DefaultLazyInmobiAdTrackerBuilder;
import com.inmobi.adserve.channels.api.trackers.DefaultLazyInmobiAdTrackerBuilderFactory;
import com.inmobi.adserve.channels.api.trackers.InmobiAdTrackerBuilder;
import com.inmobi.adserve.channels.api.trackers.InmobiAdTrackerBuilderFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RunWith(Parameterized.class)
public class InmobiAdTrackerModuleTest {
    private static final int GUICE_DEFAULT_BINDINGS_COUNT = 3;

    private final Class<? extends Annotation> builderFactoryAnnotation;
    @SuppressWarnings("rawtypes")
    private final Class builderClass;
    private static Injector injector;

    public static SASRequestParameters mockSasParams;

    static {
        injector = Guice.createInjector(new InmobiAdTrackerModule());
        if (injector.getBindings().size() != GUICE_DEFAULT_BINDINGS_COUNT + data().size()) {
            fail("Not all bindings have been tested. Please add them to the data method in InmobiAdTrackerModuleTest.");
        }
    }

    @BeforeClass
    public static void setUp() {
        mockSasParams = createNiceMock(SASRequestParameters.class);
        expect(mockSasParams.getCarrierId()).andReturn(0).anyTimes();
        expect(mockSasParams.getIpFileVersion()).andReturn(0).anyTimes();
        replay(mockSasParams);
    }

    @Parameters
    public static Collection<Object> data() {
        return Arrays.asList(new Object[][] {
                {DefaultLazyInmobiAdTrackerBuilderFactory.class, DefaultLazyInmobiAdTrackerBuilder.class}
        });
    }

    @Test
    public void testDefaultLazyInmobiAdTrackerBuilderFactory() {
        final InmobiAdTrackerBuilderFactory inmobiAdTrackerBuilderFactory = injector.getInstance(
                Key.get(InmobiAdTrackerBuilderFactory.class, builderFactoryAnnotation));

        final InmobiAdTrackerBuilder builder = inmobiAdTrackerBuilderFactory.getBuilder(mockSasParams, "", true);
        assertThat(builder.getClass(), is(equalTo(builderClass)));
    }

}