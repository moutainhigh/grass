package com.yanglinkui.grass.client.processor;

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.client.ClientProcessor;
import com.yanglinkui.grass.client.Router;
import com.yanglinkui.grass.client.RouterChain;
import com.yanglinkui.grass.registry.Registry;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RouterProcessor implements ClientProcessor {

    private final List<Router> routerList;

    private final Registry registry;

    public RouterProcessor(List<Router> routerList, Registry registry) {
        this.routerList = routerList;
        this.registry = registry;
    }

    @Override
    public CompletableFuture<GrassResponse> process(Chain<CompletableFuture<GrassResponse>> chain) {
        GrassRequest request = chain.getRequest();
        List<ApplicationInstance> sourceApplicationInstanceList = registry.getApplicationInstanceList(request.getZone(), request.getApplicationName());

        Router.Chain routerChain = new RouterChain(request, sourceApplicationInstanceList, routerList);
        List<ApplicationInstance> applicationInstanceList = routerChain.proceed(request, sourceApplicationInstanceList);
        request.setAttribute(ProcessorConstants.Request.Attribute.APPLICATION_INSTANCE_LIST, applicationInstanceList);

        return chain.proceed(request);
    }
}
