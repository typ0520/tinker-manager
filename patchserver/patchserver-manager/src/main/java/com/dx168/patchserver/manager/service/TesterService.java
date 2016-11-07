package com.dx168.patchserver.manager.service;

import com.dx168.patchserver.core.domain.Tester;
import com.dx168.patchserver.core.mapper.TesterMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

/**
 * Created by tong on 16/11/1.
 */
@Service
public class TesterService {
    @Autowired
    private TesterMapper testerMapper;

    public void save(Tester tester) {
        tester.setCreatedAt(new Date());
        tester.setUpdatedAt(new Date());
        int id = testerMapper.insert(tester);
        tester.setId(id);
    }

    public List<Tester> findAllByAppUid(String appUid) {
        return testerMapper.findAllByAppUid(appUid);
    }

    public String getAllTags(String appUid) {
        List<Tester> testerList =  testerMapper.findAllByAppUid(appUid);
        StringBuilder sb = new StringBuilder();

        if (testerList != null) {
            for (int i = 0; i < testerList.size(); i++) {
                String tag = testerList.get(i).getTag();
                if (tag == null) {
                    continue;
                }
                if (i != 0) {
                    sb.append(";");
                }
                sb.append(tag);
            }
        }
        return sb.toString();
    }

    public Tester findByTagAndUid(String tag,String appUid) {
        return testerMapper.findByTagAndUid(tag,appUid);
    }

    public void deleteById(Integer testerId) {
        testerMapper.deleteById(testerId);
    }

    public Tester findById(Integer testerId) {
        return testerMapper.findById(testerId);
    }
}
