package com.auth.controller;

import com.auth.dto.AuthenticationRequest;
import com.auth.dto.ChangePasswordDto;
import com.auth.dto.SignupRequest;
import com.auth.dto.UserDto;
import com.auth.entity.User;
import com.auth.repository.UserRepository;
import com.auth.services.auth.AuthService;
import com.auth.utils.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final UserDetailsService userDetailsService;

    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    private final AuthService authService;


    @PostMapping("/login")
    public void createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest,
                                          HttpServletResponse response) throws IOException, JSONException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(),
                    authenticationRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Incorrect username or password.");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());
        Optional<User> optionalUser = userRepository.findFirstByEmail(userDetails.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername(),optionalUser.get().getRole().name());

        if (optionalUser.isPresent()) {
        	 User user = optionalUser.get();
             // Check if the password needs to be updated
             if (authService.checkIfPasswordNeedsUpdate(user)) {
             	response.setStatus(HttpServletResponse.SC_FORBIDDEN);
             	response.getWriter().write("Changement de mot de passe requis.");
             	response.getWriter().flush();
             	return;
             }
            response.getWriter().write(new JSONObject()
                    .put("userId", optionalUser.get().getId())
                    .put("role", optionalUser.get().getRole())
                    .toString()
            );

            response.addHeader("Access-Control-Expose-Headers", "Authorization");
            response.addHeader("Access-Control-Allow-Headers", "Authorization, X-PINGOTHER, Origin, " +
                    "X-Requested-With, Content-Type, Accept, X-Custom-header");
            response.addHeader(HEADER_STRING, TOKEN_PREFIX + jwt);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signupUser(@RequestBody SignupRequest signupRequest) {
        if (authService.hasUserWithEmail(signupRequest.getEmail())) {
            return new ResponseEntity<>("User already exists", HttpStatus.NOT_ACCEPTABLE);
        }

        UserDto userDto = authService.createUser(signupRequest);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

        @GetMapping("/user/{id}")
        private ResponseEntity<?> getUserById(@PathVariable UUID id) {
            UserDto userDto = authService.getUserById(id);
            if (userDto == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(userDto);
        }

    @GetMapping("/make-admin/{id}")
    private ResponseEntity<?> makeAdmin(@PathVariable UUID id) {
        UserDto userDto = authService.makeAdmin(id);
        if (userDto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(userDto);
    }
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = authService.getAllUsers();  // Assuming this method exists in AuthService
        return ResponseEntity.ok(users);
    }
    @PostMapping("/updatepassword")
    public ResponseEntity<?> updatePassword(@RequestBody ChangePasswordDto changePasswordDto) {
        try {
            return authService.updatePasswordById(changePasswordDto);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong");
        }
    }


    @RequestMapping(value="/usersByName/{name}",method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> findByNameContains(@PathVariable("name") String name) {
        List<UserDto> usersByName = authService.findByNameContains(name);
        return ResponseEntity.ok(usersByName);
    }
}
