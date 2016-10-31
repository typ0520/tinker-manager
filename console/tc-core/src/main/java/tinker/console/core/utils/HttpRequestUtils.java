package tinker.console.core.utils;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class HttpRequestUtils {
	protected static PathMatcher pathMatcher = new AntPathMatcher();

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

	public static String getBasePath(HttpServletRequest request) {
		StringBuffer url = request.getRequestURL();
		String basePath = url
				.delete(url.length() - request.getRequestURI().length(),
						url.length()).append("/").toString();
		return basePath;
	}
}
