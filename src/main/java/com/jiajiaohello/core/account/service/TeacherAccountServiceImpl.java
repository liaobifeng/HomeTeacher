package com.jiajiaohello.core.account.service;

import com.jiajiaohello.core.account.model.RecommendType;
import com.jiajiaohello.core.account.model.TeacherAccount;
import com.jiajiaohello.core.account.model.TeacherInfo;
import com.jiajiaohello.core.info.model.Course;
import com.jiajiaohello.core.teacher.dto.TeacherEditForm;
import com.jiajiaohello.core.teacher.dto.SearchForm;
import com.jiajiaohello.core.teacher.dto.VerifyForm;
import com.jiajiaohello.support.auth.AuthHelper;
import com.jiajiaohello.support.auth.PasswordEncoder;
import com.jiajiaohello.support.auth.RegisterForm;
import com.jiajiaohello.support.auth.TeacherUserDetailService;
import com.jiajiaohello.support.core.*;
import com.jiajiaohello.support.web.Pager;

import com.jiajiaohello.support.exception.UserLogicException;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;

import javax.transaction.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Teacher: bohan
 * Date: 9/17/14
 * Time: 4:07 PM
 */
@Service
public class TeacherAccountServiceImpl implements TeacherAccountService {
    @Autowired
    private CommonDao<TeacherAccount> teacherAccountCommonDao;
    @Autowired
    private CommonDao<TeacherInfo> teacherInfoCommonDao;
    @Autowired
    private OSSService ossService;
    @Autowired
    private Jedis jedis;

    @Override
    public TeacherAccount get(String username) {
        return teacherAccountCommonDao.get(new TeacherAccount(username));
    }

    @Override
    @Transactional
    public TeacherAccount loginLoad(String username) {
        TeacherAccount account = new TeacherAccount(username);
        account = teacherAccountCommonDao.get(account);
        if (account == null) {
            return null;
        }
        account.setUpdatedAt(new Date());
        teacherAccountCommonDao.saveOrUpdate(account);
        return account;
    }

    @Override
    public TeacherAccount get(Integer teacherId) {
        return teacherAccountCommonDao.get(teacherId, TeacherAccount.class);
    }

    @Override
    public void update(TeacherEditForm teacherEditForm) throws IOException {
    	TeacherAccount account = packageTeacherAccount(teacherEditForm);
        AuthHelper.reloadAccount(account, TeacherUserDetailService.authorities);
        teacherAccountCommonDao.saveOrUpdate(account);
    }

    
    
    @Override
    public void create(RegisterForm registerForm) throws UserLogicException {
        TeacherAccount teacherAccount = get(registerForm.getPhone());
        if(teacherAccount != null) {
            throw new UserLogicException("该账号已存在！");
        }
        teacherAccount = new TeacherAccount();
        try {
            BeanUtils.copyProperties(teacherAccount, registerForm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        teacherAccount.setAvatar(CommonHelper.DEFAULT_AVATAR_URL);
        teacherAccount.setUsername(registerForm.getPhone());
        teacherAccount.setPassword(new PasswordEncoder().encode(registerForm.getPassword()));   // 加密后保存
        TeacherInfo info = new TeacherInfo();
        info.init();
        teacherAccount.setInfo(info);
        teacherAccountCommonDao.saveOrUpdate(teacherAccount);
    }

    @Override
    public void updateCourses(Integer[] courseIds) {
        TeacherAccount teacherAccount = teacherAccountCommonDao.get(new TeacherAccount(AuthHelper.getUsername()));
        List<Course> courses = new ArrayList<>();
        for (Integer courseId : courseIds) {
            Course course = new Course();
            course.setId(courseId);
            courses.add(course);
        }
        teacherAccount.getInfo().setCourses(courses);

        teacherAccountCommonDao.saveOrUpdate(teacherAccount);
    }

    @Override
    public List<TeacherAccount> getRecommendTeacherAccounts(RecommendType recommendType, Integer start, Integer size) {
        int end = start + size - 1;
        List<String> teacherIdStrings = jedis.lrange(RedisKeys.recommendTeachers.getKey(recommendType), start, end);
        List<TeacherAccount> list = new ArrayList<>();
        for (String teacherIdString : teacherIdStrings) {
            TeacherAccount teacherAccount = teacherAccountCommonDao.get(Integer.parseInt(teacherIdString), TeacherAccount.class);
            if(teacherAccount != null) {  // 如果取到数据，则加入
                list.add(teacherAccount);
            }
        }

        return list;
    }

	@Override
	public List<TeacherAccount> getTeacherAccounts(TeacherAccount entity,Integer firstResult, Integer maxResult) {
		
		List<TeacherAccount> teacherAccountList =teacherAccountCommonDao.getList(entity, firstResult, maxResult);
		
		return teacherAccountList;
	}

	@Override
	public int getCount() {
		
		return teacherAccountCommonDao.getCount(TeacherAccount.class);
	}

	@Override
	public void verityTeacher(VerifyForm verifyform) {
		TeacherAccount account = get(verifyform.getUsername());
		
		account.getInfo().setAudited(verifyform.isAudited());
        teacherInfoCommonDao.saveOrUpdate(account.getInfo());
	}

	@Override
	public void updateTeacher(TeacherEditForm teacherEditForm) throws IOException {
		TeacherAccount account = packageTeacherAccount(teacherEditForm);
        teacherAccountCommonDao.saveOrUpdate(account);
	}
    public TeacherAccount packageTeacherAccount(TeacherEditForm teacherEditForm)throws IOException {
    	TeacherAccount account = get(teacherEditForm.getUsername());

        account.setName(teacherEditForm.getName());
        account.getInfo().setCompleted(Boolean.TRUE);
        account.getInfo().setDescription(teacherEditForm.getDescription());
        account.getInfo().setSchool(teacherEditForm.getSchool());
        account.getInfo().setFreeTime(teacherEditForm.getFreeTime());
        account.getInfo().setIdentity(teacherEditForm.getIdentity());
        account.getInfo().setSex(teacherEditForm.getSex());

        String avatar = ossService.upload(teacherEditForm.getAvatarFile(), OSSBucket.avatar, Integer.toString(account.getId()));
        if(StringUtils.isNotBlank(avatar)) {
            account.setAvatar(avatar);
        }
        String identityUrl = ossService.upload(teacherEditForm.getIdentityFile(), OSSBucket.avatar, "identity_" + account.getId());
        if(StringUtils.isNotBlank(identityUrl)) {
            account.getInfo().setIdentityUrl(identityUrl);
        }
        String educationUrl = ossService.upload(teacherEditForm.getEducationFile(), OSSBucket.avatar, "education_" + account.getId());
        if(StringUtils.isNotBlank(educationUrl)) {
            account.getInfo().setEducationUrl(educationUrl);
        }
        return account;
    }

	@Override
	public List<TeacherAccount> getTeacherAccountsByCondition(
			SearchForm searchform, Pager page) {
		if(page.getTotal()==-1){
			int count=getCount();
			page.setTotal(count);
		}
		TeacherAccount account=new TeacherAccount();
		account.setUsername(searchform.getUsername());
		TeacherInfo info =new TeacherInfo();
		info.setDescription(searchform.getDescription());
		info.setFreeTime(searchform.getFreeTime());
		info.setIdentity(searchform.getIdentity());
		account.setName(searchform.getName());
		info.setSex( searchform.getSex());
		account.setUsername(searchform.getUsername());
		account.setInfo(info);
		List<TeacherAccount> teacherAccountList=getTeacherAccounts(account,page.getOffset(), page.getMaxResult());
		return teacherAccountList;
	}
    
}
