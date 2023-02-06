/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.model;

import org.springframework.data.jpa.domain.Specification;

/**
 *
 * @author Admin
 */
public class UserSpecs {

    public static Specification<User> searchByKeyword(String keyword) {
        if (keyword != null) {
            keyword = "%" + keyword.toUpperCase().trim().replace("_", "\\_").replace("%", "\\%") + "%";
        }
        System.out.println("========> keyword: " + keyword);
        final String finalKeyword = keyword;
        return (root, query, builder) -> builder.or(
                builder.like(builder.upper(root.get("fullName")), finalKeyword),
                builder.like(builder.upper(root.get("email")), finalKeyword),
                builder.like(builder.upper(root.get("mobile")), finalKeyword),
                builder.like(builder.upper(root.get("userName")), finalKeyword)
        );
    }

    public static Specification<User> searchByStatusAndKeyword(Integer status, String keyword) {
        if (keyword != null) {
            keyword = "%" + keyword.toUpperCase().trim().replace("_", "\\_").replace("%", "\\%") + "%";
        }
        final String finalKeyword = keyword;
        return (root, query, builder) -> builder.and(
                builder.equal(root.get("status"), status),
                builder.or(
                        builder.like(builder.upper(root.get("fullName")), finalKeyword),
                        builder.like(builder.upper(root.get("email")), finalKeyword),
                        builder.like(builder.upper(root.get("mobile")), finalKeyword),
                        builder.like(builder.upper(root.get("userName")), finalKeyword)
                )
        );
    }
}
