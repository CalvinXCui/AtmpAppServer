package cn.wildfirechat.app.pojo;

import cn.wildfirechat.app.jpa.Users;
import cn.wildfirechat.pojos.InputOutputUserInfo;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;


@Data
public class LoginResponse {
    /**
     *
     */
    private String userId;
    /**
     *
     */
    private String token;
    /**
     *
     */
    private JSONObject jsonObject;
    /**
     *
     */
    private boolean register;
}
