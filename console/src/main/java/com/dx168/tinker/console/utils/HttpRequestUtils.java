package com.dx168.tinker.console.utils;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HttpRequestUtils {
	protected static PathMatcher pathMatcher = new AntPathMatcher();

	public static String getDomainFromUrl(String urlStr) {
		try {
			URL url = new URL(urlStr);
			return url.getHost();
		} catch (MalformedURLException e) {
			return null;
		}		
	}
	
	public static String getIpAddr(HttpServletRequest request) {
		String ip = null;
		Enumeration enu = request.getHeaderNames();
		while (enu.hasMoreElements()) {
			String name = (String) enu.nextElement();
			if (name.equalsIgnoreCase("X-Forwarded-For")) {
				ip = request.getHeader(name);
			} else if (name.equalsIgnoreCase("Proxy-Client-IP")) {
				ip = request.getHeader(name);
			} else if (name.equalsIgnoreCase("WL-Proxy-Client-IP")) {
				ip = request.getHeader(name);
			}
			if ((ip != null) && (ip.length() != 0))
				break;
		}

		if ((ip == null) || (ip.length() == 0))
			ip = request.getRemoteAddr();
		if("0:0:0:0:0:0:0:1".equals(ip)){
			ip = "127.0.0.1" ;
		}
		return ip;
	}
	
	public static String getUserAgent(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent");
		return agent;
	}
	
	public static String getReferUrl(HttpServletRequest request) {
		String agent = request.getHeader("Referer");
		return agent;
	}
	
	  /**
     * 将request参数转成map对象
     * @param request
     * @return
     */
    public static Map<String, Object> getRequestMap(HttpServletRequest request) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        @SuppressWarnings("unchecked")
		Map<String, String[]> requestMap = request.getParameterMap();
        Iterator<String> iter = requestMap.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            String[] values = requestMap.get(key);
            if (values.length > 1) {
            } else {
                if (values[0] != null) {
                    values[0].trim().replace("\r", "").replace("\n", "");
                    paramMap.put(key, values[0].trim());
                }
            }
        }
        return paramMap;
    }

	public static Cookie getCookie(HttpServletRequest request,String name) {
		if (request.getCookies() == null) {
			return null;
		}
		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals(name)) {
				return cookie;
			}
		}
		return null;
	}

	public static boolean isJsonRequest(HttpServletRequest request) {
		String[] keys = new String[]{"content-type","Content-Type","content-Type","Content-yype"};

		String contentType = null;
		for (int i = 0;i < keys.length;i++) {
			contentType = request.getHeader(keys[i]);
			if (contentType != null) {
				break;
			}
		}

		return contentType != null &&
				contentType.toLowerCase().contains("application/json");
	}

	public static String urlEncode(String str) {
		if (str == null) {
			str = "";
		}
		try {
			return URLEncoder.encode(str, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String urlDecode(String encodeStr) {
		if (encodeStr == null) {
			encodeStr = "";
		}
		try {
			return URLDecoder.decode(encodeStr, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static boolean isAjax(HttpServletRequest request) {
		boolean isAjax = false;
		String ajaxRequest = request.getHeader("x-Requested-With");
		if (ajaxRequest != null && ajaxRequest.equals("XMLHttpRequest")) {
			isAjax = true;
		}
		return isAjax;
	}

	public static boolean isInclude(String uri, String ...includes) {
		boolean isInclude = false;
		if (includes != null) {
			for (String resource : includes) {
				if (pathMatcher.match(resource, uri)) {
					isInclude = true;
					break;
				}
			}
		}
		return isInclude;
	}
}
