package registry;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zjw on 2018/05/12 17:22
 * Description:
 */
public class EndpointUtil {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(EndpointUtil.class);

    private List<Endpoint> endpoints = null;
    private IRegistry registry = null;
    private Random random = new Random();
    private static final Object lock = new Object();
    private ArrayList<Integer> dice = new ArrayList<>();


    public EndpointUtil(IRegistry registry) {
        this.registry = registry;
    }

    public Endpoint getPoint() throws Exception{
        if (null == endpoints) {
            synchronized (lock) {
                if (null == endpoints) {
                    endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
                    for (int i = 0; i < endpoints.size(); i++) {
                        if (endpoints.get(i).getPort() == 30000) {
                            dice.add(i);
                            logger.info("fix endpoint:" + endpoints.get(i).getPort() + "on " + i);
                        } else if (endpoints.get(i).getPort() == 30001) {
                            logger.info("fix endpoint:" + endpoints.get(i).getPort() + "on " + i);
                            dice.add(i);
                            dice.add(i);
                        } else {
                            logger.info("fix endpoint:" + endpoints.get(i).getPort() + "on " + i);
                            dice.add(i);
                            dice.add(i);
                            dice.add(i);
                        }
                    }
                }
            }
        }

        // 加权随机
        int pos = dice.get(random.nextInt(dice.size()));
        return endpoints.get(pos);
    }
}
