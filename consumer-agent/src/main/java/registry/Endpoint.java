package registry;

public class Endpoint {
    private final String host;
    private final int port;
    private final String capacity;

    public Endpoint(String host, int port, String capacity) {
        this.host = host;
        this.port = port;
        this.capacity = capacity;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String toString(){
        return host + ":" + port;
    }

    public boolean equals(Object o){
        if (!(o instanceof Endpoint)){
            return false;
        }
        Endpoint other = (Endpoint) o;
        return other.host.equals(this.host) && other.port == this.port;
    }

    public int hashCode(){
        return host.hashCode() + port;
    }

    public String getCapacity() {
        return capacity;
    }
}
