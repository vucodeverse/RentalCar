/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package service.strategy;

import dto.UserDTO;

/**
 *
 * @author DELL
 */
public interface UserCreationStrategy {

    void create(UserDTO userDTO, String password);
}
