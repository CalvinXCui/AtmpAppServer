package cn.wildfirechat.app.sms;


import cn.wildfirechat.app.RestResult;

public interface SmsService {
    RestResult.RestCode sendCode(String nationCode,String mobile, String code);
}
