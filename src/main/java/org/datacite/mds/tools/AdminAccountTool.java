package org.datacite.mds.tools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.datacite.mds.validation.ValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import java.io.Console;

@Component
public abstract class AdminAccountTool extends AbstractTool {
    protected Console console = System.console();

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected ValidationHelper validationHelper;

    protected String readEncodedPassword() {
        char[] passwordChar = console.readPassword("Password:");
        if (passwordChar.length == 0)
            throw new RuntimeException("password cannot be empty");
        String password = new String(passwordChar);
        String encodedPassword = passwordEncoder.encodePassword(password, null);
        return encodedPassword;
    }
}
