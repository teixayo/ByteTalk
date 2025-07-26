package me.teixayo.bytetalk.backend.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTest {

    @Test
    public void testConstruction() {
        long id = 400L;
        String name = "Test";
        String password = "MyPassword";
        UserConnection userConnection = new UserConnection(null, null);
        User user = new User(id, name, password, userConnection);

        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(password, user.getPassword());
        assertEquals(userConnection, user.getUserConnection());
    }


}