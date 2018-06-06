package models;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zjw on 2018/06/05 17:02
 * Description:
 */
public class AsyncRequestHolder {

    // key: requestId,  value:
    private static ConcurrentHashMap<String, ChannelHandlerContext> requestHolder = new ConcurrentHashMap<>();

    public static void put(String requestId, ChannelHandlerContext ctx) {
        requestHolder.put(requestId, ctx);
    }

    public static ChannelHandlerContext get(String requestId) {
        return requestHolder.get(requestId);
    }

    public static void remove(String requestId) {
        requestHolder.remove(requestId);
    }
}
