package pl.witold.taskhub.tag;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TagSeeder implements CommandLineRunner {

    private final TagRepository tagRepository;

    public TagSeeder(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public void run(String... args) {
        List<Tag> tags = List.of(
                new Tag("FRONTEND", "Frontend", "#3b82f6", 10),
                new Tag("BACKEND", "Backend", "#22c55e", 20),
                new Tag("DEVOPS", "DevOps", "#f97316", 30),
                new Tag("BUG", "Bug", "#ef4444", 40),
                new Tag("FEATURE", "Feature", "#a855f7", 50),
                new Tag("REFACTOR", "Refactor", "#6b7280", 60)
        );

        for (Tag tag : tags) {
            if (!tagRepository.existsByCode(tag.getCode())) {
                tagRepository.save(tag);
            }
        }
    }
}