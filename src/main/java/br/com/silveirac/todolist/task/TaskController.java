package br.com.silveirac.todolist.task;

import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.silveirac.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;
    
    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        taskModel.setUserId((UUID) request.getAttribute("userId"));

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início/término da tarefa deve ser maior ou igual à data de hoje.");

        if (taskModel.getStartAt().isAfter(taskModel.getEndAt()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início deve ser anterior à data de término da terefa");

        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        var userId = request.getAttribute("userId");
        var tasks = this.taskRepository.findByUserId((UUID) userId);
        return tasks;
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id,  HttpServletRequest request) {
        var task = this.taskRepository.findById(id).orElse(null);

        if (task == null)
            return ResponseEntity.badRequest().body("Tarefa não encontrada");
        
        if (!task.getUserId().equals(request.getAttribute("userId")))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário não possui permissão para alterar essa tarefa.");

        Utils.copyNonNullProperties(taskModel, task);
        return ResponseEntity.ok().body(this.taskRepository.save(task));
    }
}
