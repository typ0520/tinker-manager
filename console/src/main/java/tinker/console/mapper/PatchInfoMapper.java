package tinker.console.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tinker.console.domain.PatchInfo;
import java.util.List;

/**
 * Created by tong on 16/10/26.
 */
@Mapper
public interface PatchInfoMapper {
    Integer insert(PatchInfo patchInfo);

    List<PatchInfo> findByUidAndVersionName(@Param("appUid") String appUid, @Param("versionName") String versionName);

    PatchInfo findById(Integer id);

    PatchInfo findByIdAndAppUid(@Param("id") Integer id,@Param("appUid") String appUid);
}
