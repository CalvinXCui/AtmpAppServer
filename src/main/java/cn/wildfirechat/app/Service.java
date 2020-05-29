package cn.wildfirechat.app;


import cn.wildfirechat.app.jpa.Users;
import cn.wildfirechat.app.pojo.ConfirmSessionRequest;
import cn.wildfirechat.app.pojo.CreateSessionRequest;
import cn.wildfirechat.app.pojo.GroupAnnouncementPojo;
import cn.wildfirechat.pojos.InputCreateDevice;
import org.springframework.web.multipart.MultipartFile;

public interface Service {

    /**
     *
     * @param user
     * @return
     */
    RestResult registered(Users user);

    /**
     *
     * @param mobile
     * @param password
     * @return
     */
    RestResult updatePasswordBymobile(Users user);

    /**
     *
     * @return
     */
    RestResult findUserAll();


    /**
     *
     * @param user
     * @return
     */
    RestResult passLogin(Users user);

    RestResult sendCode(String mobile);
    RestResult login(String mobile, String code, String clientId, int platform);

    RestResult createPcSession(CreateSessionRequest request);
    RestResult loginWithSession(String token);

    RestResult scanPc(String token);
    RestResult confirmPc(ConfirmSessionRequest request);

    RestResult putGroupAnnouncement(GroupAnnouncementPojo request);
    RestResult getGroupAnnouncement(String groupId);

    RestResult saveUserLogs(String userId, MultipartFile file);

    RestResult addDevice(InputCreateDevice createDevice);
    RestResult getDeviceList();


}
