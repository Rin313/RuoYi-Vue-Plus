package org.dromara.common.core.utils.ip;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.StringUtils;

/**
 * 获取地址类
 *
 * @author Lion Li
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressUtils {

    // 未知IP
    public static final String UNKNOWN_IP = "XX XX";
    // 内网地址
    public static final String LOCAL_ADDRESS = "内网IP";
    // 未知地址
    public static final String UNKNOWN_ADDRESS = "未知";

    public static String getClientType() {
        return getClientType(ServletUtils.getRequest());
    }
    public static String getClientType(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");
        if (StringUtils.isEmpty(userAgent)) {
            return "H5";
        }
        String ua = userAgent.toLowerCase();
        String ref = (referer != null) ? referer.toLowerCase() : "";
        if (ua.contains("miniprogram") || ref.contains("servicewechat.com")) {
            return "小程序";
        }
        boolean isMobile = ua.contains("android") 
                        || ua.contains("iphone") 
                        || ua.contains("ipad") 
                        || ua.contains("ipod")
                        || ua.contains("mobile")
                        || ua.contains("harmonyos");
        if (!isMobile) return "PC";
        if (ua.contains("okhttp")           // Android OkHttp
        || ua.contains("cfnetwork")        // iOS 底层网络框架
        || ua.contains("dart"))             // Flutter
            return "APP";
        if (ua.contains("micromessenger")||ua.contains("alipayclient") || ua.contains("alipay")||ua.contains("applewebkit") || ua.contains("mozilla"))
            return "H5";
        return "APP";  // 移动端但无浏览器特征 → 倾向 APP
    }

}

