package pl.witold.taskhub.task;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.witold.taskhub.user.Role;
import pl.witold.taskhub.user.User;

@Service
public class TaskAuthorizationService {

    public boolean canView(Task task, User user) {
        if (!sameTenant(task, user)) {
            return false;
        }

        return user.getRole() == Role.ADMIN
                || isCreator(task, user)
                || isAssignee(task, user);
    }

    public boolean canEdit(Task task, User user) {
        return canView(task, user);
    }

    public boolean canDelete(Task task, User user) {
        return sameTenant(task, user)
                && (user.getRole() == Role.ADMIN
                || isCreator(task, user));
    }

    public boolean canAssign(Task task, User user) {
        return sameTenant(task, user)
                && user.getRole() == Role.ADMIN;
    }

    public void requireView(Task task, User user) {
        if (!canView(task, user)) {
            throw new TaskNotFoundException(task.getId());
        }
    }

    public void requireEdit(Task task, User user) {
        requireView(task, user);

        if (!canEdit(task, user)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN
            );
        }
    }

    public void requireDelete(Task task, User user) {
        requireView(task, user);

        if (!canDelete(task, user)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN
            );
        }
    }

    private boolean sameTenant(Task task, User user) {
        return task.getTenant().getId()
                .equals(user.getTenant().getId());
    }

    private boolean isCreator(Task task, User user) {
        return task.getCreatedBy().getId()
                .equals(user.getId());
    }

    private boolean isAssignee(Task task, User user) {
        return task.getAssignedTo() != null
                && task.getAssignedTo().getId()
                .equals(user.getId());
    }
}