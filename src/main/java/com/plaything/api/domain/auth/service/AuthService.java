package com.plaything.api.domain.auth.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.model.request.LoginRequest;
import com.plaything.api.domain.auth.model.response.CreateUserResponse;
import com.plaything.api.domain.auth.model.response.LoginResponse;
import com.plaything.api.domain.repository.UserRepository;
import com.plaything.api.domain.repository.entity.User;
import com.plaything.api.domain.repository.entity.UserCredentials;
import com.plaything.api.security.Hasher;
import com.plaything.api.security.JWTProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final Hasher hasher;

    public String getUserFromToken(String token){
        return JWTProvider.getUserFromToken(token);
    }

    public LoginResponse login(LoginRequest loginRequest){
        Optional<User> user = userRepository.findByName(loginRequest.name());
        if(!user.isPresent()){
            log.error("NOT_EXIST_USER: {}", loginRequest.name());
            throw new CustomException(ErrorCode.NOT_EXIST_USER);
        }

        user.map(u->{
            String hashingValue = hasher.getHashingValue(loginRequest.password());
            if(!u.getCredentials().getHashedPassowrd().equals(hashingValue)){
                throw new CustomException(ErrorCode.MIS_MATCH_PASSWORD);
            }
            return hashingValue;

        }).orElseThrow(()->{
            throw new CustomException(ErrorCode.MIS_MATCH_PASSWORD);
        });

        //TODO JWT

        String token = JWTProvider.createToken(loginRequest.name());

        return new LoginResponse(ErrorCode.SUCCESS, token);
    }

    @Transactional(transactionManager = "createUserTransactionManger")
    public CreateUserResponse creatUser(CreateUserRequest request){

        Optional<User> user = userRepository.findByName(request.name());

        if(user.isPresent()){
            log.error("USER_ALREADY_EXISTS: {}", request.name());
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        try{
          User newUser = this.newUser(request.name());
          UserCredentials newCredentials = this.newUserCredentials(request.password(), newUser);
          newUser.setCredentials(newCredentials);

            User savedUser = userRepository.save(newUser);

            if(savedUser ==null){
                throw new CustomException(ErrorCode.USER_SAVED_FAILED);
            }
        } catch (Exception e){
            throw new CustomException(ErrorCode.USER_SAVED_FAILED);
        }

        return new CreateUserResponse(request.name());
    }

    private User newUser(String name){
        User user = User.builder()
                .name(name)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        return user;
    }

    private UserCredentials newUserCredentials(String password, User user){
        String hashingValue = hasher.getHashingValue(password);

        UserCredentials cre = UserCredentials.builder()
                .user(user)
                .hashedPassowrd(hashingValue)
                .build();

        return cre;
    }
}
