/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.test.util.connection;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.enm.data.ENMUser;
import com.ericsson.nms.launcher.LauncherOperator;
import com.ericsson.nms.security.OpenIDMOperatorImpl;
import com.ericsson.oss.test.util.common.constants.CommonConstants;

public class UserUtility implements CommonConstants {

    private static Logger LOGGER = LoggerFactory.getLogger(UserUtility.class);

    public ENMUser createUserAndAssignRole(final OpenIDMOperatorImpl openIDMOperator) {
        final ENMUser enmUser = createEnmUser();
        LOGGER.info("Logging in with default username and password... ");
        LOGGER.debug("Username [{}], Password [{}]", DEFAULT_ENM_USER, DEFAULT_ENM_PASS);
        openIDMOperator.connect(DEFAULT_ENM_USER, DEFAULT_ENM_PASS);
        LOGGER.info("Creating user ... [{}]", enmUser.getUsername());
        openIDMOperator.createUser(enmUser);
        LOGGER.info("Assigning administrator rights to ... [{}]", enmUser.getUsername());
        openIDMOperator.assignUsersToRole(ENM_ADMIN_USER, enmUser.getUsername());
        return enmUser;
    }

    public HttpTool logInSecurely(final LauncherOperator launcherOperator, final ENMUser enmUser) {
        LOGGER.info("Logging in as... [{}]", enmUser.getUsername());
        return launcherOperator.login(new User(enmUser.getUsername(), enmUser.getPassword(), UserType.ADMIN));
    }

    public ENMUser createEnmUser() {
        final ENMUser enmUser = new RivendellUser(USERNAME, FIRSTNAME, LASTNAME, EMAIL, true, PASSWORD);
        return enmUser;
    }

    private static class RivendellUser implements ENMUser {

        private String username;
        private String firstname;
        private String lastname;
        private String email;
        private boolean enabled;
        private String password;

        public RivendellUser(final String username, final String firstname, final String lastname, final String email, final boolean enabled,
                             final String password) {
            this.username = username;
            this.firstname = firstname;
            this.lastname = lastname;
            this.email = email;
            this.enabled = enabled;
            this.password = password;
        }

        @Override
        public <T> T getFieldValue(final String name) {
            return null;
        }

        @Override
        public Map<String, Object> getAllFields() {
            final Map<String, Object> allFields = new HashMap<String, Object>();
            allFields.put("username", username);
            allFields.put("firsname", firstname);
            allFields.put("lastname", lastname);
            allFields.put("email", email);
            allFields.put("enabled", enabled);
            allFields.put("password", password);
            return allFields;
        }

        @Override
        public String getDataSourceName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getFirstName() {
            return firstname;
        }

        @Override
        public String getLastName() {
            return lastname;
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public Boolean getEnabled() {
            return enabled;
        }

    }

}
