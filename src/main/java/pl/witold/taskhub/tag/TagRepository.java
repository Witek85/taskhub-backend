package pl.witold.taskhub.tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByCodeIn(Collection<String> codes);

    Optional<Tag> findByCode(String code);

    boolean existsByCode(String code);

    List<Tag> findAllByActiveTrueOrderBySortOrderAscLabelAsc();
}