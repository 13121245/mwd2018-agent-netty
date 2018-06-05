package models;


import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zjw on 2018/06/02 16:39
 * Description:
 */
public class TcpRequest {

    private static final AtomicLong atomicLong = new AtomicLong();

    private long id;
    private String interfaceName;
    private String methodName;
    private String parameterTypeString;
    private String parameter;

    public TcpRequest(String interfaceName, String methodName, String parameterTypeString, String parameter) {
        this.id = atomicLong.getAndIncrement();
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameterTypeString = parameterTypeString;
        this.parameter = parameter;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getParameterTypeString() {
        return parameterTypeString;
    }

    public void setParameterTypeString(String parameterTypeString) {
        this.parameterTypeString = parameterTypeString;
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
                ", interfaceName='" + interfaceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterTypeString='" + parameterTypeString + '\'' +
                ", parameter='" + parameter + '\'' +
                '}';
    }
}
