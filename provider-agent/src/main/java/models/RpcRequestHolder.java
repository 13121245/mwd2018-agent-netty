package models;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;

public class RpcRequestHolder {

    // key: requestId     value: RpcFuture
    private static ConcurrentHashMap<String,ChannelHandlerContext> processingRpc = new ConcurrentHashMap<>();

    public static void put(String requestId, ChannelHandlerContext ctx){
        processingRpc.put(requestId,ctx);
    }

    public static ChannelHandlerContext get(String requestId){
        return processingRpc.get(requestId);
    }

    public static void remove(String requestId){
        processingRpc.remove(requestId);
    }
}
