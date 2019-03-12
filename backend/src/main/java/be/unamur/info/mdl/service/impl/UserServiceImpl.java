package be.unamur.info.mdl.service.impl;

import be.unamur.info.mdl.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;

import be.unamur.info.mdl.dal.entity.User;
import be.unamur.info.mdl.dal.repository.UserRepository;
import be.unamur.info.mdl.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is responsible of all possible services related to the /api/user
 *
 */
@Service("userService")
@Transactional
class UserServiceImpl implements UserService {

  private UserRepository userRepository;


  @Autowired // This means to get the bean called userRepository
  // Which is auto-generated by Spring, we will use it to handle the data
  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }


  @Override
  public boolean register(UserDTO newUser) {
    // TODO Check if the userDTO is not null

    // TODO Check if the UserDTO's email is already taken

    // TODO Check if the UserDTO's usernameis already take

    // userRepository.save(newUser);
    return true;
  }

  @Override
  public boolean login(UserDTO user) {
    return false;
  }

  @Override
  public Boolean checkPassword(UserDTO loginCredentials, UserDTO databaseUser) {
    return null;
  }
}