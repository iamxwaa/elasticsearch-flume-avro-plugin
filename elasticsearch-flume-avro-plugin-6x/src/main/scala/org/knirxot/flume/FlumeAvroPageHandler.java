package org.knirxot.flume;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.knirxot.flume.response.PageResponse;

import java.io.IOException;

/**
 * Created by xw on 2019/9/4.
 */
public class FlumeAvroPageHandler extends FlumeAvroBaseHandler {

    protected FlumeAvroPageHandler(Settings settings, RestController restController) {
        super(settings, restController);
        registerHandler("GET", "/_flume");
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        return channel -> channel.sendResponse(new PageResponse(request));
    }
}
