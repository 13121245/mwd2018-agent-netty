package models;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;

public class RpcRequestHolder {

    // key: requestId     value: RpcFuture
    private static ConcurrentHashMap<String,ChannelHandlerContext> processingRpc = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> processingResHashCode = new ConcurrentHashMap<>();

    public static void put(String requestId, ChannelHandlerContext ctx){
        processingRpc.put(requestId,ctx);
    }

    public static ChannelHandlerContext get(String requestId){
        return processingRpc.get(requestId);
    }

    public static void remove(String requestId){
        processingRpc.remove(requestId);
    }

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
