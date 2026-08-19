package com.example.usermanagement.specification;

import com.example.usermanagement.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    public static Specification<User> hasUsernameLike(String keyword) {
        return (root, query, cb) ->
                keyword == null || keyword.isBlank()
                    ? cb.conjunction()
                    : cb.like(cb.lower(root.get("username")), "%" + keyword.toLowerCase() + "%");
    }
}
