package ge.paso.Exam.Service;

import ge.paso.Exam.dto.ServerCreationDto;
import ge.paso.Exam.entities.Server;
import ge.paso.Exam.exceptions.ErrorEnum;
import ge.paso.Exam.exceptions.GeneralException;
import ge.paso.Exam.repositories.AppRepository;
import ge.paso.Exam.repositories.ServerRepository;
import ge.paso.Exam.users.User;
import ge.paso.Exam.users.UserRoles;
import ge.paso.Exam.validation.ValidateParams;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
@EnableScheduling
public class ServerServiceImpl implements ServerService{

    private final ServerRepository serverRepository;
    private final AppRepository appRepository;

    @Override
    public void createServer(ServerCreationDto serverCreationDto, String userName) {
        userValidation(userName, "ADMIN");
        Server s = ServerDtoToEntity(serverCreationDto);
             serverRepository.save(s);
    }

    @Override
    public List<Server> getFreeServers() {
        List<Server> freeServers = serverRepository.findByStatus("F").orElse(null);
        if (freeServers != null) {
            return freeServers;
        }
        throw new GeneralException(ErrorEnum.NO_FREE_SERVERS_FOUND);
    }

    @Override
    public Server chooseServer(String userName, String serverName) {
        userValidation(userName, "USER");
        Server s = serverRepository.findByName(serverName).orElse(null);
        User u = appRepository.findByUserName(userName).orElse(null);
        int userId = u.getId();
        if(s != null) {
            s.setStatus("U");
            s.setUserId(userId);
            appRepository.save(u);
        }
        return s;
    }

    @Override
    public void releaseServer(String userName, String serverName) {
        userValidation(userName, "USER");
        serverValidation(serverName, userName);
        Server s = serverRepository.findByName(serverName).orElse(null);
        if(s != null) {
            s.setUserId(null);
            s.setStatus("F");
            serverRepository.save(s);
        }
    }

    @Override
    public void deleteUser(String superAdminName, String toDeleteUser) {
        validateDeletion(superAdminName,toDeleteUser);
        User userToDelete = appRepository.findByUserName(toDeleteUser).orElse(null);
        List<Server> servers = serverRepository.findByUserId(userToDelete.getId()).orElse(null);
        for(Server s : servers) {
         s.setUserId(null);
         s.setStatus("F");
         serverRepository.save(s);
        }
        appRepository.delete(userToDelete);

    }

    private void validateDeletion(String superAdminName, String toDeleteUser) {
        User u = appRepository.findByUserName(superAdminName).orElse(null);
        User u2 = appRepository.findByUserName(toDeleteUser).orElse(null);
        if(u == null || u2 == null) {
            throw new GeneralException(ErrorEnum.USERNAME_NOT_EXISTS);
        }

        if(!u.getRole().equalsIgnoreCase("SUPER_ADMIN")) {
            throw new GeneralException(ErrorEnum.NO_ACCESS_TO_MODIFY_USER);
        }
    }

    @Override
    public void changeRole(String superAdminName, String secondUser, String role) {
        validateDeletion(superAdminName,secondUser);
        User u = appRepository.findByUserName(secondUser).orElse(null);
        roleChecker(role, u.getRole());
        if(u != null) {
            u.setRole(role.toUpperCase());
            appRepository.save(u);
        }
    }

    private Server ServerDtoToEntity(ServerCreationDto serverCreationDto) {
        return new Server(serverCreationDto.getName(),serverCreationDto.getCapacity(), serverCreationDto.getDeleteDate(), "F", null);
    }

    @Scheduled(fixedDelay = 86400000) // means per 24 hour
    private void checkServersValidity() {
        List<Server> servers = serverRepository.getAllServers().orElse(null);
        for(Server s : servers) {
            String date = s.getDeleteDate();
            if(!isValid(date)) {
                deleteServer(s);
            }
        }
    }

    private boolean isValid(String endDate) {
        ValidateParams.checkDate(endDate);
        return true;
    }

    private void deleteServer(Server s) {
        serverRepository.delete(s);
    }

    private void userValidation(String userName, String role) {
        User u = appRepository.findByUserName(userName).orElse(null);
        if(u == null) {
            throw new GeneralException(ErrorEnum.USERNAME_NOT_EXISTS);
        }
        if(!u.getRole().equalsIgnoreCase(role)) {
            throw new GeneralException(ErrorEnum.NO_ACCESS_TO_MODIFY_SERVER);
        }
    }

    private void serverValidation(String serverName, String userName) {
        User u = appRepository.findByUserName(userName).orElse(null);
        Server s = serverRepository.findByName(serverName).orElse(null);
        if(s == null) {
            throw new GeneralException(ErrorEnum.SERVER_DO_NOT_EXISTS);
        }
        if(s.getUserId() == null || s.getUserId() != u.getId()) {
            throw new GeneralException(ErrorEnum.SERVER_IS_OWNED_BY_OTHER);
        }
    }

    private void roleChecker(String role, String currRole) {
        if(!role.equalsIgnoreCase(UserRoles.USER.name()) && !role.equalsIgnoreCase(UserRoles.ADMIN.name()) && !role.equalsIgnoreCase(UserRoles.SUPER_ADMIN.name())) {
            throw new GeneralException(ErrorEnum.INVALID_ROLE);
        }
        else if(role.equalsIgnoreCase(currRole)) {
            throw new GeneralException(ErrorEnum.ROLE_CURRENTLY_USING);
        }
    }
}
