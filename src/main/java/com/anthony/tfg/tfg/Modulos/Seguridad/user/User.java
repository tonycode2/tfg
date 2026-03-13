package com.anthony.tfg.tfg.Modulos.Seguridad.user;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.anthony.tfg.tfg.Entidades.Empleados;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "users", uniqueConstraints = { @UniqueConstraint(columnNames = { "username" }) })
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false)
    String username;
    String password;
    @Enumerated(EnumType.STRING)
    Role role;
    
    @Column(name = "password_change_required", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    Boolean passwordChangeRequired = false;
    
    @OneToOne(mappedBy = "usuario")
    Empleados empleado;

    /** 
     * @return Collection<? extends GrantedAuthority>
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security's `hasRole`/`hasAnyRole` checks for authorities with the
        // `ROLE_` prefix, so expose authorities using that convention.
        String roleName = role != null ? role.name() : "";
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    /** 
     * @return String
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    /** 
     * @return String
     */
    @Override
    public String getUsername() {
        return this.username;
    }

    /** 
     * @return boolean
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** 
     * @return boolean
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** 
     * @return boolean
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** 
     * @return boolean
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
