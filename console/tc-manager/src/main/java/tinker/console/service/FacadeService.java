package tinker.console.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by tong on 16/10/31.
 */
@Service
public class FacadeService {
    private static final Logger LOG = LoggerFactory.getLogger(FacadeService.class);

    @Value("${tc-facade-url}")
    private String facadrUrl;

    public void clearCache() {
        String url = facadrUrl + "/api/clearCache";
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            byte[] buffer = org.apache.commons.io.IOUtils.toByteArray(conn.getInputStream());
            LOG.info("clearCache res: " + new String(buffer));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
