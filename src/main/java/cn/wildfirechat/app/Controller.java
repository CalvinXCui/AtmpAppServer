package cn.wildfirechat.app;

import cn.wildfirechat.app.jpa.Users;
import cn.wildfirechat.app.pojo.*;
import cn.wildfirechat.pojos.InputCreateDevice;
import cn.wildfirechat.pojos.UserOnlineStatus;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@Slf4j
public class Controller {
    @Autowired
    private Service mService;

    @GetMapping()
    public Object health() {
        return "Ok";
    }

    /*
    移动端登录
     */
    @PostMapping(value = "/send_code", produces = "application/json;charset=UTF-8")
    public Object sendCode(@RequestBody SendCodeRequest request) {
        log.info("当前进行验证的手机号码为： +"+request.getNationCode()+request.getMobile());
        return mService.sendCode(request.getNationCode(),request.getMobile());
    }

    @PostMapping(value = "/login", produces = "application/json;charset=UTF-8")
    public Object login(@RequestBody LoginRequest request) {
        return mService.login(request);
    }

    /**
     *
     */
    @PostMapping(value = "/passLogin", produces = "application/json;charset=UTF-8")
    public Object passLogin(@RequestBody Users user,Errors errors) {

        return mService.passLogin(user);
    }

    /**
     * 注册用户
     *
     * @param user
     * @return
     */
    @PostMapping(value = "/registered", produces = "application/json;charset=UTF-8")
    public Object registered(@RequestBody @Valid Users user,Errors errors) {
        if(errors.hasErrors()){
            return RestResult.error(RestResult.RestCode.ERROR_DATA_ERRORS);
        }
        return mService.registered(user);

    }

    /**
     * 根据手机号修改用户密码
     *
     * @param user
     * @return
     */
    @PostMapping(value = "/updatePasswordBymobile", produces = "application/json;charset=UTF-8")
    public Object updatePasswordBymobile(@RequestBody Users user,Errors errors) {
        return mService.updatePasswordBymobile(user);
    }
    @PostMapping(value = "/findUserByAccountNumber", produces = "application/json;charset=UTF-8")
    public Object findUserByAccountNumber(@RequestBody FindUsersRequest request){
        log.info("账号搜索用户：参数 = " + (JSONObject)JSONObject.toJSON(request));
        return mService.findByAccountNumber(request.getAccountNumber());
    }

    @PostMapping(value = "/findByMobile", produces = "application/json;charset=UTF-8")
    public Object findByMobile(@RequestBody FindUsersRequest request){
        log.info("手机号搜索用户：参数 = " + (JSONObject)JSONObject.toJSON(request));
        return mService.findByMobile(request.getMobiles());
    }

    @GetMapping(value = "/findUserAll", produces = "application/json;charset=UTF-8")
    public Object findUserAll() {
        return mService.findUserAll();
    }

    /* PC扫码操作
    1, PC -> App     创建会话
    2, PC -> App     轮询调用session_login进行登陆，如果已经扫码确认返回token，否则反正错误码9（已经扫码还没确认)或10(还没有被扫码)。
     */
    @CrossOrigin
    @PostMapping(value = "/pc_session", produces = "application/json;charset=UTF-8")
    public Object createPcSession(@RequestBody CreateSessionRequest request) {
        return mService.createPcSession(request);
    }

    @CrossOrigin
    @PostMapping(value = "/session_login/{token}", produces = "application/json;charset=UTF-8")
    public Object loginWithSession(@PathVariable("token") String token) {
        return mService.loginWithSession(token);
    }

    /* 手机扫码操作
    1，扫码，调用/scan_pc接口。
    2，调用/confirm_pc 接口进行确认
     */
    @PostMapping(value = "/scan_pc/{token}", produces = "application/json;charset=UTF-8")
    public Object scanPc(@PathVariable("token") String token) {
        return mService.scanPc(token);
    }

    @PostMapping(value = "/confirm_pc", produces = "application/json;charset=UTF-8")
    public Object confirmPc(@RequestBody ConfirmSessionRequest request) {
        return mService.confirmPc(request);
    }

    /*
    群公告相关接口
     */
    @CrossOrigin
    @PostMapping(value = "/put_group_announcement", produces = "application/json;charset=UTF-8")
    public Object putGroupAnnouncement(@RequestBody GroupAnnouncementPojo request) {
        return mService.putGroupAnnouncement(request);
    }

    @CrossOrigin
    @PostMapping(value = "/get_group_announcement", produces = "application/json;charset=UTF-8")
    public Object getGroupAnnouncement(@RequestBody GroupIdPojo request) {
        return mService.getGroupAnnouncement(request.groupId);
    }

    /*
    用户在线状态回调
     */
    @PostMapping(value = "/user/online_event")
    public Object onUserOnlineEvent(@RequestBody UserOnlineStatus onlineStatus) {
        System.out.println("User:" + onlineStatus.userId + " on device:" + onlineStatus.clientId + " online status:" + onlineStatus.status);
        return "hello";
    }

    /*
    客户端上传协议栈日志
     */
    @PostMapping(value = "/logs/{userId}/upload")
    public Object uploadFiles(@RequestParam("file") MultipartFile file, @PathVariable("userId") String userId) throws IOException {
        return mService.saveUserLogs(userId, file);
    }

    /*
    物联网相关接口
     */
    @PostMapping(value = "/things/add_device")
    public Object addDevice(@RequestBody InputCreateDevice createDevice) {
        return mService.addDevice(createDevice);
    }

    @PostMapping(value = "/things/list_device")
    public Object getDeviceList() {
        return mService.getDeviceList();
    }

}
