package com.eventmanagement.EventManagement.controller;

import com.eventmanagement.EventManagement.model.entity.User;
import com.eventmanagement.EventManagement.model.request.SignUpRequest;
import com.eventmanagement.EventManagement.model.request.UserSignupRequest;
import com.eventmanagement.EventManagement.model.response.SignInResponse;
import com.eventmanagement.EventManagement.repository.UserRepository;
import com.eventmanagement.EventManagement.service.AccessService;
import com.eventmanagement.EventManagement.service.KeyService;
import com.eventmanagement.EventManagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CodeDeliveryDetailsType;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AccessController {


    @Autowired
    private AccessService accessService;

    @Autowired
    private UserService userService;

    @Autowired
    private KeyService keyService;

    @PostMapping("/keyGeneration")
    public ResponseEntity<String> keyGeneration(@RequestBody Map<String,String> request){
        String uniqueId=request.get("uniqueId");
        return ResponseEntity.ok(accessService.generateKey(uniqueId));
    }

    @PostMapping("/signup")
    public ResponseEntity<JSONObject> createUser(@RequestBody UserSignupRequest userSignupRequest){
        String response=accessService.createUser(userSignupRequest);
        JSONObject obj=new JSONObject();
        obj.put("message",response);
        return ResponseEntity.ok(obj);
    }

    @PostMapping("/signup_confirmation")
    public ResponseEntity<SignInResponse> signupConfirmation(@RequestBody SignUpRequest request){
        SignInResponse signInResponse=accessService.signUpConfirmation(request);
        return ResponseEntity.ok(signInResponse);
    }

    @PostMapping("/resend_signup_confirmation")
    public ResponseEntity<String> resendSignUpConfirmation(@RequestPart String email){
        return ResponseEntity.ok(accessService.resendSignUpConfirmation(email));
    }

    @PostMapping("/login")
    public ResponseEntity<SignInResponse> login(@RequestPart String encryptedUsernamePassword, @RequestPart String uniqueId){
        return ResponseEntity.ok(accessService.decryptUsernamePassword(encryptedUsernamePassword,uniqueId));
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<com.amazonaws.services.cognitoidp.model.CodeDeliveryDetailsType> forgotPassword(@RequestPart String email){
        return ResponseEntity.ok(accessService.forgotPassword(email));
    }

    @PostMapping("/resetPassword_resend")
    public ResponseEntity<com.amazonaws.services.cognitoidp.model.CodeDeliveryDetailsType> forgotPasswordResend(@RequestPart String email){
        return ResponseEntity.ok(accessService.forgotPassword(email));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<String>confirmNewPassword(@RequestPart String encryptedEmailCodePassword,
                                                    @RequestPart String uniqueId){
        accessService.decryptEmailCodePassword(uniqueId,encryptedEmailCodePassword);
        return ResponseEntity.ok("Password updated successfully!");
    }

    @PostMapping("/change_password")
    public ResponseEntity<String>changePassword(@RequestPart String encryptedPasswords,
                                                @RequestPart String uniqueId,
                                                HttpServletRequest request){
        accessService.decryptPasswords(uniqueId,encryptedPasswords,request);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("/logout/{email}")
    public ResponseEntity<String> logout(@PathVariable("email") String email,
                                         @RequestHeader("Authorization") String authorization){
        accessService.logout(email,authorization);
        return ResponseEntity.ok("User logged out successfully");
    }

    @DeleteMapping("/delete_user")
    public ResponseEntity<String> deleteUser(@RequestPart String email){
        accessService.deleteUser(email);
        return ResponseEntity.ok("User is deleted");
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<SignInResponse> refreshToken(@RequestHeader("Authorization") String authorization){
        SignInResponse response=accessService.refreshToken(authorization);
        return ResponseEntity.ok(response);
    }


    //FrontEnd=======================================================================
    @PostMapping("/passwordEncrypt")
    public ResponseEntity<String> encryptPassword(@RequestPart String password,
                                                  @RequestPart String uniqueId){
        SecretKey key=userService.getKeyFromCache(uniqueId);
        return ResponseEntity.ok(userService.encrypt(password,key));
    }

    @PostMapping("/encryptedUsernamePassword")
    public ResponseEntity<String> encryptedUsernamePassword(@RequestPart String email,
                                                            @RequestPart String password,
                                                            @RequestPart String uniqueId){
        SecretKey key=userService.getKeyFromCache(uniqueId);
        String usernamePassword=email+":"+password;
        return ResponseEntity.ok(userService.encrypt(usernamePassword,key));
    }

    @PostMapping("/encryptedEmailCodePassword")
    public ResponseEntity<String>encryptedEmailCodePassword(@RequestPart String email,
                                                            @RequestPart String resetCode,
                                                            @RequestPart String password,
                                                            @RequestPart String uniqueId){
        SecretKey key=userService.getKeyFromCache(uniqueId);
        String encryptedData=email+":"+resetCode+":"+password;
        System.out.println(encryptedData);
        return ResponseEntity.ok(userService.encrypt(encryptedData,key));
    }

    @PostMapping("/encryptedPasswords")
    public ResponseEntity<String> encryptedPasswords(@RequestPart String oldPassword,
                                                     @RequestPart String newPassword,
                                                     @RequestPart String uniqueId) {
        SecretKey key = userService.getKeyFromCache(uniqueId);
        String passwords = oldPassword + ":" + newPassword;
        String encryptedPasswords = userService.encrypt(passwords, key);
        return ResponseEntity.ok(encryptedPasswords);
    }
}




