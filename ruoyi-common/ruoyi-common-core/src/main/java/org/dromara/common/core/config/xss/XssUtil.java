package org.dromara.common.core.config.xss;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

//不应该使用性能较差的Filter全局过滤
//不应该使用需要手动调用的注解
//不应该手动维护白名单
//支持可能会使用的富文本
public final class XssUtil {
    
    // ✅ 使用 Jsoup 预设的 relaxed 白名单，无需手动维护
    private static final Safelist SAFELIST = Safelist.relaxed()
        .preserveRelativeLinks(true);  // 保留相对链接
    
    private XssUtil() {}
    
    public static String clean(String content) {
        // 空值快速返回
        if (content == null || content.isEmpty()) {
            return content;
        }
        
        // ⚡ 快速路径：不含 HTML 特征则直接返回（性能优化关键）
        if (!mayContainHtml(content)) {
            return content;
        }
        
        return Jsoup.clean(content, SAFELIST);
    }
    
    // 快速检测，避免无谓的 Jsoup 解析
    private static boolean mayContainHtml(String content) {
        return content.indexOf('<') >= 0 || 
               content.indexOf('&') >= 0 ||
               content.indexOf('\u0000') >= 0;  // 空字符攻击
    }
}