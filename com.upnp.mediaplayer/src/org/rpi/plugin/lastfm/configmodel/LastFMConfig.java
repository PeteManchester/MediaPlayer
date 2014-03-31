package org.rpi.plugin.lastfm.configmodel;

import org.rpi.utils.SecUtils;
import org.rpi.utils.Utils;

import javax.xml.bind.annotation.*;
import java.net.Proxy;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder={"userName","password", "proxyType", "proxyIP", "proxyPort"})
public class LastFMConfig {

    private String userName;

    private String password;

    // default value for the proxyType is DIRECT
    private Proxy.Type proxyType = Proxy.Type.DIRECT;

    private String proxyIP;

    private int proxyPort = -1;

    @XmlElement(name = "UserName")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @XmlElement(name = "Password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlElement(name = "ProxyType")
    public Proxy.Type getProxyType() {
        return proxyType;
    }

    public void setProxyType(Proxy.Type proxyType) {
        this.proxyType = proxyType;
    }

    @XmlElement(name = "ProxyIP")
    public String getProxyIP() {
        return proxyIP;
    }

    public void setProxyIP(String proxyIP) {
        this.proxyIP = proxyIP;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    @XmlElement(name = "ProxyPort")
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }
}
