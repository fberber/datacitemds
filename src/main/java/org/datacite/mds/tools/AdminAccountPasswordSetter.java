package org.datacite.mds.tools;

import org.datacite.mds.domain.Allocator;
import org.datacite.mds.util.DomainUtils;
import org.datacite.mds.validation.ValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.Console;

@Component
public class AdminAccountPasswordSetter extends AdminAccountTool {

    @Override
    public void run(String[] args) {
        Allocator admin = DomainUtils.getAdmin();
        if (admin == null)
            throw new RuntimeException("admin account doesn't exists");
        System.out.println(admin);
        String password = readEncodedPassword();
        admin.setPassword(password);
        validationHelper.validate(admin);
        admin.merge();
    }

    public static void main(String[] args) {
        initAndRun(args);
    }

}
