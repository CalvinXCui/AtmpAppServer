package cn.wildfirechat.app;


import cn.wildfirechat.app.jpa.Announcement;
import cn.wildfirechat.app.jpa.AnnouncementRepository;
import cn.wildfirechat.app.jpa.Users;
import cn.wildfirechat.app.jpa.UsersRepository;
import cn.wildfirechat.app.model.PCSession;
import cn.wildfirechat.app.pojo.*;
import cn.wildfirechat.app.shiro.AuthDataSource;
import cn.wildfirechat.app.shiro.TokenAuthenticationToken;
import cn.wildfirechat.app.sms.SmsService;
import cn.wildfirechat.app.tools.UUIDUserNameGenerator;
import cn.wildfirechat.app.tools.Utils;
import cn.wildfirechat.app.util.BaseUtils;
import cn.wildfirechat.common.ErrorCode;
import cn.wildfirechat.pojos.*;
import cn.wildfirechat.proto.ProtoConstants;
import cn.wildfirechat.sdk.ChatConfig;
import cn.wildfirechat.sdk.GroupAdmin;
import cn.wildfirechat.sdk.MessageAdmin;
import cn.wildfirechat.sdk.UserAdmin;
import cn.wildfirechat.sdk.model.IMResult;
import com.alibaba.fastjson.JSONObject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.json.JSONException;

import com.alibaba.fastjson.JSONArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static cn.wildfirechat.app.RestResult.RestCode.*;

