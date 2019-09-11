package org.knirxot.flume;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.knirxot.flume.response.CssResponse;
import org.knirxot.flume.response.JavaScriptResponse;
import org.knirxot.flume.response.NotFoundResponse;

/**
 * Created by xw on 2019/9/4.
 */
public class FlumeAvroResourceHandler extends FlumeAvroBaseHandler {

    @Inject
    protected FlumeAvroResourceHandler(Settings settings, Client client, RestController restController) {
        super(settings, restController, client);
        registerHandler("GET", "/_flume/js/${js_file}");
        registerHandler("GET", "/_flume/css/${css_file}");
    }

    @Override
    protected void handleRequest(RestRequest request, RestChannel channel, Client client) throws Exception {
        if (request.path().endsWith(".js")) {
            String file = request.param("js_file");
            channel.sendResponse(new JavaScriptResponse(request, "/_flume/js/" + file));
        } else if (request.path().endsWith(".css")) {
            String file = request.param("css_file");
            channel.sendResponse(new CssResponse(request, "/_flume/css/" + file));
        } else {
            channel.sendResponse(new NotFoundResponse());
        }
    }
}
