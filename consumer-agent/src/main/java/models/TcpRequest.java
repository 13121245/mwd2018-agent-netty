package models;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zjw on 2018/06/02 16:39
 * Description:
 */
public class TcpRequest {

    @JsonIgnore
    private static final AtomicLong atomicLong = new AtomicLong();

    private long id;
    private String parameter;

    public TcpRequest() {

    }

    public TcpRequest(String parameter) {
        this.id = atomicLong.getAndIncrement();
        this.parameter = parameter;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        return "TcpRequest{" +
                "id=" + id +
                ", parameter='" + parameter + '\'' +
                '}';
    }
}
