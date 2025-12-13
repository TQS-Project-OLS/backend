package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testDefaultConstructor() {
        User user = new User();
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getName());
        assertNull(user.getPassword());
    }

    @Test
    void testThreeParamConstructor() {
        User user = new User("johndoe", "john@example.com", "John Doe");
        
        assertNull(user.getId());
        assertEquals("johndoe", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("John Doe", user.getName());
        assertNull(user.getPassword());
    }

    @Test
    void testFourParamConstructor() {
        User user = new User("johndoe", "john@example.com", "John Doe", "password123");
        
        assertNull(user.getId());
        assertEquals("johndoe", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("John Doe", user.getName());
        assertEquals("password123", user.getPassword());
    }

    @Test
    void testSettersAndGetters() {
        User user = new User();
        
        user.setId(1L);
        user.setUsername("janedoe");
        user.setEmail("jane@example.com");
        user.setName("Jane Doe");
        user.setPassword("securepass");
        
        assertEquals(1L, user.getId());
        assertEquals("janedoe", user.getUsername());
        assertEquals("jane@example.com", user.getEmail());
        assertEquals("Jane Doe", user.getName());
        assertEquals("securepass", user.getPassword());
    }

    @Test
    void testEquals_SameObject_ShouldReturnTrue() {
        User user = new User("johndoe", "john@example.com", "John Doe", "password123");
        user.setId(1L);
        
        assertEquals(user, user);
    }

    @Test
    void testEquals_NullObject_ShouldReturnFalse() {
        User user = new User("johndoe", "john@example.com", "John Doe", "password123");
        user.setId(1L);
        
        assertNotEquals(null, user);
    }

    @Test
    void testEquals_DifferentClass_ShouldReturnFalse() {
        User user = new User("johndoe", "john@example.com", "John Doe", "password123");
        user.setId(1L);
        
        assertNotEquals(user, "not a user");
    }

    @Test
    void testEquals_SameIdAndAllFields_ShouldReturnTrue() {
        User user1 = new User("johndoe", "john@example.com", "John Doe", "password123");
        user1.setId(1L);
        
        User user2 = new User("johndoe", "john@example.com", "John Doe", "password456"); // Different password is OK (not compared)
        user2.setId(1L);
        
        assertEquals(user1, user2);
    }

    @Test
    void testEquals_SameIdButDifferentUsername_ShouldReturnFalse() {
        User user1 = new User("johndoe", "john@example.com", "John Doe", "password123");
        user1.setId(1L);
        
        User user2 = new User("janedoe", "john@example.com", "John Doe", "password123");
        user2.setId(1L);
        
        assertNotEquals(user1, user2);
    }

    @Test
    void testEquals_DifferentId_ShouldReturnFalse() {
        User user1 = new User("johndoe", "john@example.com", "John Doe", "password123");
        user1.setId(1L);
        
        User user2 = new User("johndoe", "john@example.com", "John Doe", "password123");
        user2.setId(2L);
        
        assertNotEquals(user1, user2);
    }

    @Test
    void testEquals_BothIdsNull_DifferentUsername_ShouldReturnFalse() {
        User user1 = new User("johndoe", "john@example.com", "John Doe", "password123");
        User user2 = new User("janedoe", "john@example.com", "John Doe", "password123");
        
        assertNotEquals(user1, user2);
    }

    @Test
    void testEquals_BothIdsNull_DifferentEmail_ShouldReturnFalse() {
        User user1 = new User("johndoe", "john@example.com", "John Doe", "password123");
        User user2 = new User("johndoe", "jane@example.com", "John Doe", "password123");
        
        assertNotEquals(user1, user2);
    }

    @Test
    void testEquals_BothIdsNull_DifferentName_ShouldReturnFalse() {
        User user1 = new User("johndoe", "john@example.com", "John Doe", "password123");
        User user2 = new User("johndoe", "john@example.com", "Jane Doe", "password123");
        
        assertNotEquals(user1, user2);
    }

    @Test
    void testEquals_BothIdsNull_AllFieldsEqual_ShouldReturnTrue() {
        User user1 = new User("johndoe", "john@example.com", "John Doe", "password123");
        User user2 = new User("johndoe", "john@example.com", "John Doe", "password123");
        
        assertEquals(user1, user2);
    }

    @Test
    void testEquals_OneIdNull_ShouldReturnFalse() {
        User user1 = new User("johndoe", "john@example.com", "John Doe", "password123");
        user1.setId(1L);
        
        User user2 = new User("johndoe", "john@example.com", "John Doe", "password123");
        
        assertNotEquals(user1, user2);
    }

    @Test
    void testEquals_OneUsernameNull_ShouldReturnFalse() {
        User user1 = new User();
        user1.setEmail("john@example.com");
        user1.setName("John Doe");
        
        User user2 = new User("johndoe", "john@example.com", "John Doe");
        
        assertNotEquals(user1, user2);
    }

    @Test
    void testEquals_OneEmailNull_ShouldReturnFalse() {
        User user1 = new User();
        user1.setUsername("johndoe");
        user1.setName("John Doe");
        
        User user2 = new User("johndoe", "john@example.com", "John Doe");
        
        assertNotEquals(user1, user2);
    }

    @Test
    void testEquals_OneNameNull_ShouldReturnFalse() {
        User user1 = new User();
        user1.setUsername("johndoe");
        user1.setEmail("john@example.com");
        
        User user2 = new User("johndoe", "john@example.com", "John Doe");
        
        assertNotEquals(user1, user2);
    }

    @Test
    void testEquals_AllFieldsNull_ShouldReturnTrue() {
        User user1 = new User();
        User user2 = new User();
        
        assertEquals(user1, user2);
    }

    @Test
    void testHashCode_SameData_ShouldHaveSameHashCode() {
        User user1 = new User("johndoe", "john@example.com", "John Doe", "password123");
        user1.setId(1L);
        
        User user2 = new User("johndoe", "john@example.com", "John Doe", "password123");
        user2.setId(1L);
        
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testHashCode_DifferentData_MayHaveDifferentHashCode() {
        User user1 = new User("johndoe", "john@example.com", "John Doe", "password123");
        user1.setId(1L);
        
        User user2 = new User("janedoe", "jane@example.com", "Jane Doe", "password456");
        user2.setId(2L);
        
        assertNotEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testHashCode_NullFields_ShouldNotThrowException() {
        User user = new User();
        
        assertDoesNotThrow(() -> user.hashCode());
    }
}
