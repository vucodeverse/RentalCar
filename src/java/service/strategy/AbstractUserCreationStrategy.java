package service.strategy;

import dao.LocationDAO;
import dao.UserDAO;
import dto.UserDTO;
import mapper.UserMapper;
import model.User;
import util.PasswordUtil;
import util.MessageUtil;
import util.exception.BusinessException;
import util.exception.ValidationException;

public abstract class AbstractUserCreationStrategy
        implements UserCreationStrategy {

    protected final UserDAO userDAO;
    protected final UserMapper userMapper;
    protected final LocationDAO locationDAO;

    protected AbstractUserCreationStrategy(
            UserDAO userDAO,
            UserMapper userMapper,
            LocationDAO locationDAO) {
        this.userDAO = userDAO;
        this.userMapper = userMapper;
        this.locationDAO = locationDAO;
    }

    protected void validateCommon(UserDTO dto) {
        if (userDAO.getUserByUsername(dto.getUsername()).isPresent()) {
            throw new ValidationException(
                    MessageUtil.getError("error.username.exists"));
        }
        if (userDAO.existsEmail(dto.getEmail())) {
            throw new ValidationException(
                    MessageUtil.getError("error.email.exists"));
        }
        if (userDAO.existsPhone(dto.getPhone())) {
            throw new ValidationException(
                    MessageUtil.getError("error.phone.exists"));
        }
    }

    protected User buildUser(UserDTO dto, String password) {

        Integer locationId = dto.getLocationId();
        if (locationId == null && dto.getCity() != null) {
            locationId = locationDAO.getOrCreateIdByCity(dto.getCity());
        }

        byte[] salt = PasswordUtil.generateSalt();
        byte[][] hash = PasswordUtil.hashPassword(password, salt);

        User user = userMapper.toModel(dto);
        user.setPasswordHash(hash[0]);
        user.setPasswordSalt(hash[1]);
        user.setLocationId(locationId);

        return user;
    }

    protected void save(User user) {
        boolean ok = userDAO.createUser(user);
        if (!ok) {
            throw new BusinessException(
                    MessageUtil.getError("error.user.add.failed"));
        }
    }
}
