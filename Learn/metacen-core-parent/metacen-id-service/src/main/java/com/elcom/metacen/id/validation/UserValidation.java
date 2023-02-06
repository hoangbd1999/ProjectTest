package com.elcom.metacen.id.validation;

import com.elcom.metacen.id.constant.Constant;
import com.elcom.metacen.id.exception.ValidationException;
import com.elcom.metacen.id.model.User;
import com.elcom.metacen.id.model.dto.AuthorizationResponseDTO;
import com.elcom.metacen.utils.StringUtil;
import com.elcom.metacen.utils.VNCharacterUtils;
import com.elcom.metacen.id.utils.encrypt.TravisAes;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserValidation.class);

    public String validateInsertUser(User user) {
        if (user == null) {
            return "PayLoad không hợp lệ";
        }
        if (user.getSignupType() == Constant.USER_SIGNUP_NORMAL && StringUtil.isNullOrEmpty(user.getEmail())
                && StringUtil.isNullOrEmpty(user.getMobile())) {
            getMessageDes().add("Email hoặc số điện thoại không được để trống");
        }

        if (!StringUtil.isNullOrEmpty(user.getEmail()) && !StringUtil.validateEmail(user.getEmail())) {
            getMessageDes().add("Email không đúng định dạng");
        }

        if (!StringUtil.isNullOrEmpty(user.getMobile()) && !StringUtil.checkMobilePhoneNumberNew(user.getMobile())) {
            getMessageDes().add("Số điện thoại không đúng định dạng");
        }

        if (StringUtil.isNullOrEmpty(user.getFullName())) {
            getMessageDes().add("Tên đầy đủ không được để trống");
        }
        
        if (StringUtil.isNullOrEmpty(user.getPassword()) || StringUtil.isNullOrEmpty(user.getMatchingPassword())) {
            getMessageDes().add("Mật khẩu không được để trống");
        } else if (!user.getPassword().equals(user.getMatchingPassword())) {
            getMessageDes().add("Mật khẩu và Nhập lại mật khẩu không trùng nhau");
        }
        
        if (!StringUtil.isNullOrEmpty(user.getBirthDay()) && !StringUtil.validateBirthDay(user.getBirthDay(), "dd/MM/yyyy")) {
            getMessageDes().add("Ngày sinh định dạng kiểu dd/MM/yyyy");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateInsertSocial(User user) {
        if (user == null) {
            return "PayLoad không hợp lệ";
        }

        LOGGER.info("ggId: " + user.getGgId() + ", fbId: " + user.getFbId() + ", appleId: " + user.getAppleId());

        if (StringUtil.isNullOrEmpty(user.getGgId()) && StringUtil.isNullOrEmpty(user.getFbId())
                && StringUtil.isNullOrEmpty(user.getAppleId())) {
            getMessageDes().add("Định danh tài khoản không được để trống.");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateLoginApple(String authorizationCode) {

        if (StringUtil.isNullOrEmpty(authorizationCode))
            getMessageDes().add("authorizationCode không được để trống.");

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateUpdateUser(User user, String password, String matchingPassword) {
        if (user == null || user.getUuid() == null) {
            return "PayLoad không hợp lệ";
        }
        
        if (!StringUtil.isNullOrEmpty(user.getEmail()) && !StringUtil.validateEmail(user.getEmail())) {
            getMessageDes().add("Email không đúng định dạng");
        }
        if (!StringUtil.isNullOrEmpty(user.getMobile()) && !StringUtil.checkMobilePhoneNumberNew(user.getMobile())) {
            getMessageDes().add("Số điện thoại không đúng định dạng");
        }
        if (!StringUtil.isNullOrEmpty(user.getFullName()) && user.getFullName().length() > 255) {
            getMessageDes().add("Tên đầy đủ chiều dài tối đa 255");
        }
        if (!StringUtil.isNullOrEmpty(user.getBirthDay()) && !StringUtil.validateBirthDay(user.getBirthDay(), "dd/MM/yyyy")) {
            getMessageDes().add("Ngày sinh định dạng kiểu dd/MM/yyyy");
        }
        if(!StringUtil.isNullOrEmpty(password) && !password.equals(matchingPassword)){
            getMessageDes().add("Mật khẩu và Nhập lại mật khẩu không trùng nhau");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateLogin(String userInfo, String password) throws ValidationException {

        if (StringUtil.isNullOrEmpty(userInfo)) {
            getMessageDes().add("Email/Số điện thoại không được để trống");
        }
        boolean isValidUserInfo = false;
        if (StringUtil.validateEmail(userInfo)) {
            isValidUserInfo = true;
        }
        if (StringUtil.checkMobilePhoneNumberNew(userInfo)) {
            isValidUserInfo = true;
        }
        if (!isValidUserInfo) {
            //getMessageDes().add("Email/Số điện thoại không đúng định dạng");
        }

        if (StringUtil.isNullOrEmpty(password)) {
            getMessageDes().add("Mật khẩu không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateUpdatePassword(AuthorizationResponseDTO dto, String curentPassword,
            String newPassword, String rePassword) throws ValidationException {

        if ((dto.getSignupType() == Constant.USER_SIGNUP_NORMAL || ((dto.getSignupType() == Constant.USER_SIGNUP_FACEBOOK
                || dto.getSignupType() == Constant.USER_SIGNUP_GOOGLE) && dto.getSetPassword() == 1))
                && StringUtil.isNullOrEmpty(curentPassword)) {
            getMessageDes().add("Mật khẩu hiện tại không được bỏ trống");
        }

        if (StringUtil.isNullOrEmpty(newPassword) || StringUtil.isNullOrEmpty(rePassword)) {
            getMessageDes().add("Mật khẩu mới không được để trống");
        } else if (!newPassword.equals(rePassword)) {
            getMessageDes().add("Mật khẩu mới và Nhập lại mật khẩu mới không trùng nhau");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateChangeEmail(AuthorizationResponseDTO dto, String password,
            String newEmail) throws ValidationException {

        if (!StringUtil.validateEmail(newEmail)) {
            getMessageDes().add("Email mới không đúng định dạng");
        }

        if ((dto.getSignupType() == Constant.USER_SIGNUP_NORMAL || ((dto.getSignupType() == Constant.USER_SIGNUP_FACEBOOK
                || dto.getSignupType() == Constant.USER_SIGNUP_GOOGLE) && dto.getSetPassword() == 1))
                && StringUtil.isNullOrEmpty(password)) {
            getMessageDes().add("Mật khẩu không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateChangeMobile(AuthorizationResponseDTO dto, String password,
            String newMobile) throws ValidationException {

        if (!StringUtil.checkMobilePhoneNumberNew(newMobile)) {
            getMessageDes().add("Số điện thoại mới không đúng định dạng");
        }

        if ((dto.getSignupType() == Constant.USER_SIGNUP_NORMAL || ((dto.getSignupType() == Constant.USER_SIGNUP_FACEBOOK
                || dto.getSignupType() == Constant.USER_SIGNUP_GOOGLE) && dto.getSetPassword() == 1))
                && StringUtil.isNullOrEmpty(password)) {
            getMessageDes().add("Mật khẩu không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateUpdateStatus(User user) {
        if (user == null || user.getUuid() == null) {
            return "PayLoad không hợp lệ";
        }
        if (StringUtil.isNullOrEmpty(user.getUuid())) {
            getMessageDes().add("Uuid không được để trống");
        }
        if (user.getStatus() == null || (user.getStatus() != 1 && user.getStatus() != -1)) {
            getMessageDes().add("Status phải kiểu số và chỉ nhận 1 trong 2 giá trị (1,-1)");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateUpdateSocialMobile(AuthorizationResponseDTO dto, String mobile) throws ValidationException {

        if (!StringUtil.checkMobilePhoneNumberNew(mobile)) {
            getMessageDes().add("Số điện thoại không đúng định dạng");
        }
        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateUpdateForgotPassword(String token, String newPassword, String rePassword) throws ValidationException {

        if (StringUtil.isNullOrEmpty(token)) {
            getMessageDes().add("Token không được để trống");
        }

        if (StringUtil.isNullOrEmpty(newPassword) || StringUtil.isNullOrEmpty(rePassword)) {
            getMessageDes().add("Mật khẩu mới không được để trống");
        } else if (!newPassword.equals(rePassword)) {
            getMessageDes().add("Mật khẩu mới và Nhập lại mật khẩu mới không trùng nhau");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateSendEmail(String emailTo, String title, String content, 
            String sign) throws ValidationException {

        if (!StringUtil.validateEmail(emailTo)) {
            getMessageDes().add("EmailTo không đúng định dạng");
        }

        if (StringUtil.isNullOrEmpty(title)) {
            getMessageDes().add("Title không được để trống");
        }
        
        if (StringUtil.isNullOrEmpty(content)) {
            getMessageDes().add("Content không được để trống");
        }
        
        if (StringUtil.isNullOrEmpty(sign)) {
            getMessageDes().add("Sign không được để trống");
        } else {
            try {
                String plainText = emailTo + title + content;
                String coDau2KhongDau = VNCharacterUtils.unAccent(plainText);
                //String decryptText = RSAUtil.decrypt(sign, RSAUtil.privateKey);
                TravisAes travisAes = new TravisAes();
                String decryptText = travisAes.decrypt("Elcom2020@123456", sign);
                System.out.println("plainText: '" + plainText + "'");
                System.out.println("coDau2KhongDau: '" + coDau2KhongDau + "'");
                System.out.println("decryptText: '" + decryptText + "'");
                if(!plainText.equals(decryptText)){
                    getMessageDes().add("Chữ ký (sign) không khớp");
                }
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(UserValidation.class.getName()).log(Level.SEVERE, null, ex);
                getMessageDes().add("Lỗi giải mã sign");
            }
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }
    
    public String validateUpdateForgotPasswordForApp(String otp, String email, String mobile, 
            String newPassword, String rePassword) throws ValidationException {

        if (StringUtil.isNullOrEmpty(otp)) {
            getMessageDes().add("OTP không được để trống");
        }
        
        if (!StringUtil.validateEmail(email) && !StringUtil.checkMobilePhoneNumberNew(mobile)) {
            getMessageDes().add("Email hoặc số điện thoại không đúng định dạng");
        }

        if (StringUtil.isNullOrEmpty(newPassword) || StringUtil.isNullOrEmpty(rePassword)) {
            getMessageDes().add("Mật khẩu mới không được để trống");
        } else if (!newPassword.equals(rePassword)) {
            getMessageDes().add("Mật khẩu mới và Nhập lại mật khẩu mới không trùng nhau");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }
}
