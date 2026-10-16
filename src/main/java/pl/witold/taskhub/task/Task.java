package pl.witold.taskhub.task;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.witold.taskhub.tag.Tag;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.NEW;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_tags",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_task_tags_task_id_tag_id",
                            columnNames = {"task_id", "tag_id"}
                    )
            }
    )
    private Set<Tag> tags = new HashSet<>();

    public Task(String name, String description, TaskPriority priority) {
        this.name = name;
        this.description = description;
        this.priority = priority == null ? TaskPriority.MEDIUM : priority;
        this.status = TaskStatus.NEW;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void update(String name, String description, TaskPriority priority, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.status = status;
    }

    public void updateName(String name) {
        if (name != null) {
            this.name = name;
        }
    }

    public void updateDescription(String description) {
        if (description != null) {
            this.description = description;
        }
    }

    public void updatePriority(TaskPriority priority) {
        if (priority != null) {
            this.priority = priority;
        }
    }

    public void updateStatus(TaskStatus status) {
        if (status != null) {
            this.status = status;
        }
    }

    public void replaceTags(Set<Tag> tags) {
        this.tags.clear();

        if (tags != null) {
            this.tags.addAll(tags);
        }
    }
}