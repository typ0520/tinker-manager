package com.dx168.patchserver.manager.service;

import com.dx168.patchserver.core.domain.*;
import com.dx168.patchserver.core.mapper.PatchInfoMapper;
import com.dx168.patchserver.core.mapper.PatchLogMapper;
import com.dx168.patchserver.core.utils.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by guoyongping on 17/10/12.
 */
@Service
public class PatchLogService {
    private static final Logger LOG = LoggerFactory.getLogger(PatchLogService.class);

    @Autowired
    private PatchLogMapper patchLogMapper;


    public List<PatchLog> findList(Map param) {
        List<PatchLog> patchInfoList = patchLogMapper.findList(param);
        return patchInfoList;
    }

}
