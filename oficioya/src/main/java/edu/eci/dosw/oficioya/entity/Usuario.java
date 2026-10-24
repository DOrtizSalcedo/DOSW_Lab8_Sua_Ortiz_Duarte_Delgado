package edu.eci.dosw.oficioya.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @NotBlank
    @Column(unique = true)
    private String correo;

    @NotBlank
    @Column(nullable = false)
    private String telefono;

    @NotBlank
    @Column(nullable = false)
    private String password_hash;

    @NotBlank
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @NotBlank
    private String oficio_principal;

    @NotBlank
    private String zona;

    @NotBlank
    private LocalDate fecha_creacion;

    public Usuario() {
    }

    public Usuario(String nombre, String correo, String telefono,
                   String password_hash, Rol rol, String oficio_principal, String zona) {
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.password_hash = password_hash;
        this.rol = rol;
        this.oficio_principal = oficio_principal;
        this.zona = zona;
        this.fecha_creacion = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getOficio_principal() {
        return oficio_principal;
    }

    public void setOficio_principal(String oficio_principal) {
        this.oficio_principal = oficio_principal;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public LocalDate getFecha_creacion() {
        return fecha_creacion;
    }

    public void setFecha_creacion(LocalDate fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }
}
