package com.atguigu.gmall.gateway.properties;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@Component
@ConfigurationProperties(prefix = "gmall.gateway.jwt")
@Data
public class JwtProperties {
    private String cookieName;
    private PublicKey publicKey;
    private String pubKeyPath;

    @PostConstruct
    public void init() {
        try {
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
