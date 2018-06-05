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
    private static ConcurrentHashMap<String, String> processingResHashCode = new ConcurrentHashMap<>();

    public static void putString(String requestId,String str){
        processingResHashCode.put(requestId,str);
    }

    public static String getString(String requestId){
        return processingResHashCode.get(requestId);
    }

    public static void removeString(String requestId){
        processingResHashCode.remove(requestId);
    }
}
