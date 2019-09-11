package org.knirxot.flume;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.knirxot.flume.client.SearchClient;
import org.knirxot.flume.agent.config.Config;
import org.knirxot.flume.agent.config.DataInfo;
import org.knirxot.flume.response.RestOkResponse;
import org.knirxot.flume.response.UnknownResponse;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xw on 2019/9/4.
 */
public class FlumeAvroRestHandler extends FlumeAvroBaseHandler {
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private SearchClient searchClient;

    protected FlumeAvroRestHandler(Settings settings, RestController restController) {
        super(settings, restController);
        registerHandler("GET", "/_flume/export");
        registerHandler("GET", "/_flume/info");
        registerHandler("GET", "/_flume/config");
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        if (request.path().endsWith("/info")) {
            if (null == searchClient) {
                return channel -> channel.sendResponse(new RestOkResponse(new DataInfo().toString()));
            } else {
                return channel -> channel.sendResponse(new RestOkResponse(searchClient.getDataInfo().toString()));
            }
        }
        if (request.path().endsWith("/config")) {
            if (null == searchClient) {
                return channel -> channel.sendResponse(new RestOkResponse(new Config(request.params()).toString()));
            } else {
                return channel -> channel.sendResponse(new RestOkResponse(searchClient.getConfig().toString()));
            }
        }
        if (request.path().endsWith("/export")) {
            if (null != searchClient) {
                if (!searchClient.getDataInfo().isFinish()) {
                    return channel -> channel.sendResponse(new RestOkResponse("{\"status\":\"RUNNING\"}"));
                }
            }
            Config config = new Config(buildParamMap(request));
            if (config.check()) {
                searchClient = new SearchClient(client, settings, config);
                exec.execute(searchClient);
                return channel -> channel.sendResponse(new RestOkResponse());
            }
            logger.warn("/_flume/export with bad parameters");
        }
        return channel -> channel.sendResponse(new UnknownResponse());
    }
}
