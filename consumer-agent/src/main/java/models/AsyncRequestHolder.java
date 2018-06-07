package models;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zjw on 2018/06/05 17:02
 * Description:
 */
public class AsyncRequestHolder {

    // key: requestId,  value:
    private static ConcurrentHashMap<Long, ChannelHandlerContext> requestHolder = new ConcurrentHashMap<>();

    public static void put(long requestId, ChannelHandlerContext ctx) {
        requestHolder.put(requestId, ctx);
    }

    public static ChannelHandlerContext get(long requestId) {
        return requestHolder.get(requestId);
    }

    public static void remove(long requestId) {
        requestHolder.remove(requestId);
    }
}
