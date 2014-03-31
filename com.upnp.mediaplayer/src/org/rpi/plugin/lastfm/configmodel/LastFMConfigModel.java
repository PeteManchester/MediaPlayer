package org.rpi.plugin.lastfm.configmodel;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "LastFM")
@XmlAccessorType(XmlAccessType.FIELD)
public class LastFMConfigModel {

    @XmlElement(name = "Config")
    private LastFMConfig config;

    @XmlElementWrapper(name = "BlackList")
    @XmlElement(name = "BlackListItem")
    private List<org.rpi.plugin.lastfm.configmodel.BlackList> blackList;

    public LastFMConfig getConfig() {
        return config;
    }

    public void setConfig(LastFMConfig config) {
        this.config = config;
    }

    public List<org.rpi.plugin.lastfm.configmodel.BlackList> getBlackList() {
        return blackList;
    }

    public void setBlackList(List<org.rpi.plugin.lastfm.configmodel.BlackList> blackList) {
        this.blackList = blackList;
    }
}
