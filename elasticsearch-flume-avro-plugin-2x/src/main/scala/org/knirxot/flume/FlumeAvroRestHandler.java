package org.knirxot.flume;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.knirxot.flume.agent.config.Config;
import org.knirxot.flume.agent.config.DataInfo;
import org.knirxot.flume.client.SearchClient;
import org.knirxot.flume.response.RestOkResponse;
import org.knirxot.flume.response.UnknownResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xw on 2019/9/4.
 */
public class FlumeAvroRestHandler extends FlumeAvroBaseHandler {
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private SearchClient searchClient;

    @Inject
    protected FlumeAvroRestHandler(Settings settings, Client client, RestController restController) {
        super(settings, restController, client);
        registerHandler("GET", "/_flume/export");
        registerHandler("GET", "/_flume/stop");
        registerHandler("GET", "/_flume/info");
        registerHandler("GET", "/_flume/config");
    }

    @Override
    protected void handleRequest(RestRequest request, RestChannel channel, Client client) throws Exception {
        if (request.path().endsWith("/info")) {
            if (null == searchClient) {
                channel.sendResponse(new RestOkResponse(new DataInfo().toString()));
            } else {
                channel.sendResponse(new RestOkResponse(searchClient.getDataInfo().toString()));
            }
        } else if (request.path().endsWith("/config")) {
            if (null == searchClient) {
                channel.sendResponse(new RestOkResponse(new Config(request.params()).toString()));
            } else {
                channel.sendResponse(new RestOkResponse(searchClient.getConfig().toString()));
            }
        } else if (request.path().endsWith("/export")) {
            if (null != searchClient) {
                if (!searchClient.getDataInfo().isFinish()) {
                    channel.sendResponse(new RestOkResponse("{\"status\":\"RUNNING\"}"));
                    return;
                }
            }
            Config config = new Config(buildParamMap(request));
            if (config.check()) {
                searchClient = new SearchClient(client, settings, config);
                exec.execute(searchClient);
                channel.sendResponse(new RestOkResponse());
            } else {
                logger.warn("/_flume/export with bad parameters");
                channel.sendResponse(new UnknownResponse());
            }
        } else if (request.path().endsWith("/stop")) {
            if (null != searchClient) {
                searchClient.stop();
                channel.sendResponse(new RestOkResponse("{\"status\":\"STOP\"}"));
            } else {
                channel.sendResponse(new RestOkResponse("{\"status\":\"NOT RUNNING\"}"));
            }
        } else {
            channel.sendResponse(new UnknownResponse());
        }
    }
}
