package cn.wildfirechat.app.jpa;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Slf4j
@Entity
@Table(name = "t_user")
public class Users {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private String id;
    /**
     *
     */
    @Column(name = "_uid")
    private String uid;
    /**
     *
     */
    @Column(name = "_name")
    private String name;
    /**
     * 昵称
     */
    @Column(name = "_display_name")
    @NotBlank(message = "displayName不能为空")
    private String displayName;
    /**
     * 账号
     */
    @Column(name = "_account_number")
    private String accountNumber;
    /**
     * 密码
     */
    @Column(name = "_passwd_md5")
    @NotBlank(message = "password不能为空")
    private String password;
    /**
     * 头像
     */
    @Column(name = "_portrait")
    private String portrait;
    /**
     * 性别
     */
    @Column(name = "_gender")
    private String gender;

    @Column(name = "_nation_code")
    @NotBlank(message = "手机地区号不能为空")
    private String nationCode;
    /**
     * 手机号
     */
    @Column(name = "_mobile")
    @NotBlank(message = "手机号不能为空")
    private String mobile;
    /**
     * 邮箱
     */
    @Column(name = "_email")
    private String email;
    /**
     * 地址
     */
    @Column(name = "_address")
    private String address;
    /**
     * 公司
     */
    @Column(name = "_company")
    private String commpany;

    @Column(name="_client_id")
    private String clientId;
    /**
     * 平台
     */
    @Column(name = "_platform")
    private Integer platform;
    /**
     *
     */
    @Column(name = "_salt")
    private String salt;
    /**
     *
     */
    @Column(name = "_extra")
    private String extra;
    /**
     *
     */
    @Column(name = "_type")
    private String type;
    /**
     *
     */
    @Column(name = "_dt")
    private String dt;
    /**
     * Register
     */
    @Column(name="_register")
    private String register;
    /**
     *
     */
    @Column(name = "_create_time")
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private String createTime;
    /**
     *
     */
    @Column(name = "_deleted")
    private String deleted;

    @Transient
    private String code;

}
