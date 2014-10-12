package com.inmobi.adserve.channels.server.servlet;

import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.repository.NativeAdTemplateRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.util.Utils.TestUtils;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NativeAdTemplateRepository.class, NativeAdTemplateEntity.class})
public class ServletTemplateTest {

    private Map<String, List<String>> createMapFromString(String siteId) {
        Map<String, List<String>> params = new HashMap<>();
        if(null != siteId) {
            params.put("siteId", Arrays.asList(siteId));
        }
        return params;
    }

    @Test
    public void testHandleRequestSiteIdFoundEntityNotNull() throws Exception {
        String siteId = TestUtils.SampleStrings.siteId;

        mockStatic(CasConfigUtil.class);
        RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
        HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        ResponseSender mockResponseSender = createMock(ResponseSender.class);
        NativeAdTemplateRepository mockTemplateRepository = createMock(NativeAdTemplateRepository.class);
        NativeAdTemplateEntity mockEntity = createMock(NativeAdTemplateEntity.class);

        expect(mockQueryStringDecoder.parameters())
                .andReturn(createMapFromString(siteId)).times(1);
        expect(mockRepositoryHelper.getNativeAdTemplateRepository()).andReturn(mockTemplateRepository).times(1);
        expect(mockTemplateRepository.query(siteId)).andReturn(mockEntity).times(1);
        expect(mockEntity.getJSON()).andReturn("{dummy}").times(1);
        mockResponseSender.sendResponse("{dummy}", null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;
        CasConfigUtil.repositoryHelper = mockRepositoryHelper;

        ServletTemplate tested = new ServletTemplate();
        tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

        verifyAll();
    }

    @Test
    public void testHandleRequestSiteIdFoundEntityNull() throws Exception {
        String siteId = TestUtils.SampleStrings.siteId;

        mockStatic(CasConfigUtil.class);
        RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
        HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        ResponseSender mockResponseSender = createMock(ResponseSender.class);
        NativeAdTemplateRepository mockTemplateRepository = createMock(NativeAdTemplateRepository.class);

        expect(mockQueryStringDecoder.parameters())
                .andReturn(createMapFromString(siteId)).times(1);
        expect(mockRepositoryHelper.getNativeAdTemplateRepository()).andReturn(mockTemplateRepository).times(1);
        expect(mockTemplateRepository.query(siteId)).andReturn(null).times(1);
        mockResponseSender.sendResponse("No template found for site Id " + siteId, null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;
        CasConfigUtil.repositoryHelper = mockRepositoryHelper;

        ServletTemplate tested = new ServletTemplate();
        tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

        verifyAll();
    }

    @Test
    public void testHandleRequestSiteIdNotFound() throws Exception {
        QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
        HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        ResponseSender mockResponseSender = createMock(ResponseSender.class);

        expect(mockQueryStringDecoder.parameters())
                .andReturn(createMapFromString(null)).times(1);
        mockResponseSender.sendResponse("Invalid siteId", null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;

        ServletTemplate tested = new ServletTemplate();
        tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        ServletTemplate tested = new ServletTemplate();
        assertThat(tested.getName(), is(IsEqual.equalTo("template")));
    }
}