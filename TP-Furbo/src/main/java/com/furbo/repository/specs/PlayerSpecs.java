package com.furbo.repository.specs;

import com.furbo.model.Player;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class PlayerSpecs {
    private PlayerSpecs() {}

    public static Specification<Player> withFilters(String league, String team, String position) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (league != null)
                predicates.add(cb.equal(root.get("league"), league));
            if (team != null)
                predicates.add(cb.like(cb.lower(root.get("team")),
                        "%" + escapeLike(team.toLowerCase()) + "%", '\\'));
            if (position != null)
                predicates.add(cb.equal(root.get("position"), position));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
