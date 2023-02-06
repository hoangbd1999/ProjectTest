package com.elcom.metacen.id.repository;

import com.elcom.metacen.id.constant.Constant;
import com.elcom.metacen.id.model.User;
import com.elcom.metacen.id.model.dto.UserPagingDTO;
import com.elcom.metacen.utils.StringUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author anhdv
 */
@Repository
public class UserCustomizeRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserCustomizeRepository.class);

    @Autowired
    public UserCustomizeRepository(EntityManagerFactory factory) {
        super(factory);
    }

    public User findByUuid(String uuid) {
        Session session = openSession();
        try {
            User user = session.load(User.class, uuid);
            return user;
        } catch (EntityNotFoundException ex) {
            LOGGER.error(ex.toString());
        } finally {
            closeSession(session);
        }
        return null;
    }

    public User findByUsername(String userName) {
        Session session = openSession();
        Object result = null;
        try {
            Query query = session.createNativeQuery("SELECT * FROM " + this.getSchema() + ".user WHERE email = ?", User.class);
            query.setParameter(1, userName);
            result = query.getSingleResult();
        } catch (NoResultException ex) {
            LOGGER.error(ex.toString());
        } finally {
            closeSession(session);
        }
        return result != null ? (User) result : null;
    }

    public boolean updateLastLogin(String uuid, String loginIp) {
        Session session = openSession();
        try {
            Transaction tx = session.beginTransaction();
            String update = " ";
            if (!StringUtil.isNullOrEmpty(loginIp)) {
                update += ", login_ip = :loginIp ";
            }
            String sql = "Update " + this.getSchema() + ".user SET last_login = now() " + update + " WHERE uuid = :uuid ";
            Query query = session.createNativeQuery(sql);
            if (!StringUtil.isNullOrEmpty(loginIp)) {
                query.setParameter("loginIp", loginIp);
            }
            query.setParameter("uuid", uuid);
            int result = query.executeUpdate();
            tx.commit();
            return result > 0;
        } catch (Exception ex) {
            LOGGER.error(ex.toString());
            ex.printStackTrace();
        } finally {
            closeSession(session);
        }
        return false;
    }

    public boolean updateUser(User user) {
        Session session = openSession();
        try {
            Transaction tx = session.beginTransaction();
            String update = " ";
            if (!StringUtil.isNullOrEmpty(user.getMobile())) {
                update += ", mobile = :mobile ";
            }
            if (!StringUtil.isNullOrEmpty(user.getEmail())) {
                update += ", email = :email ";
            }
            if (!StringUtil.isNullOrEmpty(user.getFullName())) {
                update += ", full_name = :fullName ";
            }
            if (!StringUtil.isNullOrEmpty(user.getSkype())) {
                update += ", skype = :skype ";
            }
            if (!StringUtil.isNullOrEmpty(user.getFacebook())) {
                update += ", facebook = :facebook ";
            }
            if (!StringUtil.isNullOrEmpty(user.getAvatar())) {
                update += ", avatar = :avatar ";
            }
            if (!StringUtil.isNullOrEmpty(user.getAddress())) {
                update += ", address = :address ";
            }
            if (!StringUtil.isNullOrEmpty(user.getBirthDay())) {
                update += ", birth_day = :birthDay ";
            }
            if (user.getGender() != null) {
                update += ", gender = :gender ";
            }
            if (user.getProfileUpdate() != null) {
                update += ", profile_update = :profileUpdate ";
            }
            if (user.getAvatarUpdate() != null) {
                update += ", avatar_update = :avatarUpdate ";
            }
            if (user.getStatus() != null) {
                update += ", status = :status ";
            }
            if (user.getPoliceRank() != null) {
                update += ", police_rank = :policeRank ";
            }
            if (user.getPosition() != null) {
                update += ", position = :position ";
            }
            String sql = "Update " + this.getSchema() + ".user SET last_update = now() " + update + " WHERE uuid = :uuid ";
            Query query = session.createNativeQuery(sql);
            if (!StringUtil.isNullOrEmpty(user.getMobile())) {
                query.setParameter("mobile", user.getMobile());
            }
            if (!StringUtil.isNullOrEmpty(user.getEmail())) {
                query.setParameter("email", user.getEmail());
            }
            if (!StringUtil.isNullOrEmpty(user.getFullName())) {
                query.setParameter("fullName", user.getFullName());
            }
            if (!StringUtil.isNullOrEmpty(user.getSkype())) {
                query.setParameter("skype", user.getSkype());
            }
            if (!StringUtil.isNullOrEmpty(user.getFacebook())) {
                query.setParameter("facebook", user.getFacebook());
            }
            if (!StringUtil.isNullOrEmpty(user.getAvatar())) {
                query.setParameter("avatar", user.getAvatar());
            }
            if (!StringUtil.isNullOrEmpty(user.getAddress())) {
                query.setParameter("address", user.getAddress());
            }
            if (!StringUtil.isNullOrEmpty(user.getBirthDay())) {
                query.setParameter("birthDay", user.getBirthDay());
            }
            if (user.getGender() != null) {
                query.setParameter("gender", user.getGender());
            }
            if (user.getProfileUpdate() != null) {
                query.setParameter("profileUpdate", user.getProfileUpdate());
            }
            if (user.getAvatarUpdate() != null) {
                query.setParameter("avatarUpdate", user.getAvatarUpdate());
            }
            if (user.getStatus() != null) {
                query.setParameter("status", user.getStatus());
            }
            if (user.getPoliceRank() != null) {
                query.setParameter("policeRank", user.getPoliceRank());
            }
            if (user.getPosition() != null) {
                query.setParameter("position", user.getPosition());
            }
            query.setParameter("uuid", user.getUuid());
            int result = query.executeUpdate();
            tx.commit();
            return result > 0;
        } catch (Exception ex) {
            LOGGER.error(ex.toString());
            ex.printStackTrace();
        } finally {
            closeSession(session);
        }
        return false;
    }

    public boolean insertTest() {
        Session session = openSession();
        try {
            for (int i = 1; i <= 4; i++) {
                Query query = session.createNativeQuery(" insert into elcom_user(username, password, full_name) "
                        + " values(:userName, :password, :fullName ) ");
                query.setParameter("userName", "anhdv_" + i);
                query.setParameter("password", "anhdv_pw_" + i);
                query.setParameter("fullName", "do viet anh_" + i);
                query.executeUpdate();
            }
        } catch (NoResultException ex) {
            LOGGER.error(ex.toString());
        } finally {
            closeSession(session);
        }
        return true;
    }

    public Boolean countUser(String username, String password) {

        Session session = openSession();
        try {
            StoredProcedureQuery query = session.createStoredProcedureQuery("countUser");

            query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN); //userName
            query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN); //fullName
            query.registerStoredProcedureParameter(3, Integer.class, ParameterMode.OUT); //total count

            query.setParameter(1, username);
            query.setParameter(2, password);

            query.execute();

            Integer total = (Integer) query.getOutputParameterValue(3);
            LOGGER.info("total user: " + total);

        } catch (NoResultException ex) {
            LOGGER.error(ex.toString());
        } finally {
            closeSession(session);
        }
        return true; //enter your condition
    }

    public User findByEmail(String email) {
        Session session = openSession();
        Object result = null;
        try {
            Query query = session.createNativeQuery(" SELECT * FROM " + this.getSchema() + ".user WHERE email = ? AND is_delete = 0", User.class);
            query.setParameter(1, email.trim());
            result = query.getSingleResult();
        } catch (Exception ex) {
            LOGGER.error(ex.toString());
        } finally {
            closeSession(session);
        }
        return result != null ? (User) result : null;
    }

    public User findByMobile(String mobile) {
        Session session = openSession();
        Object result = null;
        try {
            Query query = session.createNativeQuery("SELECT * FROM " + this.getSchema() + ".user WHERE mobile = ? AND is_delete = 0", User.class);
            query.setParameter(1, mobile);
            result = query.getSingleResult();
        } catch (Exception ex) {
            LOGGER.error(ex.toString());
        } finally {
            closeSession(session);
        }
        return result != null ? (User) result : null;
    }

    public User findByUserName(String userName) {
        Session session = openSession();
        Object result = null;
        try {
            Query query = session.createNativeQuery("SELECT * FROM " + this.getSchema() + ".user WHERE user_name = ? AND is_delete = 0", User.class);
            query.setParameter(1, userName);
            result = query.getSingleResult();
        } catch (Exception ex) {
            LOGGER.error(ex.toString());
        } finally {
            closeSession(session);
        }
        return result != null ? (User) result : null;
    }

    public User findByEmailOrMobile(String userInfo) {
        Session session = openSession();
        Object result = null;
        try {
            Query query = session.createNativeQuery("SELECT * FROM " + this.getSchema() + ".user WHERE "
                    + " (email = ? AND is_delete = 0) OR (mobile = ? AND is_delete = 0) ", User.class);
            query.setParameter(1, userInfo);
            query.setParameter(2, userInfo);
            result = query.getSingleResult();
        } catch (Exception ex) {
            LOGGER.error(ex.toString());
        } finally {
            closeSession(session);
        }
        return result != null ? (User) result : null;
    }

    public User findByEmailOrMobileOrUserName(String userInfo) {
        Session session = openSession();
        Object result = null;
        try {
            String sql = "SELECT * FROM " + this.getSchema() + ".user WHERE "
                    + "(email = ? AND is_delete = 0) OR (mobile = ? AND is_delete = 0)"
                    + " OR (user_name = ? AND is_delete = 0) ";
            Query query = session.createNativeQuery(sql, User.class);
            LOGGER.info("sql ==> {}", sql);
            query.setParameter(1, userInfo);
            query.setParameter(2, userInfo);
            query.setParameter(3, userInfo);
            result = query.getSingleResult();
        } catch (Exception ex) {
            LOGGER.error(ex.toString());
        } finally {
            closeSession(session);
        }
        return result != null ? (User) result : null;
    }

    public User findBySocial(Integer signupType, String socialId) {
        Session session = openSession();
        try {
            String sql = "FROM " + this.getSchema() + ".User u WHERE u.signupType = :signupType AND u.isDelete = 0 ";
            switch (signupType) {
                case Constant.USER_SIGNUP_FACEBOOK:
                    sql += " AND u.fbId = :socialId ";
                    break;
                case Constant.USER_SIGNUP_GOOGLE:
                    sql += " AND u.ggId = :socialId ";
                    break;
                case Constant.USER_SIGNUP_APPLE:
                    sql += " AND u.appleId = :socialId ";
                    break;
                default:
                    break;
            }

            Query query = session.createQuery(sql, User.class)
                    .setParameter("signupType", signupType)
                    .setParameter("socialId", socialId.trim());
            Object object = query.getSingleResult();
            return object != null ? (User) object : null;
        } catch (Exception ex) {
            LOGGER.error(ex.toString());
        } finally {
            closeSession(session);
        }
        return null;
    }

    public boolean connectSocial(User user, Integer socialType, String socialId) {
        Session session = openSession();
        try {
            Transaction tx = session.beginTransaction();
            String update = "";
            switch (socialType) {
                case Constant.USER_SIGNUP_FACEBOOK:
                    update = " u.fbId = :socialId ";
                    break;
                case Constant.USER_SIGNUP_GOOGLE:
                    update = " u.ggId = :socialId ";
                    break;
                case Constant.USER_SIGNUP_APPLE:
                    update = " u.appleId = :socialId ";
                    break;
                default:
                    break;
            }
            String sql = "Update " + this.getSchema() + ".User u SET " + update + " WHERE u.uuid = :uuid ";
            Query query = session.createQuery(sql);
            query.setParameter("socialId", socialId.trim());
            query.setParameter("uuid", user.getUuid());

            int result = query.executeUpdate();
            tx.commit();
            return result > 0;
        } catch (Exception ex) {
            LOGGER.error(ex.toString());
        } finally {
            closeSession(session);
        }
        return false;
    }

    public UserPagingDTO findAll(String keyword, Integer status, Integer currentPage,
            Integer rowsPerPage, String sort, Integer signupType, Integer mobileVerify,
            List<String> adminUuidList, String startDate, String endDate, Pageable pageable) {
        Session session = openSession();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<User> criteria = builder.createQuery(User.class);
            Root<User> usersRoot = criteria.from(User.class);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(usersRoot.get("isDelete"), 0));
            if (!StringUtil.isNullOrEmpty(keyword)) {
                keyword = "%" + keyword.toUpperCase().trim() + "%";
                predicates.add(builder.or(builder.like(builder.upper(usersRoot.get("fullName")), keyword),
                        builder.like(builder.upper(usersRoot.get("email")), keyword),
                        builder.like(builder.upper(usersRoot.get("mobile")), keyword),
                        builder.like(builder.upper(usersRoot.get("userName")), keyword)
                ));
            }
            if (status != null) {
                predicates.add(builder.equal(usersRoot.get("status"), status));
            }
            if (signupType != null) {
                predicates.add(builder.equal(usersRoot.get("signupType"), signupType));
            }
            if (mobileVerify != null) {
                predicates.add(builder.equal(usersRoot.get("mobileVerify"), mobileVerify));
            }
            if (adminUuidList != null && !adminUuidList.isEmpty()) {
                predicates.add(builder.not(usersRoot.get("uuid").in(adminUuidList)));
            }
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                //Date crated
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                startDate += " 00:00:00";
                endDate += " 23:59:59";
                Predicate createdPre = builder.between(usersRoot.get("createdAt"), df.parse(startDate), df.parse(endDate));
                predicates.add(createdPre);
            }
            if (!predicates.isEmpty()) {
                criteria.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));
            }
            // Nếu ko truyền sortBy thì mặc định sort theo createdAt DESC
            if (StringUtil.isNullOrEmpty(sort)) {
                criteria.orderBy(builder.desc(usersRoot.get("createdAt")));
            } else {
                criteria.orderBy(builder.desc(usersRoot.get(sort)));
            }

            List<User> result = session.createQuery(criteria).setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize()).getResultList();
            // Create Count Query
            CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
            Root<User> usersRootCount = countQuery.from(User.class);
            countQuery.select(builder.count(usersRootCount));
            if (!predicates.isEmpty()) {
                countQuery.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));
            }
            Long count = session.createQuery(countQuery).getSingleResult();

            //UserPaging DTO
            UserPagingDTO userPagingDTO = new UserPagingDTO();
            userPagingDTO.setDataRows(result);
            userPagingDTO.setTotalRows(count);
            return userPagingDTO;
        } catch (Exception ex) {
            LOGGER.error("findAll().ex: " + ex.getCause().toString());
            ex.printStackTrace();
        } finally {
            closeSession(session);
        }
        return null;
    }
}
