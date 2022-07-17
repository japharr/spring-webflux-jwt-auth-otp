package io.github.jelilio.jwtauthotp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.jwtauthotp.entity.auditing.AbstractAuditingEntity;
import io.github.jelilio.jwtauthotp.entity.enumeration.Role;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.*;
import java.util.stream.Collectors;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User extends AbstractAuditingEntity implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Getter
    @Column("id")
    private Long id;

    @Version
    @JsonIgnore
    @Getter @Setter
    private Long version;

    @Getter @Setter
    @Column("name")
    private String name;

    @Getter @Setter
    @Column("email")
    private String email;

    @Setter
    @Column("username")
    private String username;

    @Setter
    @JsonIgnore
    @Column("password")
    private String password;

    @Column("enabled")
    @Setter
    private boolean enabled;

    @Column("roles")
    private String roles;

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(roles == null) return Collections.emptyList();

        return Arrays.stream(roles.split(","))
            .map(authority -> new SimpleGrantedAuthority(authority.toLowerCase()))
            .collect(Collectors.toList());
    }

    public void setRoles(Set<Role> roles) {
        Set<String> _roles = roles.stream()
            .map(Enum::name)
            .collect(Collectors.toSet());

        this.roles = String.join(",", _roles);
    }

    public Set<Role> getRoles() {
        return Arrays.stream(roles.split(","))
            .map(Role::valueOf)
            .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return password;
    }

}