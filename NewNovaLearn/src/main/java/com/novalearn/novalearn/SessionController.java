package com.novalearn.novalearn;

import com.novalearn.novalearn.dto.CreateSessionForm;
import com.novalearn.novalearn.model.Session;
import com.novalearn.novalearn.model.User;
import com.novalearn.novalearn.service.SessionService;
import com.novalearn.novalearn.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class SessionController {

    private final SessionService sessionService;
    private final UserService userService;

    public SessionController(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    private Optional<User> currentUser(Principal principal) {
        if (principal == null) return Optional.empty();
        return userService.findByEmail(principal.getName());
    }

    @SuppressWarnings("null")
    private Map<String, String> getFieldErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));
    }

    // ---- Show the sessions listing page (all active/upcoming) ----
    @GetMapping("/sessions")
    public String listSessions(@RequestParam(required = false) String error, Principal principal, Model model) {
        List<Session> sessions = sessionService.getActiveSessions();
        model.addAttribute("sessions", sessions);
        if ("ended".equals(error)) {
            model.addAttribute("errorMessage", "This live session has ended.");
        }
        currentUser(principal).ifPresent(u -> {
            model.addAttribute("nickname", u.getNickname());
            model.addAttribute("role", u.getRole().name().toLowerCase());
        });
        return "sessions";
    }

    // ---- Show create-session form (lecturers only) ----
    @GetMapping("/create-session")
    @PreAuthorize("hasRole('LECTURER')")
    public String showCreateSession(Principal principal, Model model) {
        model.addAttribute("createSessionForm", new CreateSessionForm());
        currentUser(principal).ifPresent(u -> model.addAttribute("nickname", u.getNickname()));
        return "create-session";
    }

    // ---- Handle session creation ----
    @PostMapping("/create-session")
    @PreAuthorize("hasRole('LECTURER')")
    public String handleCreateSession(
            @Valid CreateSessionForm form,
            BindingResult bindingResult,
            Principal principal,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("fieldErrors", getFieldErrors(bindingResult));
            model.addAttribute("createSessionForm", form);
            return "create-session";
        }

        Optional<User> optUser = currentUser(principal);
        if (optUser.isEmpty()) {
            return "redirect:/login";
        }

        try {
            Session session = sessionService.createSession(form.getTitle(), form.getSubject(),
                    null, optUser.get());
            return "redirect:/live-session/" + session.getRoomCode();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create session: " + e.getMessage());
            currentUser(principal).ifPresent(u -> model.addAttribute("nickname", u.getNickname()));
            return "create-session";
        }
    }

    // ---- Join a session by room code ----
    @GetMapping("/join-session")
    public String joinSession(@RequestParam String code, Principal principal, Model model) {
        Optional<Session> optSession = sessionService.findByRoomCode(code.trim().toUpperCase());
        if (optSession.isEmpty()) {
            model.addAttribute("errorMessage", "No session found with that room code. Please check and try again.");
            currentUser(principal).ifPresent(u -> {
                model.addAttribute("nickname", u.getNickname());
                model.addAttribute("role", u.getRole().name().toLowerCase());
            });
            return "sessions";
        }
        Session session = optSession.get();
        if (session.getStatus() == Session.SessionStatus.ENDED) {
            model.addAttribute("errorMessage", "This session has already ended.");
            model.addAttribute("sessions", sessionService.getActiveSessions());
            currentUser(principal).ifPresent(u -> {
                model.addAttribute("nickname", u.getNickname());
                model.addAttribute("role", u.getRole().name().toLowerCase());
            });
            return "sessions";
        }
        return "redirect:/live-session/" + session.getRoomCode();
    }

    // ---- The actual live session room page ----
    @GetMapping("/live-session/{roomCode}")
    public String liveSessionRoom(@PathVariable String roomCode, Principal principal, Model model) {
        Optional<Session> optSession = sessionService.findByRoomCode(roomCode);
        if (optSession.isEmpty()) {
            return "redirect:/sessions";
        }
        Session liveSession = optSession.get();
        if (liveSession.getStatus() == Session.SessionStatus.ENDED) {
            return "redirect:/sessions?error=ended";
        }
        model.addAttribute("liveSession", liveSession);
        model.addAttribute("jitsiRoom", "NovaLearn-" + roomCode);

        currentUser(principal).ifPresent(u -> {
            model.addAttribute("nickname", u.getNickname());
            boolean isHost = u.getEmail().equals(liveSession.getHost().getEmail());
            model.addAttribute("isHost", isHost);
        });
        return "live-session";
    }

    // ---- End a session (lecturer only) ----
    @PostMapping("/end-session/{id}")
    @PreAuthorize("hasRole('LECTURER')")
    public String endSession(@PathVariable Long id, Principal principal) {
        Optional<User> optUser = currentUser(principal);
        if (optUser.isPresent()) {
            Optional<Session> optSession = sessionService.findById(id);
            if (optSession.isPresent() && optSession.get().getHost().getEmail().equals(optUser.get().getEmail())) {
                sessionService.endSession(id);
                return "redirect:/lecturer-dashboard?success=Live+session+ended+successfully!";
            }
        }
        return "redirect:/lecturer-dashboard?error=Unable+to+end+that+session.";
    }
}
