package com.match_intel.backend.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("env.mail")
public class EmailProperties {

    private String smtpHost = "localhost";
    private int smtpPort = 25;
    private String username = "";
    private String password = "";


    public String getSmtpHost() {
        return smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
