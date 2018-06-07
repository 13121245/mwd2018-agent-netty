package models;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;

public class RpcRequestHolder {

    // key: requestId     value: RpcFuture
    private static ConcurrentHashMap<Long,ChannelHandlerContext> processingRpc = new ConcurrentHashMap<>();

    public static void put(long requestId, ChannelHandlerContext ctx){
        processingRpc.put(requestId,ctx);
    }

    public static ChannelHandlerContext get(long requestId){
        return processingRpc.get(requestId);
    }

    public static void remove(long requestId){
        processingRpc.remove(requestId);
    }
}
