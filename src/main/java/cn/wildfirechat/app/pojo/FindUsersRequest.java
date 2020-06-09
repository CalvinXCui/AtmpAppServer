package cn.wildfirechat.app.pojo;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class FindUsersRequest {
    /**
     *
     */
    private String accountNumber;
    /**
     *
     */
    private String nationCode;
    /**
     *
     */
    private String mobiles;

}
