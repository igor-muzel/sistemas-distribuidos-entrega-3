package org.example.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.common.Categoria;


@JsonInclude(JsonInclude.Include.NON_DEFAULT) // Ignora campos com valores padrão (0 para int)

public class User {
    private String operacao; // Operação: cadastrarUsuario, login, editarUsuario, etc.
    private String ra; // Registro acadêmico (RA)
    private String senha; // Senha do usuário
    private String nome; // Nome do usuário
    private String token; // Token gerado para login
    private String tipoUsuario; // Tipo de usuário: "ADM" ou "comum"
    private User usuario; // Usuário para edição
    private Categoria categoria; // Categoria para criação
    private int id; // ID da categoria para exclusão

    // Getters e Setters
    public String getOperacao() {
        return operacao;
    }

    public void setOperacao(String operacao) {
        this.operacao = operacao;
    }

    public String getRa() {
        return ra;
    }

    public void setRa(String ra) {
        this.ra = ra;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}