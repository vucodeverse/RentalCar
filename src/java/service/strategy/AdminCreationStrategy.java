package service.strategy;

import dao.LocationDAO;
import dao.UserDAO;
import dto.UserDTO;
import mapper.UserMapper;
import model.User;

public class AdminCreationStrategy
        extends AbstractUserCreationStrategy {

    public AdminCreationStrategy(
            UserDAO userDAO,
            UserMapper userMapper,
            LocationDAO locationDAO) {
        super(userDAO, userMapper, locationDAO);
    }

    @Override
    public void create(UserDTO dto, String password) {
        validateCommon(dto);

        dto.setRoleId(3); // ADMIN
        User user = buildUser(dto, password);

        // rule riêng cho admin (nếu có)
        // ví dụ: bắt buộc email công ty

        save(user);
    }
}
