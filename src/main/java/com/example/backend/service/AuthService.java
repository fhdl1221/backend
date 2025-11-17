package com.example.backend.service;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.SignUpRequest;
import com.example.backend.model.InitialSurvey;
import com.example.backend.model.User;
import com.example.backend.model.UserPreference;
import com.example.backend.repository.InitialSurveyRepository;
import com.example.backend.repository.UserPreferenceRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserPreferenceRepository userPreferenceRepository;
    @Autowired
    private InitialSurveyRepository initialSurveyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String login(LoginRequest loginRequest) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getEmail(),
                                loginRequest.getPassword()
                        )
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return "User logged in successfully!";
        }

        // 회원가입 처리 - 3개 테이블을 동시 저장하기 때문에 transactional 필수
        @Transactional
        public User signup(SignUpRequest signUpRequest) {
//        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
//            throw new RuntimeException("Error: Username is already taken!");
//        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User();
        // user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setIndustry(signUpRequest.getIndustry());
        user.setCareerYears(signUpRequest.getCareerYears());

        // User 저장 (UserRepository 필요)
        User savedUser = userRepository.save(user);

        // UserPreference 엔티티 생성 및 저장
        UserPreference prefs = new UserPreference();
        prefs.setUser(savedUser);
        prefs.setNotificationEnable(signUpRequest.isAllowNotification());
        if(signUpRequest.getPreferences() != null) {
            prefs.setContentTypeVideo(signUpRequest.getPreferences().getOrDefault("video", false));
            prefs.setContentTypeAudio(signUpRequest.getPreferences().getOrDefault("audio", false));
            prefs.setContentTypeText(signUpRequest.getPreferences().getOrDefault("text", false));
        }
        userPreferenceRepository.save(prefs);

        // InitialSurvey 엔티티 생성 및 저장
        InitialSurvey survey = new InitialSurvey();
        survey.setUser(savedUser);
        List<String> answers = signUpRequest.getSurveyAnswers();
        if(answers != null && answers.size() == 5) {
            survey.setQuestion1(answers.get(0));
            survey.setQuestion2(answers.get(1));
            survey.setQuestion3(answers.get(2));
            survey.setQuestion4(answers.get(3));
            survey.setQuestion5(answers.get(4));
            survey.setStressScore(calculateStressScore(answers));
        }
        initialSurveyRepository.save(survey);

        return savedUser;
     }

     private Integer calculateStressScore(List<String> answers) {
        int score = 0;
        for (String answer : answers) {
            if("yes".equalsIgnoreCase(answer))
                score++;
        }
        return score;
     }
}