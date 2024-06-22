package homework.arch.users.validation;

import homework.arch.users.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

import static org.springframework.util.StringUtils.hasText;

@Component("beforeCreateUserValidator")
public class UserValidator implements Validator {
    private static Pattern PHONE_PATTERN = Pattern.compile("([+]?\\d{5,})");
    private static Pattern OWASP_EMAIL_PATTERN
                                = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        User user = (User) obj;
        if (!hasText(user.getFirstName())) {
            errors.rejectValue("firstName", "blank");
        }
        if (!hasText(user.getLastName())) {
            errors.rejectValue("lastName", "blank");
        }
        if (!hasText(user.getEmail())) {
            errors.rejectValue("email", "blank");
        } else if (!isValidEmail(user.getEmail())) {
            errors.rejectValue("email", "invalid");
        }
        if (!hasText(user.getPhone())) {
            errors.rejectValue("phone", "blank");
        } else if (!isValidPhone(user.getPhone())) {
            errors.rejectValue("phone", "invalid");
        }
    }

    private boolean isValidPhone(String phone) {
        return PHONE_PATTERN.matcher(phone).matches();
    }

    private boolean isValidEmail(String email) {
        return OWASP_EMAIL_PATTERN.matcher(email).matches();
    }

}
