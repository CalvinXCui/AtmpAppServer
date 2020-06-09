package cn.wildfirechat.app.pojo;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class LoginRequest {
    /**
     *
     */
    private String nationCode;
    /**
     *
     */
    private String mobile;
    /**
     *
     */
    private String password;
    /**
     *
     */
    private String code;
    /**
     *
     */
    private String clientId;
    /**
     *
     */
    private Integer platform;

}
