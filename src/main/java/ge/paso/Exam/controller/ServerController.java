package ge.paso.Exam.controller;

import ge.paso.Exam.Service.ServerService;
import ge.paso.Exam.dto.ServerCreationDto;
import ge.paso.Exam.entities.Server;
import ge.paso.Exam.users.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/server")
public class ServerController {

    private final ServerService serverService;
    private final AuthenticationManager authenticationManager;
    private String userName = "";

    public ServerController(ServerService serverService, AuthenticationManager authenticationManager) {
        this.serverService = serverService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/login")
    public ResponseEntity<String> logIn() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication auth = securityContext.getAuthentication();
        userName = ((User) auth.getPrincipal()).getUsername();
        String role = ((User) auth.getPrincipal()).getRole();
        String toShow = "You've logged in as - " + userName + ", Role - " + role;
        return new ResponseEntity<>("<h1>" + toShow + "</h1>", HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<String> createServer(@Valid @RequestBody ServerCreationDto serverCreationDto, BindingResult b) {
        serverService.createServer(serverCreationDto, userName);
        return new ResponseEntity<>("server with name - " + serverCreationDto.getName() + " created", HttpStatus.OK);
    }

    @GetMapping("/get")
    public ResponseEntity<List<Server>> getAllFreeServers() {
        return new ResponseEntity<>(serverService.getFreeServers(), HttpStatus.OK);
    }

    @GetMapping("/choose/{serverName}")
    public ResponseEntity<Server> chooseServer(@PathVariable String serverName) {
        return new ResponseEntity<>(serverService.chooseServer(userName,serverName), HttpStatus.OK);
    }

    @GetMapping("/release/{serverName}")
    public ResponseEntity<String> releaseServer(@PathVariable String serverName) {
        serverService.releaseServer(userName,serverName);
        System.out.println(userName);
        return new ResponseEntity<>("Server with name - " + serverName + " was released by user - " + userName, HttpStatus.OK);
    }

    @GetMapping("/deleteUser/{toDeleteUser}")
    public ResponseEntity<String> deleteUser(@PathVariable String toDeleteUser) {
        serverService.deleteUser(userName,toDeleteUser);
        return new ResponseEntity<>("username - " + toDeleteUser + " was deleted by super admin - " + userName, HttpStatus.OK);
    }

    @GetMapping("/changeRole/{secondUser}/{role}")
    public ResponseEntity<String> changeRole(@PathVariable String secondUser, @PathVariable String role) {
        serverService.changeRole(userName, secondUser, role);
        return new ResponseEntity<>("username - " + secondUser + "'s role was changed by super admin - " + userName, HttpStatus.OK);
    }

}
