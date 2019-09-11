package org.knirxot.flume

import java.util
import java.util.function.Supplier

import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver
import org.elasticsearch.cluster.node.DiscoveryNodes
import org.elasticsearch.common.settings.{ClusterSettings, IndexScopedSettings, Settings, SettingsFilter}
import org.elasticsearch.plugins.{ActionPlugin, Plugin}
import org.elasticsearch.rest.{RestController, RestHandler}

/**
 * Created by xw on 2019/9/3.
 */
class FlumeAvroPlugin extends Plugin with ActionPlugin {
  override def getRestHandlers(settings: Settings,
                               restController: RestController,
                               clusterSettings: ClusterSettings,
                               indexScopedSettings: IndexScopedSettings,
                               settingsFilter: SettingsFilter,
                               indexNameExpressionResolver: IndexNameExpressionResolver,
                               nodesInCluster: Supplier[DiscoveryNodes]): util.List[RestHandler] = {
    val list = new util.ArrayList[RestHandler]()
    list.add(new FlumeAvroPageHandler(settings, restController))
    list.add(new FlumeAvroRestHandler(settings, restController))
    list.add(new FlumeAvroResourceHandler(settings, restController))
    list
  }
}