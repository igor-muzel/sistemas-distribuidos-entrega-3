package org.example.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) // Ignorar campos nulos
public class Categoria {
    private int id; // ID da categoria (não é mais estático)
    private String nome; // Nome da categoria

    // Construtor padrão
    public Categoria() {
        // O ID será definido pelo servidor ao salvar a categoria
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return "Categoria{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                '}';
    }
}