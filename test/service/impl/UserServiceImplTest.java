/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package service.impl;

import dao.LocationDAO;
import dao.UserDAO;
import dto.UserDTO;
import java.util.Optional;
import mapper.UserMapper;
import model.User;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import util.PasswordUtil;
import util.exception.DataAccessException;
import util.exception.ValidationException;

/**
 *
 * @author DELL
 */
public class UserServiceImplTest {

    private UserServiceImpl userService;

    private UserDAO userDAOMock;
    private UserMapper userMapperMock;
    private LocationDAO locationDAOMock;

    @Before
    public void setUp() throws Exception {
        //Tạo mock
        userDAOMock = mock(UserDAO.class);
        userMapperMock = mock(UserMapper.class);
        locationDAOMock = mock(LocationDAO.class);

        //Tạo service
        userService = new UserServiceImpl();

        //Inject mock bằng reflection
        inject(userService, "usersDAO", userDAOMock);
        inject(userService, "userMapper", userMapperMock);
        inject(userService, "locationsDAO", locationDAOMock);
    }

    // helper inject private field
    private void inject(Object target, String field, Object value) throws Exception {
        var f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    /*
    //Test login admin thành công
    @Test
    public void testLoginUser_success() {
        // given
        User user = new User();
        user.setUserId(1);
        user.setUsername("admin");

        when(userDAOMock.getUserByUsername("admin"))
                .thenReturn(Optional.of(user));

        UserDTO dto = new UserDTO();
        when(userMapperMock.toDTO(user)).thenReturn(dto);

        try (var mocked = mockStatic(util.PasswordUtil.class)) {
            mocked.when(()
                    -> util.PasswordUtil.verifyPassword(any(), any(), any()))
                    .thenReturn(true);

            // when
            Optional<UserDTO> result
                    = userService.loginUser("admin", "123456");

            // then
            assertTrue(result.isPresent());
            assertEquals(dto, result.get());
        }
    }


    //Test login nhưng không có username
    @Test(expected = ValidationException.class)
    public void testLoginUser_userNotFound() {
        when(userDAOMock.getUserByUsername("admin"))
                .thenReturn(Optional.empty());

        userService.loginUser("admin", "123456");
    }

    //Test login sai mk
    @Test(expected = ValidationException.class)
    public void testLoginUser_wrongPassword() {
        User user = new User();
        when(userDAOMock.getUserByUsername(any()))
                .thenReturn(Optional.of(user));

        try (var mocked = mockStatic(util.PasswordUtil.class)) {
            mocked.when(()
                    -> util.PasswordUtil.verifyPassword(any(), any(), any()))
                    .thenReturn(false);

            userService.loginUser("admin", "wrong");
        }
    }
     */
    //============================================================
    @Test
    public void testAddUser_success1() {
        // given
        UserDTO dto = new UserDTO();
        dto.setUsername("test");
        dto.setEmail("test@gmail.com");
        dto.setPhone("0123456789");
        dto.setCity("Hanoi");

        User user = new User();

        when(userDAOMock.getUserByUsername("test"))
                .thenReturn(Optional.empty());
        when(userDAOMock.existsEmail("test@gmail.com"))
                .thenReturn(false);
        when(userDAOMock.existsPhone("0123456789"))
                .thenReturn(false);
        when(locationDAOMock.getOrCreateIdByCity("Hanoi"))
                .thenReturn(1);
        when(userMapperMock.toModel(dto))
                .thenReturn(user);
        doReturn(true)
                .when(userDAOMock)
                .createUser(any(User.class));

        try (var mocked = mockStatic(PasswordUtil.class)) {

            mocked.when(PasswordUtil::generateSalt)
                    .thenReturn(new byte[]{1, 2, 3});

            mocked.when(()
                    -> PasswordUtil.hashPassword(anyString(), any(byte[].class))
            ).thenReturn(new byte[][]{
                new byte[]{9, 9, 9},
                new byte[]{1, 2, 3}
            });

            // when
            userService.addUser(dto, "123456");
        }

        // then
        verify(userDAOMock).createUser(any(User.class));
        verify(userMapperMock).toModel(dto);
        verify(locationDAOMock).getOrCreateIdByCity("Hanoi");

        //kiểm tra dữ liệu đã được set
        assertArrayEquals(new byte[]{9, 9, 9}, user.getPasswordHash());
        assertArrayEquals(new byte[]{1, 2, 3}, user.getPasswordSalt());
    }

    //Test add user thành công
    @Test
    public void testAddUser_success() {
        // given
        UserDTO dto = new UserDTO();
        dto.setUsername("test");
        dto.setEmail("test@gmail.com");
        dto.setPhone("0123456789");
        dto.setCity("Hanoi");

        User user = new User();

        // when
        when(userDAOMock.getUserByUsername("test"))
                .thenReturn(Optional.empty());
        when(userDAOMock.existsEmail("test@gmail.com"))
                .thenReturn(false);
        when(userDAOMock.existsPhone("0123456789"))
                .thenReturn(false);

        when(locationDAOMock.getOrCreateIdByCity("Hanoi"))
                .thenReturn(1);

        when(userMapperMock.toModel(dto)).thenReturn(user);
        doReturn(true)
                .when(userDAOMock)
                .createUser(any(User.class));

        try (var mocked = mockStatic(PasswordUtil.class)) {
            mocked.when(PasswordUtil::generateSalt)
                    .thenReturn(new byte[]{1, 2, 3});

            mocked.when(()
                    -> PasswordUtil.hashPassword(any(), any())
            ).thenReturn(new byte[][]{
                new byte[]{9, 9, 9},
                new byte[]{1, 2, 3}
            });

            userService.addUser(dto, "123456");
        }

        verify(userDAOMock).createUser(any(User.class));
    }

    ////================================Test nhập username toàn dấu cách
    @Test
    public void testAddUser_usernameOnlySpaces() {

        UserDTO dto = new UserDTO();
        dto.setUsername("        ");
        dto.setEmail("test@gmail.com");
        dto.setPhone("0123456789");
        dto.setCity("Hanoi");

        // when + then
        assertThrows(ValidationException.class, ()
                -> userService.addUser(dto, "123456")
        );

        // đảm bảo không tạo user trong DB
        verify(userDAOMock, never()).createUser(any());
    }

    //================================Test user với username null
    @Test
    public void testAddUser_usernameNull() {
        UserDTO dto = new UserDTO();
        dto.setUsername(null);
        dto.setEmail("test@gmail.com");
        dto.setPhone("0123456789");
        dto.setCity("Hanoi");

        assertThrows(DataAccessException.class, ()
                -> userService.addUser(dto, "123456")
        );

        verify(userDAOMock, never()).getUserByUsername(any());
        verify(userDAOMock, never()).createUser(any());
    }

    //Test nhập username thừa dấu cách
    @Test
    public void testAddUser_usernameWithExtraSpaces() {
        // given
        UserDTO dto = new UserDTO();
        dto.setUsername(" admin ");
        dto.setEmail("test@gmail.com");
        dto.setPhone("0123456789");
        dto.setCity("Hanoi");

        // when + then
        assertThrows(ValidationException.class, ()
                -> userService.addUser(dto, "123456")
        );

        // đảm bảo DAO KHÔNG BAO GIỜ được gọi
        verify(userDAOMock, never()).createUser(any());
    }

    //================================Test add username nhưng phone đã tồn tại
    @Test(expected = ValidationException.class)
    public void testAddUser_phoneExists() {
        // given
        UserDTO dto = new UserDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@gmail.com");
        dto.setPhone("0123456789");

        // when
        // username chưa tồn tại
        when(userDAOMock.getUserByUsername("testuser"))
                .thenReturn(Optional.empty());

        when(userDAOMock.existsPhone("0123456789"))
                .thenReturn(true);

        // ===== WHEN =====
        userService.addUser(dto, "123456");

        // ===== THEN =====
        assertThrows(ValidationException.class, ()
                -> userService.addUser(dto, "123456")
        );
    }

    //================================Test add username nhưng email đã tồn tại
    @Test(expected = ValidationException.class)
    public void testAddUser_emailExits() {
        // given
        UserDTO dto = new UserDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@gmail.com");
        dto.setPhone("0123456789");

        // username chưa tồn tại
        when(userDAOMock.getUserByUsername("testuser"))
                .thenReturn(Optional.empty());

        // email đã tồn tại ❗
        when(userDAOMock.existsEmail("test@gmail.com"))
                .thenReturn(true);

        // ===== WHEN =====
        userService.addUser(dto, "123456");

        // ===== THEN =====
        assertThrows(ValidationException.class, ()
                -> userService.addUser(dto, "123456")
        );

    }

    //================================Test add username nhưng username đã tồn tại
    @Test(expected = ValidationException.class)
    public void testAddUser_usernameExists() {
        // given
        UserDTO dto = new UserDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@gmail.com");
        dto.setPhone("0123456789");

        User existingUser = new User();
        existingUser.setUsername("testuser");

        // username ĐÃ tồn tại ❗
        when(userDAOMock.getUserByUsername("testuser"))
                .thenReturn(Optional.of(existingUser));

        // ===== WHEN =====
        userService.addUser(dto, "123456");

        // ===== THEN =====
        assertThrows(ValidationException.class, ()
                -> userService.addUser(dto, "123456")
        );
    }

    //========================================Test hashfail
    @Test
    public void testAddUser_passwordCausesDataAccessException() {

        // ===== GIVEN =====
        UserDTO dto = new UserDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@gmail.com");
        dto.setPhone("0123456789");
        dto.setCity("Hanoi");

        User user = new User();

        when(userDAOMock.getUserByUsername("testuser"))
                .thenReturn(Optional.empty());
        when(userDAOMock.existsEmail("test@gmail.com"))
                .thenReturn(false);
        when(userDAOMock.existsPhone("0123456789"))
                .thenReturn(false);
        when(locationDAOMock.getOrCreateIdByCity("Hanoi"))
                .thenReturn(1);
        when(userMapperMock.toModel(dto))
                .thenReturn(user);
        doReturn(true).when(userDAOMock).createUser(any());

        // ===== WHEN + THEN =====
        try (var mocked = mockStatic(PasswordUtil.class)) {

            mocked.when(PasswordUtil::generateSalt)
                    .thenReturn(new byte[]{1, 2, 3});

            //giả lập lỗi khi hash password
            mocked.when(()
                    -> PasswordUtil.hashPassword(anyString(), any(byte[].class))
            ).thenThrow(new RuntimeException("Hash failed"));

            assertThrows(DataAccessException.class, ()
                    -> userService.addUser(dto, "123456")
            );
        }

        // đảm bảo KHÔNG tạo user trong DB
        verify(userDAOMock, never()).createUser(any());
    }

    //=======================================================
    @Test
    public void testAddUser_passwordNull() {

        // ===== GIVEN =====
        UserDTO dto = new UserDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@gmail.com");
        dto.setPhone("0123456789");
        dto.setCity("Hanoi");

        User user = new User();

        // các điều kiện hợp lệ để đi đến bước hash password
        when(userDAOMock.getUserByUsername("testuser"))
                .thenReturn(Optional.empty());
        when(userDAOMock.existsEmail("test@gmail.com"))
                .thenReturn(false);
        when(userDAOMock.existsPhone("0123456789"))
                .thenReturn(false);
        when(locationDAOMock.getOrCreateIdByCity("Hanoi"))
                .thenReturn(1);
        when(userMapperMock.toModel(dto))
                .thenReturn(user);
        doReturn(true).when(userDAOMock).createUser(any());

        // ===== WHEN + THEN =====
        try (var mocked = mockStatic(PasswordUtil.class)) {

            mocked.when(PasswordUtil::generateSalt)
                    .thenReturn(new byte[]{1, 2, 3});

            //pass null hash có khả năng lỗi
            mocked.when(()
                    -> PasswordUtil.hashPassword(null, new byte[]{1, 2, 3})
            ).thenThrow(new RuntimeException("Password null"));

            assertThrows(DataAccessException.class, ()
                    -> userService.addUser(dto, null)
            );
        }

        // đảm bảo không tạo user trong DB
        verify(userDAOMock, never()).createUser(any());
    }

    //=======================================================
    @Test(expected = ValidationException.class)
    public void testAddUser_emailWithExtraSpaces_shouldThrowValidationException() {
        // ===== GIVEN =====
        UserDTO dto = new UserDTO();
        dto.setUsername("testuser");
        dto.setEmail(" test@gmail.com ");   //email có khoảng trắng thừa
        dto.setPhone("0123456789");
        dto.setCity("Hanoi");

        // username chưa tồn tại
        when(userDAOMock.getUserByUsername("testuser"))
                .thenReturn(Optional.empty());

        // giả sử phone chưa tồn tại (để lỗi chỉ đến từ email)
        when(userDAOMock.existsPhone("0123456789"))
                .thenReturn(false);

        // ===== WHEN =====
        userService.addUser(dto, "123456");

        // ===== THEN =====
        assertThrows(ValidationException.class, ()
                -> userService.addUser(dto, "123456")
        );
    }
    
    //===============================================================
    @Test(expected = ValidationException.class)
    public void testAddUser_emailIsNull() {
        // ===== GIVEN =====
        UserDTO dto = new UserDTO();
        dto.setUsername("testuser");
        dto.setEmail(null);
        dto.setPhone("0123456789");
        dto.setCity("Hanoi");

        // username chưa tồn tại
        when(userDAOMock.getUserByUsername("testuser"))
                .thenReturn(Optional.empty());

        // phone hợp lệ
        when(userDAOMock.existsPhone("0123456789"))
                .thenReturn(false);

        // ===== WHEN =====
        userService.addUser(dto, "123456");

        // ===== THEN =====
        assertThrows(ValidationException.class, ()
                -> userService.addUser(dto, "123456")
        );
    }
    
    //===============================================================
    @Test(expected = ValidationException.class)
    public void testAddUser_phoneIsNull() {
        // ===== GIVEN =====
        UserDTO dto = new UserDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@gmail.com");
        dto.setPhone(null);
        dto.setCity("Hanoi");

        // username chưa tồn tại
        when(userDAOMock.getUserByUsername("testuser"))
                .thenReturn(Optional.empty());

        // phone hợp lệ
        when(userDAOMock.existsEmail("test@gmail.com"))
                .thenReturn(false);

        // ===== WHEN =====
        userService.addUser(dto, "123456");

        // ===== THEN =====
        assertThrows(ValidationException.class, ()
                -> userService.addUser(dto, "123456")
        );
    }
}
