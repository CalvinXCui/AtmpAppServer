package cn.wildfirechat.app.jpa;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

@Data
@Slf4j
@Entity
@Table(name = "t_user_session")
public class UserSession {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private String id;

    @Column(name = "_uid")
    private String _uid;

    @Column(name = "_cid")
    private String _cid;

    @Column(name = "_token")
    private String _token;

    @Column(name = "_voip_token")
    private String _voip_token;

    @Column(name = "_secret")
    private String _secret;

    @Column(name = "_db_secret")
    private String _db_secret;

    @Column(name = "_platform")
    private String _platform;

    @Column(name = "_push_type")
    private String _push_type;

    @Column(name = "_package_name")
    private String _package_name;

    @Column(name = "_device_name")
    private String _device_name;

    @Column(name = "_device_version")
    private String _device_version;

    @Column(name = "_phone_name")
    private String _phone_name;

    @Column(name = "_language")
    private String _language;

    @Column(name = "_carrier_name")
    private String _carrier_name;

    @Column(name = "_dt")
    private String _dt;

    @Column(name = "_deleted")
    private String _deleted;

    @Column(name = "_user_type")
    private String _user_type;
}
