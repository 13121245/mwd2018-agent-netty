package models;

import java.util.concurrent.ConcurrentHashMap;

public class TcpRequestHolder {

    // key: requestId     value: TpcFuture
    private static ConcurrentHashMap<String, TcpFuture> processingRpc = new ConcurrentHashMap<>();

    public static void put(String requestId, TcpFuture tcpFuture){
        processingRpc.put(requestId, tcpFuture);
    }

    public static TcpFuture get(String requestId){
        return processingRpc.get(requestId);
    }

    public static void remove(String requestId){
        processingRpc.remove(requestId);
    }
}
