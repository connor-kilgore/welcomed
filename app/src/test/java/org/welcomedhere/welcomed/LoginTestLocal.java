package org.welcomedhere.welcomed;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.welcomedhere.welcomed.data.LoginDataSource;
import org.welcomedhere.welcomed.data.LoginRepository;
import org.welcomedhere.welcomed.data.Result;
import org.welcomedhere.welcomed.data.model.LoggedInUser;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LoginTestLocal {
    private String validEmail = "test@test.com";
    private String invalidEmail = "notAnEmail";
    private String validPassword = "Password!!";
    private String invalidPassword = "p";
    private String simulateServerFailure = "ServFail";

    @Mock
    LoginDataSource data;

    @Mock
    LoginRepository inst;

    @Before
    public void initMocks() {
        // Mock DataSource behavior.
        // We won't be actually contacting Firebase here, so emulate expected behavior.
        data = mock(LoginDataSource.class);

        when(data.login(validEmail,validPassword))
                .thenReturn(new Result.Success<>(null));

        when(data.login(validEmail,invalidPassword))
                .thenReturn(new Result.Error(
                        new Exception("6, Password")));

        when(data.login(invalidEmail,validPassword))
                .thenReturn(new Result.Error(
                        new Exception("6, Email")));

        // Usually won't bother checking pass if email is incorrect anyways
        when(data.login(invalidEmail,invalidPassword))
                .thenReturn(new Result.Error(
                        new Exception("6, Email")));

        // Generic exception, we can't predict exact failure result/cause.
        when(data.login(simulateServerFailure, simulateServerFailure))
                .thenReturn(new Result.Error(new Exception()));

        // Skip what we'd normally do for logout, usually clears Firebase credentials.
        doNothing().when(data).logout();

        inst = LoginRepository.getInstance(data);
    }

    @Test
    public void LoginTest_CorrectCredentials_NoFailure() {
        Assert.assertEquals(Result.Success.class,
                inst.login(validEmail, validPassword).getClass());
        inst.logout();
    }

    @Test
    public void LoginTest_CorrectCredentials_Failure() {
        Result<LoggedInUser> res = inst.login(simulateServerFailure, simulateServerFailure);
        Assert.assertEquals(Result.Error.class,res.getClass());
        inst.logout();
    }

    @Test
    public void LoginTest_IncorrectPass() {
        Result<LoggedInUser> res = inst.login(validEmail, invalidPassword);
        Assert.assertEquals(Result.Error.class,
                res.getClass());
        Assert.assertEquals(new Exception("6, Password").toString(),
                ((Result.Error)res).getError().toString());
        inst.logout();
    }

    @Test
    public void LoginTest_IncorrectEmail() {
        Result<LoggedInUser> res = inst.login(invalidEmail, validPassword);
        Assert.assertEquals(Result.Error.class,
                res.getClass());
        Assert.assertEquals(new Exception("6, Email").toString(),
                ((Result.Error)res).getError().toString());
        inst.logout();
    }

    @Test
    public void LoginTest_IncorrectEmail_IncorrectPassword() {
        Result<LoggedInUser> res = inst.login(invalidEmail, invalidPassword);
        Assert.assertEquals(Result.Error.class,
                res.getClass());
        Assert.assertEquals(new Exception("6, Email").toString(),
                ((Result.Error)res).getError().toString());
        inst.logout();
    }
}
