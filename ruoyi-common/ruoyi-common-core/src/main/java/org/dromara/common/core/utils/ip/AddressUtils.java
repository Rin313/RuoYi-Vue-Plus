package org.dromara.common.core.utils.ip;

import cn.hutool.http.HtmlUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.NetUtils;
import org.dromara.common.core.utils.StringUtils;

/**
 * 获取地址类
 *
 * @author Lion Li
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressUtils {

    // 未知地址
    public static final String UNKNOWN = "XX XX";

    public static String getRealAddressByIP(String ip) {
        // 处理空串并过滤HTML标签
        ip = HtmlUtil.cleanHtmlTag(StringUtils.blankToDefault(ip,""));
        // 判断是否为IPv4或IPv6，如果不是则返回未知地址
        if (!NetUtils.isIPv4(ip) && !NetUtils.isIPv6(ip)) {
            return UNKNOWN;
        }
        // 内网不查询
        if (NetUtils.isInnerIPv6(ip) || NetUtils.isInnerIP(ip)) {
            return "内网IP";
        }
        return RegionUtils.getCityInfo(ip);
    }

}
