package org.ten.btp.config;

/**
 * Created by ing on 2019-04-28.
 */
public class ApplicationConfig extends  AbstractConfig {

    private static final long serialVersionUID=2L;

    private String name;
    private String id;
    private String version;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
