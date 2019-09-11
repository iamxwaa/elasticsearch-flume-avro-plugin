package org.knirxot.flume;

import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestModule;

/**
 * Created by xw on 2019/9/10.
 */
public class FlumeAvroPlugin extends Plugin {

    public FlumeAvroPlugin() {
    }

    @Override
    public String name() {
        return "flume-avro";
    }

    @Override
    public String description() {
        return "Export index with flume avro agent";
    }

    public void onModule(RestModule module) {
        module.addRestAction(FlumeAvroPageHandler.class);
        module.addRestAction(FlumeAvroRestHandler.class);
        module.addRestAction(FlumeAvroResourceHandler.class);
    }
}
