package org.knirxot.flume;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.knirxot.flume.response.CssResponse;
import org.knirxot.flume.response.JavaScriptResponse;
import org.knirxot.flume.response.NotFoundResponse;

import java.io.IOException;

/**
 * Created by xw on 2019/9/4.
 */
public class FlumeAvroResourceHandler extends FlumeAvroBaseHandler {

    protected FlumeAvroResourceHandler(Settings settings, RestController restController) {
        super(settings, restController);
        registerHandler("GET", "/_flume/js/${js_file}");
        registerHandler("GET", "/_flume/css/${css_file}");
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        if (request.path().endsWith(".js")) {
            String file = request.param("js_file");
            return channel -> channel.sendResponse(new JavaScriptResponse(request, "/_flume/js/" + file));
        } else if (request.path().endsWith(".css")) {
            String file = request.param("css_file");
            return channel -> channel.sendResponse(new CssResponse(request, "/_flume/css/" + file));
        } else {
            return channel -> channel.sendResponse(new NotFoundResponse());
        }
    }
}