@org.springframework.stereotype.Service
public class ServiceImpl implements Service {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceImpl.class);

    @Autowired
    private SmsService smsService;

    @Autowired
    private IMConfig mIMConfig;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private UsersRepository userRepository;

    @Value("${sms.super_code}")
    private String superCode;

    @Value("${logs.user_logs_path}")
    private String userLogPath;

    @Autowired
    private UUIDUserNameGenerator userNameGenerator;

    @Autowired
    private AuthDataSource authDataSource;

    @PostConstruct
    private void init() {
        ChatConfig.initAdmin(mIMConfig.admin_url, mIMConfig.admin_secret);
    }


    /**
     * 根据手机号注册信息
     *
     * @param user
     * @return
     */
    @Override
    public RestResult registered(Users user) {
        LOG.info("当前注册时：用户输入信息：" + "地区号= " + user.getNationCode() + "  手机号= " + user.getMobile());
        String code;
        LOG.info("注册时当前用户输入验证码为： " + user.getCode());
        String newNationCode = BaseUtils.handNationCode(user.getNationCode());
        if (StringUtils.equalsIgnoreCase(user.getCode(), superCode)) {
            code = superCode;
        } else {
            code = AuthDataSource.mRecords.get(newNationCode + user.getMobile()).getCode();
        }

//        LOG.info("注册时取得超级验证码 ： " + superCode);
        if (!StringUtils.equalsIgnoreCase(code, user.getCode())) {
            return RestResult.error(ERROR_CODE_INCORRECT);
        }
        List<Users> users = userRepository.findByMobileIs(user.getMobile());
        if (users != null && !users.isEmpty()) {
            return RestResult.error(RestResult.RestCode.ERROR_DATA_EXISTS);
        }/**/
        try {
            user.setId(UUID.randomUUID().toString());
//            user.setName(userNameGenerator.getUserName(user.getMobile()));
            user.setUid(getRandomUid(8));
            user.setAccountNumber(String.valueOf(getAccountNumberInt((int) ((Math.random() * 9 + 1) * 1000000))));
            user.setName(user.getAccountNumber());
            user.setDt("1");
            user.setDeleted("0");
            user.setNationCode(newNationCode);
            user.setMobile(user.getMobile());
            user.setRegister("0");
            user.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            Users save = userRepository.save(user);
            LOG.info("注册生成的用户为： " + save + "  注册生成的用户时间为： " + user.getCreateTime());
            RestResult restResult = RestResult.ok(SUCCESS);
            restResult.setResult(user);
            return restResult;
        } catch (Exception e) {
            return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
        }
    }

    /**
     * 根据手机号修改密码
     *
     * @param mobile
     * @param password
     * @return
     */
    @Override
    public RestResult updatePasswordBymobile(Users user) {
        String newNationCode = BaseUtils.handNationCode(user.getNationCode());
        String code;
        LOG.info("修改密码时当前用户输入验证码为： " + user.getCode());
        if (StringUtils.equalsIgnoreCase(user.getCode(), superCode)) {
            code = superCode;
        } else {
            if (AuthDataSource.mRecords.containsKey(newNationCode + user.getMobile())) {
                code = AuthDataSource.mRecords.get(newNationCode + user.getMobile()).getCode();
            } else {
                return RestResult.error(ERROR_CODE_INCORRECT);
            }
        }

//        LOG.info("修改密码时取得超级验证码 ： " + superCode);
        if (!StringUtils.equalsIgnoreCase(code, user.getCode())) {
            return RestResult.error(ERROR_CODE_INCORRECT);
        }
        List<Users> users = userRepository.findByMobileIs(user.getMobile());
        if (users == null || users.isEmpty()) {
            RestResult errorResult = RestResult.error(ERROR_DATA_NOT_EXISTS);
            errorResult.setResult("用户不存在");
            return errorResult;
        }
        userRepository.updatePasswordBymobile(user.getMobile(), user.getPassword().trim());
        RestResult result = RestResult.error(SUCCESS);
        result.setMessage("修改成功");
        result.setResult(users);
        return result;
    }

    /**
     * @return
     */
    @Override
    public RestResult findUserAll() {
        List<Users> users = userRepository.findAll();
        RestResult result = RestResult.ok(SUCCESS);
        result.setResult(users);
        return result;
    }

    @Override
    public RestResult sendCode(String nationCode, String mobile) {
        String newNationCode = BaseUtils.handNationCode(nationCode);
        try {
            String code = Utils.getRandomCode(4);
            LOG.info("当前手机号 " + mobile + " 生成的验证码为： " + code);
            RestResult.RestCode restCode = authDataSource.insertRecord(newNationCode, mobile, code);

            if (restCode != SUCCESS) {
                return RestResult.error(restCode);
            }


            restCode = smsService.sendCode(newNationCode, mobile, code);
            if (restCode == RestResult.RestCode.SUCCESS) {
                return RestResult.ok(restCode);
            } else {
                authDataSource.clearRecode(newNationCode, mobile);
                return RestResult.error(restCode);
            }
        } catch (JSONException e) {
            // json解析错误
            e.printStackTrace();
            authDataSource.clearRecode(newNationCode, mobile);
        }
        return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
    }

    /**
     * 使用用户名和密码登录
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public RestResult passLogin(Users users) {
        LOG.info("Login passLogin");
        /**
         * 查询用户是否存在
         */
        List<Users> userList = userRepository.findByAccountNumberAndPassword(users.getAccountNumber().trim(), users.getPassword().trim());

        if (userList.size() > 0) {
            Users user = userList.get(0);
            Subject subject = SecurityUtils.getSubject();
            // 在认证提交前准备 token（令牌）
            UsernamePasswordToken token = new UsernamePasswordToken(user.getMobile(), superCode);
            // 执行认证登陆
            try {
                subject.login(token);
            } catch (UnknownAccountException uae) {
                return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
            } catch (IncorrectCredentialsException ice) {
                return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
            } catch (LockedAccountException lae) {
                return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
            } catch (ExcessiveAttemptsException eae) {
                return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
            } catch (AuthenticationException ae) {
                return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
            }
            if (subject.isAuthenticated()) {
                long timeout = subject.getSession().getTimeout();
                LOG.info("Login success " + timeout);
            } else {
                token.clear();
                return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
            }

            //使用用户id获取token
            IMResult<OutputGetIMTokenData> tokenResult = null;
            try {
                tokenResult = UserAdmin.getUserToken(user.getUid(), user.getClientId(), user.getPlatform() == null ? 0 : user.getPlatform());
                LOG.info("账号登录clientId=" + user.getClientId());

                if (tokenResult.getErrorCode() != ErrorCode.ERROR_CODE_SUCCESS) {
                    LOG.error("Get user failure {}", tokenResult.code);
                    return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
                }

                subject.getSession().setAttribute("userId", user.getUid());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //返回用户id，token和是否新建
            LoginResponse response = new LoginResponse();
            response.setUserId(user.getUid());
            response.setJsonObject((JSONObject) JSONObject.toJSON(user));
            response.setToken(tokenResult.getResult().getToken());
            response.setRegister(Integer.valueOf(user.getRegister()) == 0 ? true : false);
            LOG.info("账号密码登陆回传 = " + (JSONObject) JSONObject.toJSON(response));
            if (response.isRegister()) {
                if (!StringUtils.isEmpty(mIMConfig.welcome_for_new_user)) {
                    sendTextMessage(user.getUid(), mIMConfig.welcome_for_new_user);
                    userRepository.updateRegisterById("1", user.getId());
                }
            } else {
                if (!StringUtils.isEmpty(mIMConfig.welcome_for_back_user)) {
                    sendTextMessage(user.getUid(), mIMConfig.welcome_for_back_user);
                }
            }

            return RestResult.ok(response);
        } else {
            return RestResult.error(ERROR_USERNAME_AND_PASSWORD_NOT_EXISTS);
        }
    }

    /**
     * 手机验证码登录
     *
     * @param nationCode 地区号
     * @param mobile     手机号
     * @param code       验证码
     * @param clientId
     * @param platform
     * @return
     */
    @Override
    public RestResult login(LoginRequest request) {
        LOG.info("Login login");
        String newNationCode = BaseUtils.handNationCode(request.getNationCode());
        String s_code;
        LOG.info("手机验证码登录时，当前用户输入验证码为： " + request.getCode());
        if (StringUtils.equalsIgnoreCase(request.getCode(), superCode)) {
            s_code = superCode;
        } else {
            if (AuthDataSource.mRecords.containsKey(newNationCode + request.getMobile())) {
                s_code = AuthDataSource.mRecords.get(newNationCode + request.getMobile()).getCode();
            } else {
                return RestResult.error(ERROR_DATA_ERRORS);
            }
        }

        if (!StringUtils.equalsIgnoreCase(s_code, request.getCode())) {
            return RestResult.error(ERROR_CODE_INCORRECT);
        }

        Subject subject = SecurityUtils.getSubject();
        // 在认证提交前准备 token（令牌）

        UsernamePasswordToken token = new UsernamePasswordToken(request.getMobile(), superCode);
        // 执行认证登陆
        try {
            subject.login(token);
        } catch (UnknownAccountException uae) {
            return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
        } catch (IncorrectCredentialsException ice) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
        } catch (LockedAccountException lae) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
        } catch (ExcessiveAttemptsException eae) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
        } catch (AuthenticationException ae) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
        }
        if (subject.isAuthenticated()) {
            long timeout = subject.getSession().getTimeout();
            LOG.info("Login success " + timeout);
        } else {
            token.clear();
            return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
        }


        try {
            //使用电话号码查询用户信息。<这个查询返回的数据不完整>
//            IMResult<InputOutputUserInfo> userResult = UserAdmin.getUserByMobile(mobile);
            /**
             * 根据手机号查询用户
             */
            List<Users> byMobileIs = userRepository.findByMobileIs(request.getMobile());

            //如果用户信息不存在，创建用户
            InputOutputUserInfo user_;
            Users user;
            if (byMobileIs.size() <= 0) {
                LOG.info("User not exist, try to create");
                try {
                    user = new Users();
                    user.setId(UUID.randomUUID().toString());
                    user.setUid(getRandomUid(8));
                    if (mIMConfig.use_random_name) {
                        String displayName = "用户" + (int) (Math.random() * 10000);
                        user.setDisplayName(displayName);
                    } else {
                        user.setDisplayName(request.getMobile());
                    }
                    user.setNationCode(newNationCode);
                    user.setMobile(request.getMobile());
                    user.setPassword(request.getPassword());
                    user.setAccountNumber(String.valueOf(getAccountNumberInt((int) ((Math.random() * 9 + 1) * 1000000))));
                    user.setName(user.getAccountNumber());
                    user.setDt("1");
                    user.setDeleted("0");
                    user.setRegister("0");
                    user.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    user.setPlatform(request.getPlatform());
                    user.setClientId(request.getClientId());
                    user.setCode(request.getCode());
                    Users save = userRepository.save(user);
                    LOG.info("注册生成的用户为： " + save + "  注册生成的用户时间为： " + user.getCreateTime());
                } catch (Exception e) {
                    LOG.info("当前手机号未在系统注册，登陆时自动创建用户失败");
                    return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
                }
            } else {
                user = byMobileIs.get(0);
            }

            //使用用户id获取token
            IMResult<OutputGetIMTokenData> tokenResult = tokenResult = UserAdmin.getUserToken(user.getUid(), request.getClientId(), request.getPlatform() == null ? 0 : request.getPlatform());
            LOG.info("手机登录clientId=" + request.getClientId());
            if (tokenResult.getErrorCode() != ErrorCode.ERROR_CODE_SUCCESS) {
                LOG.error("Get user failure {}", tokenResult.code);
                return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
            }

            subject.getSession().setAttribute("userId", user.getUid());

            //返回用户id，token和是否新建
            LoginResponse response = new LoginResponse();
            response.setUserId(user.getUid());
            response.setJsonObject((JSONObject) JSONObject.toJSON(user));
            response.setToken(tokenResult.getResult().getToken());
            response.setRegister(StringUtils.equalsIgnoreCase(user.getRegister(), "0") ? true : false);
            LOG.info("手机验证码登录，返回信息 ： " + (JSONObject) JSONObject.toJSON(response));
            if (response.isRegister()) {
                if (!StringUtils.isEmpty(mIMConfig.welcome_for_new_user)) {
                    sendTextMessage(user.getUid(), mIMConfig.welcome_for_new_user);
                    userRepository.updateRegisterById("1", user.getId());
                }
            } else {
                if (!StringUtils.isEmpty(mIMConfig.welcome_for_back_user)) {
                    sendTextMessage(user.getUid(), mIMConfig.welcome_for_back_user);
                }
            }

            return RestResult.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Exception happens {}", e);
            return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
        }
    }


    private void sendTextMessage(String toUser, String text) {
        Conversation conversation = new Conversation();
        conversation.setTarget(toUser);
        conversation.setType(ProtoConstants.ConversationType.ConversationType_Private);
        MessagePayload payload = new MessagePayload();
        payload.setType(1);
        payload.setSearchableContent(text);


        try {
            IMResult<SendMessageResult> resultSendMessage = MessageAdmin.sendMessage("admin", conversation, payload);
            if (resultSendMessage != null && resultSendMessage.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                LOG.info("发送消息成功");
            } else {
                LOG.error("发送消息错误 {}", resultSendMessage != null ? resultSendMessage.getErrorCode().code : "unknown");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("发送消息错误 {}", e.getLocalizedMessage());
        }

    }


    @Override
    public RestResult createPcSession(CreateSessionRequest request) {
        PCSession session = authDataSource.createSession(request.getClientId(), request.getToken(), request.getPlatform());
        SessionOutput output = session.toOutput();
        return RestResult.ok(output);
    }

    @Override
    public RestResult loginWithSession(String token) {
        Subject subject = SecurityUtils.getSubject();
        // 在认证提交前准备 token（令牌）
        // comment start 如果确定登录不成功，就不通过Shiro尝试登录了
        TokenAuthenticationToken tt = new TokenAuthenticationToken(token);
        RestResult.RestCode restCode = authDataSource.checkPcSession(token);
        if (restCode != SUCCESS) {
            return RestResult.error(restCode);
        }
        // comment end

        // 执行认证登陆
        // comment start 由于PC端登录之后，可以请求app server创建群公告等。为了保证安全, PC端登录时，也需要在app server创建session。
        try {
            subject.login(tt);
        } catch (UnknownAccountException uae) {
            return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
        } catch (IncorrectCredentialsException ice) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
        } catch (LockedAccountException lae) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
        } catch (ExcessiveAttemptsException eae) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
        } catch (AuthenticationException ae) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
        }
        if (subject.isAuthenticated()) {
            LOG.info("Login success");
        } else {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_INCORRECT);
        }
        // comment end

        PCSession session = authDataSource.getSession(token, true);
        if (session == null) {
            subject.logout();
            return RestResult.error(RestResult.RestCode.ERROR_CODE_EXPIRED);
        }
        subject.getSession().setAttribute("userId", session.getConfirmedUserId());

        try {
            //使用用户id获取token
            IMResult<OutputGetIMTokenData> tokenResult = UserAdmin.getUserToken(session.getConfirmedUserId(), session.getClientId(), session.getPlatform());
            if (tokenResult.getCode() != 0) {
                LOG.error("Get user failure {}", tokenResult.code);
                subject.logout();
                return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
            }
            //返回用户id，token和是否新建
            LoginResponse response = new LoginResponse();
            response.setUserId(session.getConfirmedUserId());
            response.setToken(tokenResult.getResult().getToken());
            return RestResult.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            subject.logout();
            return RestResult.error(RestResult.RestCode.ERROR_SERVER_ERROR);
        }
    }

    @Override
    public RestResult scanPc(String token) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");
        return authDataSource.scanPc(userId, token);
    }

    @Override
    public RestResult confirmPc(ConfirmSessionRequest request) {
        return authDataSource.confirmPc(request.getUser_id(), request.getToken());
    }

    @Override
    public RestResult getGroupAnnouncement(String groupId) {
        Optional<Announcement> announcement = announcementRepository.findById(groupId);
        if (announcement.isPresent()) {
            GroupAnnouncementPojo pojo = new GroupAnnouncementPojo();
            pojo.groupId = announcement.get().getGroupId();
            pojo.author = announcement.get().getAuthor();
            pojo.text = announcement.get().getAnnouncement();
            pojo.timestamp = announcement.get().getTimestamp();
            return RestResult.ok(pojo);
        } else {
            return RestResult.error(ERROR_GROUP_ANNOUNCEMENT_NOT_EXIST);
        }
    }

    @Override
    public RestResult putGroupAnnouncement(GroupAnnouncementPojo request) {
        if (!StringUtils.isEmpty(request.text)) {
            Subject subject = SecurityUtils.getSubject();
            String userId = (String) subject.getSession().getAttribute("userId");
            boolean isGroupMember = false;
            try {
                IMResult<OutputGroupMemberList> imResult = GroupAdmin.getGroupMembers(request.groupId);
                if (imResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS && imResult.getResult() != null && imResult.getResult().getMembers() != null) {
                    for (PojoGroupMember member : imResult.getResult().getMembers()) {
                        if (member.getMember_id().equals(userId)) {
                            if (member.getType() != ProtoConstants.GroupMemberType.GroupMemberType_Removed
                                    && member.getType() != ProtoConstants.GroupMemberType.GroupMemberType_Silent) {
                                isGroupMember = true;
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!isGroupMember) {
                return RestResult.error(ERROR_NO_RIGHT);
            }

            Conversation conversation = new Conversation();
            conversation.setTarget(request.groupId);
            conversation.setType(ProtoConstants.ConversationType.ConversationType_Group);
            MessagePayload payload = new MessagePayload();
            payload.setType(1);
            payload.setSearchableContent("@所有人 " + request.text);
            payload.setMentionedType(2);


            try {
                IMResult<SendMessageResult> resultSendMessage = MessageAdmin.sendMessage(request.author, conversation, payload);
                if (resultSendMessage != null && resultSendMessage.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                    LOG.info("send message success");
                } else {
                    LOG.error("send message error {}", resultSendMessage != null ? resultSendMessage.getErrorCode().code : "unknown");
                    return RestResult.error(ERROR_SERVER_ERROR);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("send message error {}", e.getLocalizedMessage());
                return RestResult.error(ERROR_SERVER_ERROR);
            }
        }

        Announcement announcement = new Announcement();
        announcement.setGroupId(request.groupId);
        announcement.setAuthor(request.author);
        announcement.setAnnouncement(request.text);
        request.timestamp = System.currentTimeMillis();
        announcement.setTimestamp(request.timestamp);

        announcementRepository.save(announcement);
        return RestResult.ok(request);
    }

    @Override
    public RestResult saveUserLogs(String userId, MultipartFile file) {
        File localFile = new File(userLogPath, userId + "_" + file.getOriginalFilename());

        try {
            file.transferTo(localFile);
        } catch (IOException e) {
            e.printStackTrace();
            return RestResult.error(ERROR_SERVER_ERROR);
        }

        return RestResult.ok(null);
    }

    @Override
    public RestResult addDevice(InputCreateDevice createDevice) {
        try {
            Subject subject = SecurityUtils.getSubject();
            String userId = (String) subject.getSession().getAttribute("userId");

            if (!StringUtils.isEmpty(createDevice.getDeviceId())) {
                IMResult<OutputDevice> outputDeviceIMResult = UserAdmin.getDevice(createDevice.getDeviceId());
                if (outputDeviceIMResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                    if (!createDevice.getOwners().contains(userId)) {
                        return RestResult.error(ERROR_NO_RIGHT);
                    }
                } else if (outputDeviceIMResult.getErrorCode() != ErrorCode.ERROR_CODE_NOT_EXIST) {
                    return RestResult.error(ERROR_SERVER_ERROR);
                }
            }

            IMResult<OutputCreateDevice> result = UserAdmin.createOrUpdateDevice(createDevice);
            if (result != null && result.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                return RestResult.ok(result.getResult());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.error(ERROR_SERVER_ERROR);
    }

    @Override
    public RestResult getDeviceList() {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");
        try {
            IMResult<OutputDeviceList> imResult = UserAdmin.getUserDevices(userId);
            if (imResult != null && imResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                return RestResult.ok(imResult.getResult().getDevices());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.error(ERROR_SERVER_ERROR);
    }

    /**
     * 随机生成length位字符串
     *
     * @param length
     * @return
     */
    public String getRandomUid(int length) {
        String uId = BaseUtils.getRandomStr(length);
        List<Users> uidList = userRepository.findByUid(uId);
        if (uidList.size() > 0) {
            return getRandomUid(length);
        } else {
            return uId;
        }
    }

    /**
     * 随机生成7位数字，并且在数据库中不存在
     *
     * @return
     */
    public int getAccountNumberInt(int accountNumberInt) {
        List<Users> accountNumberList = userRepository.findByAccountNumber(String.valueOf(accountNumberInt));
        if (accountNumberList.size() > 0) {
            return getAccountNumberInt((int) ((Math.random() * 9 + 1) * 1000000));
        } else {
            return accountNumberInt;
        }
    }

    /**
     * 账号查询
     *
     * @param accountNumber 账号
     * @return
     */
    public RestResult findByAccountNumber(String accountNumber) {
        List<Users> usersList = userRepository.findByAccountNumber(accountNumber);
        if (usersList.size() <= 0) {
            return RestResult.error(ERROR_DATA_NOT_EXISTS);
        } else {
            FindUsersResponse response = new FindUsersResponse();
            response.setUser((JSONObject) JSONObject.toJSON(usersList.get(0)));
            LOG.info("根据账号搜索回传信息 ： " + (JSONObject) JSONObject.toJSON(response));
            return RestResult.ok(response);
        }
    }

    /**
     * 地区号+手机号查询
     *
     * @param nationCode 地区号
     * @param mobile     手机号
     * @return
     */
    public RestResult findByMobile(String mobile) {
        LOG.info("service层实现的手机号搜索：" + mobile);
        List<Users> usersList = userRepository.findByMobileIs(mobile);
        if (usersList.size() <= 0) {
            return RestResult.error(ERROR_DATA_NOT_EXISTS);
        } else {
            FindUsersResponse response = new FindUsersResponse();
            response.setUser((JSONObject) JSONObject.toJSON(usersList.get(0)));
            LOG.info("根据手机号搜索用户： " + (JSONObject) JSONObject.toJSON(response));
            return RestResult.ok(response);
        }
    }

}
