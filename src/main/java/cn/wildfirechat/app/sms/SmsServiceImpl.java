package cn.wildfirechat.app.sms;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.shiro.AuthDataSource;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SmsServiceImpl implements SmsService {
    private static final Logger LOG = LoggerFactory.getLogger(SmsServiceImpl.class);


    private static class AliyunCommonResponse {
        String Message;
        String Code;
    }

    @Value("${sms.verdor}")
    private int smsVerdor;

    @Autowired
    private TencentSMSConfig mTencentSMSConfig;

    @Autowired
    private AliyunSMSConfig aliyunSMSConfig;

    @Override
    public RestResult.RestCode sendCode(String nationCode,String mobile, String code) {
        if (smsVerdor == 1) {
            return sendTencentCode(nationCode,mobile, code);
        } else if(smsVerdor == 2) {
            return sendAliyunCode(nationCode,mobile, code);
        }else if(smsVerdor == 3) {
            return sendMeiLianCode(nationCode,mobile, code);
        } else {
            return RestResult.RestCode.ERROR_SERVER_NOT_IMPLEMENT;
        }
    }

    private RestResult.RestCode sendTencentCode(String nationCode,String mobile, String code) {
        try {
//            String[] params = {code};
            String[] params = new String[2];
            params[0] = code;
            params[1] = "1";
            LOG.info("发送腾讯短信验证需要参数：appID =" +mTencentSMSConfig.appid+"," + "appKey =" +mTencentSMSConfig.appkey);
            SmsSingleSender ssender = new SmsSingleSender(mTencentSMSConfig.appid, mTencentSMSConfig.appkey);
            SmsSingleSenderResult result = ssender.sendWithParam(nationCode, mobile,
                    mTencentSMSConfig.templateId, params, null, "", "");
            if (result.result == 0) {
                return RestResult.RestCode.SUCCESS;
            } else {
                LOG.error("Failure to send SMS {}", result);
                return RestResult.RestCode.ERROR_SERVER_ERROR;
            }
        } catch (HTTPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return RestResult.RestCode.ERROR_SERVER_ERROR;
    }

    /**
     * <暂未使用>
     * @param nationCode 地区编号
     * @param mobile 手机号
     * @param code 验证码
     * @return 发送结果
     */
    private RestResult.RestCode sendAliyunCode(String nationCode,String mobile, String code) {
        LOG.info("阿里云当前配文件配置："+aliyunSMSConfig.getAccessKeyId() + aliyunSMSConfig.getAccessSecret());
        DefaultProfile profile = DefaultProfile.getProfile("default", aliyunSMSConfig.getAccessKeyId(), aliyunSMSConfig.getAccessSecret());
        IAcsClient client = new DefaultAcsClient(profile);

        String templateparam = "{\"code\":\"" + code + "\"}";
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("PhoneNumbers", nationCode+mobile);
        request.putQueryParameter("SignName", aliyunSMSConfig.getSignName());
        request.putQueryParameter("TemplateCode", aliyunSMSConfig.getTemplateCode());
        request.putQueryParameter("TemplateParam", templateparam);
        try {
            CommonResponse response = client.getCommonResponse(request);
            LOG.info(response.getData());
            if (response.getData() != null) {
                AliyunCommonResponse aliyunCommonResponse = new Gson().fromJson(response.getData(), AliyunCommonResponse.class);
                if (aliyunCommonResponse != null) {
                    if (aliyunCommonResponse.Code.equalsIgnoreCase("OK")) {
                        return RestResult.RestCode.SUCCESS;
                    } else {
                        LOG.info("Send aliyun sms failure with message:" + aliyunCommonResponse.Message);
                    }
                }
            }
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }


        return RestResult.RestCode.ERROR_SERVER_ERROR;
    }

   public static RestResult.RestCode sendMeiLianCode(String nationCode, String mobile, String code){

        //连接超时及读取超时设置
        System.setProperty("sun.net.client.defaultConnectTimeout", "30000"); //连接超时：30秒
        System.setProperty("sun.net.client.defaultReadTimeout", "60000");	//读取超时：30秒

        //新建一个StringBuffer链接
        StringBuffer buffer = new StringBuffer();


        //String encode = "GBK"; //页面编码和短信内容编码为GBK。重要说明：如提交短信后收到乱码，请将GBK改为UTF-8测试。如本程序页面为编码格式为：ASCII/GB2312/GBK则该处为GBK。如本页面编码为UTF-8或需要支持繁体，阿拉伯文等Unicode，请将此处写为：UTF-8

        String encode = "UTF-8";

        String username = "13074614669";  //用户名

        String password_md5 = "1ADBB3178591FD5BB0C248518F39BF6D";  //密码

//        String mobile = "";  //手机号,只发一个号码：13800000001。发多个号码：13800000001,13800000002,...N 。使用半角逗号分隔。

        String apikey = "722a90bec91355290e6c0e99593713ec";  //apikey秘钥（请登录 http://m.5c.com.cn 短信平台-->账号管理-->我的信息 中复制apikey）

        String content = "您好，您的验证码是："+code+"【ATME】";  //要发送的短信内容，特别注意：签名必须设置，网页验证码应用需要加添加【图形识别码】。

       String newMobile = mobile.replaceFirst("^0*", "");

        try {


            String contentUrlEncode = URLEncoder.encode(content,encode);  //对短信内容做Urlencode编码操作。注意：如

            //把发送链接存入buffer中，如连接超时，可能是您服务器不支持域名解析，请将下面连接中的：【m.5c.com.cn】修改为IP：【115.28.23.78】
            buffer.append("http://115.28.23.78/api/send/index.php?username="+username+"&password_md5="+password_md5+"&mobile="+nationCode+newMobile+"&apikey="+apikey+"&content="+contentUrlEncode+"&encode="+encode);

            //System.out.println(buffer); //调试功能，输入完整的请求URL地址

            //把buffer链接存入新建的URL中
            URL url = new URL(buffer.toString());

            //打开URL链接
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            //使用POST方式发送
            connection.setRequestMethod("POST");

            //使用长链接方式
            connection.setRequestProperty("Connection", "Keep-Alive");

            //发送短信内容
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            //获取返回值
            String result = reader.readLine();

            //输出result内容，查看返回值，成功为success，错误为error，详见该文档起始注释
            LOG.info("发送结果： "+result);
            if(!result.isEmpty()&& result.contains("success")){
                return RestResult.RestCode.SUCCESS;
            }else {
                return RestResult.RestCode.ERROR_SERVER_ERROR;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return RestResult.RestCode.ERROR_SERVER_ERROR;
        }

    }

    public static void main(String[] args) {
        String str1 = "abcd:123456";
        boolean def = str1.contains("abcd");
        System.out.println(def);
//        int result1 = str1.indexOf("cde");
//        if(result1 != -1){
//            System.out.println("字符串str中包含子串“a”"+result1);
//        }else{
//            System.out.println("字符串str中不包含子串“a”"+result1);
//        }
    }

}
