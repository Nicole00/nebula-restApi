package com.vesoft.nebula.graph.server.entity;

import java.io.Serializable;

public class NebulaConnectRequest implements Serializable {

    /**
     * graph 服务的地址
     */
    private String host;

    /**
     * NebulaGraph 的用户名，未开启鉴权则默认 root
     */
    private String username;

    /**
     * NebulaGraph 的用户密码，未开启鉴权则默认 nebula
     */
    private String password;

    private String space;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    @Override
    public String toString() {
        return "NebulaConnectRequest{" +
                "host='" + host + '\'' +
                ", username='" + username + '\'' +
                ", space='" + space + '\'' +
                '}';
    }
}
