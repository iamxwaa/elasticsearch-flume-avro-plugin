package org.knirxot.flume;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.http.netty.NettyHttpChannel;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.knirxot.flume.response.PageResponse;

import java.io.IOException;

/**
 * Created by xw on 2019/9/4.
 */
public class FlumeAvroPageHandler extends FlumeAvroBaseHandler {

    @Inject
    protected FlumeAvroPageHandler(Settings settings, Client client, RestController restController) {
        super(settings, restController, client);
        registerHandler("GET", "/_flume");
    }


    @Override
    protected void handleRequest(RestRequest request, RestChannel channel, Client client) throws Exception {
        channel.sendResponse(new PageResponse(request));
    }
}
