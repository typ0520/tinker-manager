package tinker.console.mapper;

import tinker.console.domain.BasicUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by tong on 16/10/26.
 */
@Mapper
public interface UserMapper {
    BasicUser findByUsername(String username);

    Integer insert(BasicUser basicUser);
}
