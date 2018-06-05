import nettys.CAServer;
import registry.EndpointUtil;
import registry.EtcdRegistry;
import registry.IRegistry;

/**
 * Created by zjw on 2018/06/01 14:34
 * Description:
 */
public class ConsumerAgent {

    public static void main(String[] args) throws Exception {
        CAServer server = new CAServer(Integer.valueOf(System.getProperty("server.port")));
        server.startServer();
    }

}
