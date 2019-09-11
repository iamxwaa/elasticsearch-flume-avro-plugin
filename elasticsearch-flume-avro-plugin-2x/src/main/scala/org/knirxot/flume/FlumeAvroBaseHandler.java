package org.knirxot.flume;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xw on 2019/9/4.
 */
public abstract class FlumeAvroBaseHandler extends BaseRestHandler {
    private final RestController restController;

    @Inject
    protected FlumeAvroBaseHandler(Settings settings, RestController restController, Client client) {
        super(settings, restController, client);
        this.restController = restController;
    }

    /**
     * es会对请求参数做校验,所以要重新获取参数
     *
     * @param request
     * @return
     */
    protected Map<String, String> buildParamMap(RestRequest request) {
        Map<String, String> params = new HashMap<>(request.params().size());
        request.params().forEach((k, v) -> {
            request.param(k);
            params.put(k, v);
        });
        return params;
    }

    protected void registerHandler(String method, String path) {
        logger.info("regist " + method + "#" + path + "#" + this.getClass().getSimpleName());
        restController.registerHandler(RestRequest.Method.valueOf(method), path, this);
    }
}
