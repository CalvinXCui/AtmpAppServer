package cn.wildfirechat.app.pojo;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendCodeRequest {
    /**
     *
     */
    private String nationCode;
    /**
     *
     */
    private String mobile;

}
