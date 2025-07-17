package me.teixayo.bytetalk.backend.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserManagerTest {

    @Test
    public void testAddRemoveUserMethod() {
        UserManager userManager = new UserManager();
        User user = new User(10,"Test","Password",null);
        userManager.addUser(user);
        assertEquals(userManager.getUsers().get(user.getId()),user);
        userManager.removeUser(user);
        assertNotEquals(userManager.getUsers().get(user.getId()),user);

    }
}