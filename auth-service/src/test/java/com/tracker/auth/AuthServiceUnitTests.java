package com.tracker.auth;

import com.tracker.auth.controller.AuthControllerTest;
import com.tracker.auth.service.AuthServiceTest;
import com.tracker.auth.service.JwtServiceTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        AuthServiceTest.class,
        JwtServiceTest.class,
        AuthControllerTest.class
})
public class AuthServiceUnitTests {
}