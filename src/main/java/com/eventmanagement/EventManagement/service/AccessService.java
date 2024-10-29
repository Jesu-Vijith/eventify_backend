package com.eventmanagement.EventManagement.service;

import com.eventmanagement.EventManagement.configuration.CacheRepository;
import com.eventmanagement.EventManagement.exception.CustomException;
import com.eventmanagement.EventManagement.model.entity.RoleEnum;
import com.eventmanagement.EventManagement.model.entity.User;
import com.eventmanagement.EventManagement.model.request.UserSignupRequest;
import com.eventmanagement.EventManagement.model.response.SignInResponse;
import com.eventmanagement.EventManagement.model.response.TokenInfoCache;
import com.eventmanagement.EventManagement.repository.RolesRepository;
import com.eventmanagement.EventManagement.repository.UserRepository;
import com.eventmanagement.EventManagement.security.TokenProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.http.HttpHeaders;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class AccessService {

    @Autowired
    private CacheRepository cacheRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private ModelMapper modelMapper;

    @Value(value = "${cognito.userPoolId:us-east-1_S25lwvWJG}")
    private String userPoolId;

    @Value(value = "${cognito.clientId:57l2q94j2084c1jmv0ggmhluai}")
    private String clientId;

    @Autowired
    private CognitoIdentityProviderClient cognitoClient;

    public String createUser(UserSignupRequest signupRequest) {
        Optional.ofNullable(signupRequest.getPassword())
                .orElseThrow(() -> new CustomException("Password Cannot be Null", HttpStatus.BAD_REQUEST));
        SecretKey key = null;
        try {
            key = cacheRepository.getKey(signupRequest.getUniqueId());
            String password = null;
            password = userService.decrypt(signupRequest.getPassword(), key);
            System.out.println("DecryptedPassword: " + password);
            software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest cogSignUpRequest = SignUpRequest.builder()
                    .clientId(clientId)
                    .username(signupRequest.getEmail())
                    .password(password)
                    .build();
            SignUpResponse signUpResponse;
            signUpResponse = cognitoClient.signUp(cogSignUpRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        catch (UsernameExistsException ex) {
            throw new CustomException("Cognito user already exists", HttpStatus.BAD_REQUEST, ex);
        } catch (InvalidPasswordException invalidPasswordException) {
            throw new CustomException("Cognito invalid password", HttpStatus.BAD_REQUEST, invalidPasswordException);
        } catch (InvalidParameterException ex) {
            throw new CustomException("Cognito service encounters an invalid parameter.", HttpStatus.BAD_REQUEST, ex);
        } catch (CodeDeliveryFailureException codeDeliveryFailureException) {
            throw new CustomException("cognito verification code delivery failed", HttpStatus.BAD_REQUEST, codeDeliveryFailureException);
        } catch (NotAuthorizedException notAuthorizedException) {
            notAuthorizedException.printStackTrace();
            throw new CustomException("user isn't authorized.", HttpStatus.BAD_REQUEST, notAuthorizedException);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            throw new CustomException("Cognito service can't find the requested resource.", HttpStatus.BAD_REQUEST, resourceNotFoundException);
        } catch (LimitExceededException limitExceededException) {
            throw new CustomException("Exceeded daily email limit for the operation or the account", HttpStatus.BAD_REQUEST, limitExceededException);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CustomException("Cognito error", HttpStatus.INTERNAL_SERVER_ERROR, ex);
        }

        User user = new User();
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setAge(signupRequest.getAge());
        user.setAddress(signupRequest.getAddress());
        user.setGender(signupRequest.getGender());
        user.setProfession(signupRequest.getProfession());
        user.setMobileNumber(signupRequest.getMobileNumber());
        user.setAadharNumber(signupRequest.getAadharNumber());
        if (signupRequest.getUserType().equalsIgnoreCase("organizer")) {
            user.setRole(RoleEnum.ORGANIZER);
            user.setRoles(rolesRepository.findByRoleName(user.getRole()));
        } else {
            user.setRole(RoleEnum.ATTENDEE);
            user.setRoles(rolesRepository.findByRoleName(user.getRole()));
        }
        userRepository.save(user);
        return "User created successfully";
    }



    public SignInResponse signUpConfirmation(com.eventmanagement.EventManagement.model.request.SignUpRequest request) {
        User user=userRepository.findByEmail(request.getEmail())
                .orElseThrow(()->new CustomException("User Not Found", HttpStatus.BAD_REQUEST));
        ConfirmSignUpRequest confirmSignUpRequest=ConfirmSignUpRequest.builder()
                .clientId(clientId)
                .username(request.getEmail())
                .confirmationCode(request.getConfirmationCode())
                .build();
        ConfirmSignUpResponse response=null;
        SignInResponse signInResponse=new SignInResponse();

        try {
            SecretKey key=cacheRepository.getKey(request.getUniqueId());
            response = cognitoClient.confirmSignUp(confirmSignUpRequest);
            if (response != null) {
                String password = userService.decrypt(request.getPassword(),key);
                final Map<String, String> authParams = new HashMap<>();
                String newUserName = request.getEmail();
                authParams.put("USERNAME", newUserName);
                authParams.put("PASSWORD", password);
                AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                        .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                        .clientId(clientId)
                        .userPoolId(userPoolId)
                        .authParameters(authParams)
                        .build();
                AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);
                AuthenticationResultType authenticationResult= authResponse.authenticationResult();

                if (authenticationResult.accessToken() != null) {
                    user.setIsActive(true);
                    userRepository.save(user);
                    signInResponse = tokenProvider.createIdmToken(user,
                            authenticationResult.expiresIn().longValue(),
                            authenticationResult.accessToken(),
                            authenticationResult.refreshToken(),
                            authenticationResult.idToken());

                } else {
                    throw new CustomException("Incorrect EmailId or password", HttpStatus.BAD_REQUEST);
                }

            } else {
                throw new CustomException("Error while user confirmation", HttpStatus.BAD_REQUEST);
            }
        } catch (CodeMismatchException ex) {
            throw new CustomException("Confirmation code doesn't match", HttpStatus.BAD_REQUEST, ex);
        } catch (ExpiredCodeException ex) {
            throw new CustomException("Confirmation code has expired.", HttpStatus.BAD_REQUEST, ex);
        } catch (InternalErrorException ex) {
            throw new CustomException("Cognito encounters an internal error.", HttpStatus.BAD_REQUEST, ex);
        } catch (InvalidParameterException ex) {
            throw new CustomException("Cognito service encounters an invalid parameter", HttpStatus.BAD_REQUEST, ex);
        } catch (NotAuthorizedException ex) {
            throw new CustomException("user isn't authorized.", HttpStatus.BAD_REQUEST, ex);
        } catch (ResourceNotFoundException ex) {
            throw new CustomException("Cognito service can't find the requested resource", HttpStatus.BAD_REQUEST, ex);
        } catch (UserNotFoundException ex) {
            throw new CustomException("user isn't found.", HttpStatus.BAD_REQUEST, ex);
        } catch (DataIntegrityViolationException exception) {
            exception.printStackTrace();
            throw new CustomException(
                    Objects.requireNonNull(exception.getRootCause(), exception.getMessage()).getMessage(),
                    HttpStatus.BAD_REQUEST);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return signInResponse;
    }

    public String resendSignUpConfirmation(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException("User Not Found", HttpStatus.NOT_FOUND));
        ResendConfirmationCodeRequest resendConfirmationCodeRequest = ResendConfirmationCodeRequest.builder()
                .clientId(clientId)
                .username(email)
                .build();

        ResendConfirmationCodeResponse result = null;
        try {
            result = cognitoClient.resendConfirmationCode(resendConfirmationCodeRequest);
        } catch (LimitExceededException ex) {
            throw new CustomException("Attempt limit exceeded,please try after some time", HttpStatus.BAD_REQUEST, ex);
        } catch (ExpiredCodeException ex) {
            throw new CustomException("Confirmation code has expired", HttpStatus.BAD_REQUEST, ex);
        } catch (InternalErrorException ex) {
            throw new CustomException("Cognito encounters an internal error", HttpStatus.BAD_REQUEST, ex);
        } catch (com.amazonaws.services.cognitoidp.model.InvalidParameterException ex) {
            throw new CustomException("Cognito service encounters an invalid parameter", HttpStatus.BAD_REQUEST, ex);
        } catch (NotAuthorizedException ex) {
            throw new CustomException("User isn't authorized", HttpStatus.BAD_REQUEST, ex);
        } catch (ResourceNotFoundException ex) {
            throw new CustomException("Cognito service can't find the requested resource", HttpStatus.BAD_REQUEST, ex);
        } catch (UserNotFoundException ex) {
            throw new CustomException("User isn't found", HttpStatus.BAD_REQUEST, ex);
        }
        if (result != null) {
            return "Resend  confirmation code to given mail successfully";
        }
        return null;
    }

    public SignInResponse decryptUsernamePassword(String encryptedEmailPassword, String uniqueId) {
        try {
            SecretKey key=cacheRepository.getKey(uniqueId);
            String decryptedData=userService.decrypt(encryptedEmailPassword,key);
            String []strings=decryptedData.split(":");
            String email=strings[0];
            String password=strings[1];
            System.out.println(email);
            System.out.println(password);

            return loginUser(email,password);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SignInResponse loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Enter the valid EmailId", HttpStatus.NOT_FOUND));
        SignInResponse response = new SignInResponse();
        Map<String, String> authParams = new HashMap<>();
        String newUserName = user.getEmail();
        authParams.put("USERNAME", newUserName);
        authParams.put("PASSWORD", password);
        AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .clientId(clientId)
                .userPoolId(userPoolId)
                .authParameters(authParams)
                .build();
        AuthenticationResultType authenticationResult = null;
        try {
            AdminInitiateAuthResponse result = cognitoClient.adminInitiateAuth(authRequest);
            authenticationResult = result.authenticationResult();
            if (authenticationResult.accessToken() != null) {
                response = tokenProvider.createIdmToken(user,
                        authenticationResult.expiresIn().longValue(), authenticationResult.accessToken(),
                        authenticationResult.refreshToken(), authenticationResult.idToken());
            } else {
                throw new CustomException("Incorrect EmailId or password", HttpStatus.BAD_REQUEST);
            }
            return response;
        } catch (com.amazonaws.services.cognitoidp.model.NotAuthorizedException ex) {
            throw new CustomException("Incorrect EmailId or password", HttpStatus.INTERNAL_SERVER_ERROR, ex);
        } catch (InvalidParameterException ex) {
            throw new CustomException("Amazon Cognito service encounters an invalid parameter",
                    HttpStatus.INTERNAL_SERVER_ERROR, ex);
        } catch (com.amazonaws.services.cognitoidp.model.ResourceNotFoundException ex) {
            throw new CustomException("Amazon Cognito service can't find the requested resource",
                    HttpStatus.INTERNAL_SERVER_ERROR, ex);
        } catch (com.amazonaws.services.cognitoidp.model.UserNotConfirmedException ex) {
            throw new CustomException("user isn't confirmed successfully", HttpStatus.INTERNAL_SERVER_ERROR, ex);
        } catch (NullPointerException ex) {
            throw new CustomException("Give the valid EmailId/password", HttpStatus.BAD_REQUEST, ex);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public com.amazonaws.services.cognitoidp.model.CodeDeliveryDetailsType  forgotPassword(String email) {
        User user=userRepository.findByEmail(email).orElseThrow(
                ()->new CustomException("User Not Found",HttpStatus.BAD_REQUEST));
        try {
            com.amazonaws.services.cognitoidp.model.CodeDeliveryDetailsType details = new com.amazonaws.services.cognitoidp.model.CodeDeliveryDetailsType();
            ForgotPasswordRequest forgotPasswordRequest = ForgotPasswordRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .build();
            ForgotPasswordResponse response = cognitoClient.forgotPassword(forgotPasswordRequest);
            CodeDeliveryDetailsType details1 = response.codeDeliveryDetails();
            details.setAttributeName(details1.attributeName());
            details.setDeliveryMedium(details1.deliveryMediumAsString());
            details.setDestination(details1.destination());
            return details;
        }
        catch (
                com.amazonaws.services.cognitoidp.model.UserNotFoundException ex) {
            throw new CustomException("User not found in cognito", HttpStatus.NOT_FOUND, ex);
        } catch (
                com.amazonaws.services.cognitoidp.model.LimitExceededException e) {
            throw new CustomException("Attempt limit exceeded, please try after some time", HttpStatus.BAD_REQUEST, e);
        } catch (Exception e) {
            throw new CustomException("Failed to send OTP", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void decryptEmailCodePassword(String uniqueId, String encryptedEmailCodePassword) {
        try {
            SecretKey key=cacheRepository.getKey(uniqueId);
            String decryptData=userService.decrypt(encryptedEmailCodePassword,key);
            String[]strings=decryptData.split(":");
            String email=strings[0];
            String resetCode=strings[1];
            String password=strings[2];
            updateForgotPassword(email,resetCode,password);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateForgotPassword(String email, String resetCode, String password) {
        ConfirmForgotPasswordRequest request=ConfirmForgotPasswordRequest.builder()
                .clientId(clientId)
                .username(email)
                .confirmationCode(resetCode)
                .password(password)
                .build();
        ConfirmForgotPasswordResponse response=cognitoClient.confirmForgotPassword(request);
    }

    public void decryptPasswords(String uniqueId, String encryptedPasswords, HttpServletRequest request) {
        try {
            SecretKey key=cacheRepository.getKey(uniqueId);
            String decryptedPassword=userService.decrypt(encryptedPasswords,key);
            String[]strings=decryptedPassword.split(":");
            String oldPassword=strings[0];
            String newPassword=strings[1];
            changePassword(oldPassword,newPassword,request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void changePassword(String oldPassword,String newPassword,HttpServletRequest request){
        try {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            String token = authHeader.substring(7);
            TokenInfoCache tokenInfoCache = cacheRepository.getTokenInfo(token);

            if (tokenInfoCache == null) {
                throw new CustomException("Enter valid Email", HttpStatus.BAD_REQUEST);
            }

            ChangePasswordRequest passwordRequest = ChangePasswordRequest.builder()
                    .accessToken(tokenInfoCache.getCognitoAccessToken())
                    .previousPassword(oldPassword)
                    .proposedPassword(newPassword)
                    .build();
            ChangePasswordResponse response = cognitoClient.changePassword(passwordRequest);
        }
        catch (
                com.amazonaws.services.cognitoidp.model.InternalErrorException ex) {
            throw new CustomException("Cognito encounters an internal error", HttpStatus.INTERNAL_SERVER_ERROR, ex);
        } catch (
                com.amazonaws.services.cognitoidp.model.InvalidPasswordException ex) {
            throw new CustomException("Cognito encounters an invalid password", HttpStatus.BAD_REQUEST, ex);
        } catch (
                com.amazonaws.services.cognitoidp.model.NotAuthorizedException ex) {
            throw new CustomException("User isn't authorized", HttpStatus.BAD_REQUEST, ex);
        } catch (
                com.amazonaws.services.cognitoidp.model.LimitExceededException ex) {
            throw new CustomException("Attempt limit exceeded, please try after some time",
                    HttpStatus.BAD_REQUEST, ex);
        }
    }

    public void logout(String email, String authorization) {
        authorization=authorization.substring(7);
        String accessToken=authorization.trim();
        TokenInfoCache tokenInfoCache=cacheRepository.getTokenInfo(authorization);
        if(tokenInfoCache==null){
            throw new CustomException("Enter valid emailID",HttpStatus.NOT_FOUND);
        }
        AdminUserGlobalSignOutRequest signOutRequest= AdminUserGlobalSignOutRequest.builder()
                .username(email)
                .userPoolId(userPoolId)
                .build();
        AdminUserGlobalSignOutResponse response=cognitoClient.adminUserGlobalSignOut(signOutRequest);
        try {
            cacheRepository.signOutTokenInfo(accessToken);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public void deleteUser(String email) {
        User user=userRepository.findByEmail(email).orElseThrow(()->new CustomException("User not Found",HttpStatus.NOT_FOUND));
        AdminDeleteUserRequest deleteUserRequest= AdminDeleteUserRequest.builder()
                .userPoolId(userPoolId)
                .username(email)
                .build();
        AdminDeleteUserResponse response=cognitoClient.adminDeleteUser(deleteUserRequest);
        userRepository.delete(user);
    }

    public SignInResponse refreshToken(String refreshToken){
        refreshToken = refreshToken.substring(7);
        TokenInfoCache tokenInfoCache = cacheRepository.getUserDataByRefreshToken(refreshToken);
        String cognitoRefreshToken = tokenInfoCache.getCognitoRefreshToken();
        User user = modelMapper.map(tokenInfoCache, User.class);
        user.setUserId(tokenInfoCache.getUserId());

        SignInResponse signInResponse = new SignInResponse();
        if (user == null) {
            throw new CustomException("User not found", HttpStatus.NOT_FOUND);
        }
        if (cognitoRefreshToken != null) {
            final Map<String, String> authParams = new HashMap<>();
            authParams.put("REFRESH_TOKEN", cognitoRefreshToken);
            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                    .clientId(clientId)
                    .userPoolId(userPoolId)
                    .authParameters(authParams)
                    .build();

            AuthenticationResultType authenticationResult = null;
            try {
                AdminInitiateAuthResponse result = cognitoClient.adminInitiateAuth(authRequest);
                authenticationResult = result.authenticationResult();
                SignInResponse response = null;
                if (authenticationResult.accessToken() != null) {
                    response = tokenProvider.userRefreshIdmToken(user, cognitoRefreshToken, refreshToken, authenticationResult.accessToken(), authenticationResult.idToken(), authenticationResult.expiresIn().longValue());
                    signInResponse.setUserId(response.getUserId());
                    signInResponse.setEmail(response.getEmail());
                    signInResponse.setIdmAccessToken(response.getIdmAccessToken());
                    signInResponse.setIdmRefreshToken(response.getIdmRefreshToken());
                    signInResponse.setName(response.getName());
                    signInResponse.setIsActive(response.getIsActive());
                    signInResponse.setExpiresIn(response.getExpiresIn());
                    signInResponse.setRole(response.getRole());
                } else {
                    throw new CustomException("In-valid Token", HttpStatus.BAD_REQUEST);
                }
            } catch (com.amazonaws.services.cognitoidp.model.NotAuthorizedException e) {
                throw new CustomException("Invalid refresh token", HttpStatus.INTERNAL_SERVER_ERROR, e);
            } catch (InvalidParameterException e) {
                throw new CustomException("Amazon Cognito service encounters an invalid parameter", HttpStatus.INTERNAL_SERVER_ERROR, e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return signInResponse;
    }

    public String generateKey() {
        int n = 256;
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(n);
            String uniqueId = UUID.randomUUID().toString();
            cacheRepository.setKey(uniqueId, keyGen.generateKey());
            return uniqueId;
        } catch (NoSuchAlgorithmException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }


}
