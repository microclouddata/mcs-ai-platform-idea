package com.mcs.auth.user;

import com.mcs.auth.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "users")
public class User extends BaseEntity {
    @Indexed(unique = true)
    private String email;
    private String passwordHash;
    private String name;
    private UserRole role = UserRole.USER;
}
