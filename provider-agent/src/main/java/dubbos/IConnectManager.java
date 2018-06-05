package dubbos;

import io.netty.channel.Channel;

/**
 * Created by zjw on 2018/05/04 18:33
 * Description:
 */
public interface IConnectManager {

    Channel getChannel() throws Exception;
}
