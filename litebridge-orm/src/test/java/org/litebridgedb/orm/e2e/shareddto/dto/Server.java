package org.litebridgedb.orm.e2e.shareddto.dto;

public class Server {

    private String host;
    private Status status;

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }
}
