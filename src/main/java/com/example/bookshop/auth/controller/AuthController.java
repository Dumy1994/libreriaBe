package com.example.bookshop.auth.controller;

import com.example.bookshop.auth.repository.UserRepository;
import com.example.bookshop.auth.JwtUtils;
import com.example.bookshop.auth.model.ERole;
import com.example.bookshop.auth.model.Role;
import com.example.bookshop.auth.model.User;
import com.example.bookshop.auth.payload.request.LoginRequest;
import com.example.bookshop.auth.payload.request.SignupRequest;
import com.example.bookshop.auth.payload.response.JwtResponse;
import com.example.bookshop.auth.payload.response.MessageResponse;
import com.example.bookshop.auth.repository.RoleRepository;
import com.example.bookshop.auth.repository.UserRepository;
import com.example.bookshop.auth.service.UserDetailsImpl;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }
        User user0 = userRepository.findByEmail(signUpRequest.getEmail());
        if (user0 != null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    // reset password
    @PostMapping("/new-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody SignupRequest signUpRequest) {
        User user1 = userRepository.findByTokenReset(signUpRequest.getToken());
        if (user1 != null) {
            user1.setPassword(encoder.encode(signUpRequest.getPassword()));
            user1.setTokenReset(null);
            userRepository.save(user1);
            return ResponseEntity.ok(new MessageResponse("Password reset successfully!"));
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Password is incorrect!"));
        }
    }

    // send email to reset password
    @PostMapping("/req-reset-password")
    public ResponseEntity<?> sendEmail(@Valid @RequestBody SignupRequest signUpRequest) {
        // set properties
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        // set session
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("ddll1592@gmail.com", "eoltbiumcxgajogs");
            }
        });

        User user1 = userRepository.findByEmail(signUpRequest.getEmail());
        if (user1 != null) {
            // set message
            // String password = user1.getPassword();
            String password = RandomString.make(10);
            user1.setTokenReset(password);
            
            userRepository.save(user1);
            String message = "http://localhost:4200/response-reset-password/" + password;

            // send email
            try {
                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress("ddll1592@gmail.com"));
                msg.setRecipient(Message.RecipientType.TO, new InternetAddress(signUpRequest.getEmail()));
                msg.setSubject("Reset Password");
                msg.setText(message);
                Transport.send(msg);
            } catch (MessagingException e) {
                e.printStackTrace();
            }

            return ResponseEntity.ok(new MessageResponse("Email sent successfully!"));

        } else {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is not found!"));
        }
    }

    // /valid-password-token
    @PostMapping("/valid-password-token")
    public ResponseEntity<?> validPasswordToken(@Valid @RequestBody SignupRequest signUpRequest) {
        Boolean user = userRepository.existsByTokenReset(signUpRequest.getToken());
        if (user) {
            return ResponseEntity.ok(new MessageResponse("Token is valid!"));
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Token is not valid!"));
        }
    }
}
