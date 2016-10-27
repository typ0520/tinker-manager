package tinker.console.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tinker.console.domain.VersionInfo;
import java.util.List;

/**
 * Created by tong on 16/10/26.
 */
@Mapper
public interface VersionInfoMapper {
    Integer insert(VersionInfo versionInfo);

    List<VersionInfo> findAllByAppUid(String appUid);

    VersionInfo findByUidAndVersionName(@Param("appUid") String appUid,@Param("versionName") String versionName);
}
