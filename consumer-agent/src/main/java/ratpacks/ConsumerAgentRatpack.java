package ratpacks;

import okhttp3.OkHttpClient;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;
import registry.EndpointUtil;
import registry.EtcdRegistry;
import registry.IRegistry;


/**
 * Created by zjw on 2018/05/11 14:34
 * Description:
 */
public class ConsumerAgentRatpack {

    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private EndpointUtil endpointUtil = new EndpointUtil(registry);
    private OkHttpClient httpClient = new OkHttpClient();


    private void startServer() throws Exception{
        RatpackServer.start(server -> server.serverConfig(ServerConfig.embedded().port(20000))
                .registryOf(registry -> registry.add("world"))
                .handlers(chain -> chain.post(new MyHandler(endpointUtil, httpClient))
                ));
    }


    public static void main(String[] args) throws Exception{
        ConsumerAgentRatpack ca = new ConsumerAgentRatpack();
        ca.startServer();
    }

}
