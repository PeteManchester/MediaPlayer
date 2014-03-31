package org.rpi.plugin.lastfm.configmodel;

import org.rpi.utils.SecUtils;
import org.rpi.utils.Utils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.Proxy;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class LastFMConfig {

    private String userName;

    private String password;

    private String passwordEnc;

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

    @XmlElement(name = "Password_ENC")
    public String getPasswordEnc() {
        return passwordEnc;
    }

    public void setPasswordEnc(String passwordEnc) {
        this.passwordEnc = passwordEnc;
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
