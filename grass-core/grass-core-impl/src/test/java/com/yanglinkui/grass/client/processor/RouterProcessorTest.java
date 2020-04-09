package com.yanglinkui.grass.client.processor;

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.DefaultGrassRequest;
import com.yanglinkui.grass.mock.*;
import com.yanglinkui.grass.client.Router;
import com.yanglinkui.grass.DefaultGrassResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;


public class RouterProcessorTest {

    Processor.Chain<CompletableFuture<GrassResponse>> chain;

    MockRegistry registry;

    private String zone = "zone-test";

    private String application = "app3";

    @Test
    public void testProcess() {
        ApplicationInstance instance = new MockDefaultApplicationInstance(zone, application, MockProtocolFactory.ID);
        List<ApplicationInstance> applicationInstanceList = new ArrayList<>();
        applicationInstanceList.add(instance);

        List<Router> routerList = new ArrayList<>();
        routerList.add(new TestRouter());

        registry = new MockRegistry();
        registry.setApplicationInstanceList(applicationInstanceList);

        DefaultGrassRequest request = new DefaultGrassRequest();
        request.setZone(zone);
        request.setApplicationName(application);

        chain = new MockProcessorChain(request, CompletableFuture.completedFuture(new DefaultGrassResponse()));
        RouterProcessor processor = new RouterProcessor(routerList, registry);
        processor.process(chain);

        List<ApplicationInstance> result = request.getAttribute(ProcessorConstants.Request.Attribute.APPLICATION_INSTANCE_LIST);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(instance, result.get(0));

    }

    private static class TestRouter implements Router {
        @Override
        public List<ApplicationInstance> choose(Chain chain) {
            return chain.getApplicationInstanceList();
        }
    }

}
