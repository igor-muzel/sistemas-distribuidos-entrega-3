package org.example.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.common.*;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL) // Ignora campos null

public class UserManager {
    private static final List<Categoria> categorias = new ArrayList<>(); // Lista de categorias
    public static final Map<String, User> registeredUsers = new HashMap<>(); // Map<RA, User>

    static Response handleUserRequest(String jsonInput) throws Exception {
        Response response = new Response();

        try {
            User user = JsonUtils.fromJson(jsonInput, User.class);

            switch (user.getOperacao()) {
                case "cadastrarUsuario":
                    return handleUserRegistration(user);

                case "login":
                    return handleUserLogin(user);

                case "logout":
                    return handleLogout(user);

                case "obterDadosCadastro":
                    return handleGetUserData(user);
                case "editarUsuario":
                    return handleEditUserData(user);
                case "excluirUsuario": // Nova operação
                    return handleDeleteUser(user);
                case "salvarCategoria": // Nova operação
                    return handleSaveCategoria(user);
                case "listarCategorias": // Nova operação
                    return handleListarCategorias(user);
                case "listarUsuarios":  // Nova operação para listar usuários
                    return handleListarUsuarios(user);
                case "localizarUsuario": // Nova operação
                    return handleLocalizarUsuario(user);
                case "excluirCategoria": // Nova operação
                    return handleExcluirCategoria(user);
                case "localizarCategoria":
                    return handleLocalizarCategoria(user);
                default:
                    throw new IllegalArgumentException("Operação inválida.");
            }
        } catch (Exception e) {
            response.setStatus(401);
            response.setOperacao("Erro");
            response.setMensagem("Não foi possível ler o json recebido.");
        }
        return response;
    }

    private static Response handleLocalizarCategoria(User user) throws Exception {
        Response response = new Response();

        // Verificar se o token (RA) foi enviado
        if (user.getToken() == null || user.getToken().isEmpty()) {
            response.setStatus(401);
            response.setOperacao("localizarCategoria");
            response.setMensagem("Token inválido ou ausente.");
            return response;
        }

        // Verificar se o ID da categoria foi enviado
        if (user.getId() <= 0) {
            response.setStatus(401);
            response.setOperacao("localizarCategoria");
            response.setMensagem("ID da categoria inválido ou ausente.");
            return response;
        }

        synchronized (registeredUsers) {
            // Verificar se o usuário que está fazendo a requisição existe
            User requester = registeredUsers.get(user.getToken());
            if (requester == null) {
                response.setStatus(401);
                response.setOperacao("localizarCategoria");
                response.setMensagem("Usuário que fez a requisição não encontrado.");
                return response;
            }
            boolean encontrada = false;
            // Buscar a categoria pelo ID
            Categoria categoriaEncontrada = null;
            for (Categoria categoria : categorias) {
                if (categoria.getId() == user.getId()) {
                    categoriaEncontrada = categoria;
                    encontrada = true;
                    break;
                }
            }

            // Verificar se a categoria foi encontrada
            if (categoriaEncontrada == null || !encontrada) {
                response.setStatus(401);
                response.setOperacao("localizarCategoria");
                response.setMensagem("Categoria não encontrada.");
                return response;
            }

            // Retornar os dados da categoria
            response.setStatus(201);
            response.setOperacao("localizarCategoria");
            response.setCategoria(categoriaEncontrada); // Retorna a categoria encontrada
        }
        System.out.println("Resposta do servidor (JSON): " + JsonUtils.toJson(response));

        return response;
    }

    private static Response handleExcluirCategoria(User user) throws Exception {
        Response response = new Response();

        // Verificar se o token (RA) foi enviado
        if (user.getToken() == null || user.getToken().isEmpty()) {
            response.setStatus(401);
            response.setOperacao("excluirCategoria");
            response.setMensagem("Token inválido ou ausente.");
            return response;
        }

        // Verificar se o ID da categoria foi enviado
        if (user.getId() <= 0) {
            response.setStatus(401);
            response.setOperacao("excluirCategoria");
            response.setMensagem("ID da categoria inválido ou ausente.");
            return response;
        }

        synchronized (registeredUsers) {
            // Verificar se o usuário que está fazendo a requisição existe
            User requester = registeredUsers.get(user.getToken());
            if (requester == null) {
                response.setStatus(401);
                response.setOperacao("excluirCategoria");
                response.setMensagem("Usuário que fez a requisição não encontrado.");
                return response;
            }

            // Verificar se o requester é um administrador
            boolean isAdmin = "ADM".equalsIgnoreCase(requester.getTipoUsuario());
            if (!isAdmin) {
                response.setStatus(403); // Acesso não autorizado
                response.setOperacao("excluirCategoria");
                response.setMensagem("Acesso não autorizado. Somente administradores podem excluir categorias.");
                return response;
            }

            // Buscar a categoria pelo ID
            Categoria categoriaParaExcluir = null;
            for (Categoria categoria : categorias) {
                if (categoria.getId() == user.getId()) {
                    categoriaParaExcluir = categoria;
                    break;
                }
            }

            // Verificar se a categoria foi encontrada
            if (categoriaParaExcluir == null) {
                response.setStatus(404);
                response.setOperacao("excluirCategoria");
                response.setMensagem("Categoria com o ID especificado não encontrada.");
                return response;
            }

            // Excluir a categoria
            categorias.remove(categoriaParaExcluir);

            response.setStatus(201);
            response.setOperacao("excluirCategoria");
            response.setMensagem("Exclusão realizada com sucesso.");
        }
        System.out.println("Resposta do servidor (JSON): " + JsonUtils.toJson(response));

        return response;
    }

    private static Response handleLocalizarUsuario(User user) throws Exception {
        Response response = new Response();

        // Verificar se o token (RA) foi enviado
        if (user.getToken() == null || user.getToken().isEmpty()) {
            response.setStatus(401);
            response.setOperacao("localizarUsuario");
            response.setMensagem("Token inválido ou ausente.");
            return response;
        }

        // Verificar se o RA a ser buscado foi enviado
        if (user.getRa() == null || user.getRa().isEmpty()) {
            response.setStatus(401);
            response.setOperacao("localizarUsuario");
            response.setMensagem("RA inválido ou ausente.");
            return response;
        }

        synchronized (registeredUsers) {
            // Verificar se o usuário que está fazendo a requisição existe
            User requester = registeredUsers.get(user.getToken());
            if (requester == null) {
                response.setStatus(401);
                response.setOperacao("localizarUsuario");
                response.setMensagem("Usuário que fez a requisição não encontrado.");
                return response;
            }

            // Verificar se o RA a ser buscado existe
            User usuarioBuscado = registeredUsers.get(user.getRa());
            if (usuarioBuscado == null) {
                response.setStatus(401);
                response.setOperacao("localizarUsuario");
                response.setMensagem("Usuário com o RA especificado não encontrado.");
                return response;
            }

            // Verificar se o requester é um administrador ou está tentando buscar a si mesmo
            boolean isAdmin = "ADM".equalsIgnoreCase(requester.getTipoUsuario());
            boolean isSelf = user.getToken().equals(user.getRa());

            if (!isAdmin && !isSelf) {
                response.setStatus(401); // Acesso não autorizado
                response.setOperacao("localizarUsuario");
                response.setMensagem("Usuário não encontrado");
                return response;
            }

            // Criar um novo objeto User com apenas os campos necessários
            User usuarioFormatado = new User();
            usuarioFormatado.setRa(usuarioBuscado.getRa());
            usuarioFormatado.setSenha(usuarioBuscado.getSenha());
            usuarioFormatado.setNome(usuarioBuscado.getNome());

            // Retornar os dados do usuário buscado
            response.setStatus(201);
            response.setOperacao("localizarUsuario");
            response.setUsuario(usuarioFormatado); // Retorna os dados do usuário formatado
        }
        System.out.println("Resposta do servidor (JSON): " + JsonUtils.toJson(response));

        return response;
    }

    private static Response handleListarUsuarios(User user) throws Exception {
        Response response = new Response();

        // Verificar se o token (RA) foi enviado
        if (user.getToken() == null || user.getToken().isEmpty()) {
            response.setStatus(401);
            response.setOperacao("listarUsuarios");
            response.setMensagem("Token inválido ou ausente.");
            return response;
        }

        synchronized (registeredUsers) {
            // Verificar se o usuário que está fazendo a requisição existe
            User requester = registeredUsers.get(user.getToken());
            if (requester == null) {
                response.setStatus(401);
                response.setOperacao("listarUsuarios");
                response.setMensagem("Usuário que fez a requisição não encontrado.");
                return response;
            }

            // Verificar se o requester é um administrador
            boolean isAdmin = "ADM".equalsIgnoreCase(requester.getTipoUsuario());
            if (!isAdmin) {
                response.setStatus(401); // Acesso não autorizado
                response.setOperacao("listarUsuarios");
                response.setMensagem("Acesso não autorizado. Somente administradores podem listar usuários.");
                return response;
            }

            // Retornar a lista de todos os usuários cadastrados no formato especificado
            List<User> allUsers = new ArrayList<>(registeredUsers.values());

            // Criar uma lista de usuários no formato desejado (sem o campo id)
            List<User> usuariosFormatados = new ArrayList<>();
            for (User u : allUsers) {
                User usuarioFormatado = new User();
                usuarioFormatado.setRa(u.getRa());
                usuarioFormatado.setSenha(u.getSenha()); // Inclui a senha (se necessário)
                usuarioFormatado.setNome(u.getNome());
                usuariosFormatados.add(usuarioFormatado);
            }

            response.setStatus(201);
            response.setOperacao("listarUsuarios");
            response.setUsuarios(usuariosFormatados);  // Retorna a lista de usuários no formato especificado
        }
        System.out.println("Resposta do servidor (JSON): " + JsonUtils.toJson(response));

        return response;
    }


    private static Response handleListarCategorias(User user) throws Exception {
        Response response = new Response();

        // Verificar se o token (RA) foi enviado
        if (user.getToken() == null || user.getToken().isEmpty()) {
            response.setStatus(401);
            response.setOperacao("listarCategorias");
            response.setMensagem("Token inválido ou ausente.");
            return response;
        }

        synchronized (registeredUsers) {
            // Verificar se o usuário que está fazendo a requisição existe
            User requester = registeredUsers.get(user.getToken());
            if (requester == null) {
                response.setStatus(401);
                response.setOperacao("listarCategorias");
                response.setMensagem("Usuário que fez a requisição não encontrado.");
                return response;
            }

            // Retornar a lista de categorias
            response.setStatus(201);
            response.setOperacao("listarCategorias");
            response.setCategorias(categorias);  // Retorna a lista de categorias
        }
        System.out.println("Resposta do servidor (JSON): " + JsonUtils.toJson(response));

        return response;
    }

    private static Response handleSaveCategoria(User user) throws Exception {
        Response response = new Response();

        // Verificar se o token (RA) foi enviado
        if (user.getToken() == null || user.getToken().isEmpty()) {
            response.setStatus(401);
            response.setOperacao("salvarCategoria");
            response.setMensagem("Token inválido ou ausente.");
            return response;
        }

        // Verificar se a categoria foi enviada
        if (user.getCategoria() == null || user.getCategoria().getNome() == null || user.getCategoria().getNome().isEmpty()) {
            response.setStatus(401);
            response.setOperacao("salvarCategoria");
            response.setMensagem("Dados da categoria inválidos ou ausentes.");
            return response;
        }

        synchronized (registeredUsers) {
            // Verificar se o usuário que está fazendo a requisição existe
            User requester = registeredUsers.get(user.getToken());
            if (requester == null) {
                response.setStatus(401);
                response.setOperacao("salvarCategoria");
                response.setMensagem("Usuário que fez a requisição não encontrado.");
                return response;
            }

            // Verificar se o requester é um administrador
            System.out.println("Tipo de usuário do requester: " + requester.getTipoUsuario()); // Depuração
            boolean isAdmin = "ADM".equalsIgnoreCase(requester.getTipoUsuario());
            System.out.println("É administrador? " + isAdmin); // Depuração

            if (!isAdmin) {
                response.setStatus(401); // Acesso não autorizado
                response.setOperacao("salvarCategoria");
                response.setMensagem("Acesso não autorizado. Somente administradores podem criar categorias.");
                return response;
            }

            // Salvar a categoria
            Categoria novaCategoria = user.getCategoria();
            novaCategoria.setId(generateCategoriaId()); // Gerar um ID único para a categoria
            categorias.add(novaCategoria); // Adicionar a categoria à lista

            response.setStatus(201);
            response.setOperacao("salvarCategoria");
            response.setMensagem("Categoria salva com sucesso.");
        }
        System.out.println("Resposta do servidor (JSON): " + JsonUtils.toJson(response));

        return response;
    }

    private static int generateCategoriaId() {
        if (categorias.isEmpty()) {
            return 1; // Primeiro ID
        } else {
            // Retorna o último ID + 1
            return categorias.get(categorias.size() - 1).getId() + 1;
        }
    }

    // Método para gerar um ID único para a categoria (exemplo simples)

    private static Response handleDeleteUser(User user) throws Exception {
        Response response = new Response();

        // Verificar se o token (RA) foi enviado
        if (user.getToken() == null || user.getToken().isEmpty()) {
            response.setStatus(401);
            response.setOperacao("excluirUsuario");
            response.setMensagem("Token inválido ou ausente.");
            return response;
        }

        // Verificar se o RA a ser excluído foi enviado
        if (user.getRa() == null || user.getRa().isEmpty()) {
            response.setStatus(401);
            response.setOperacao("excluirUsuario");
            response.setMensagem("RA inválido ou ausente.");
            return response;
        }

        synchronized (registeredUsers) {
            // Verificar se o usuário que está fazendo a requisição existe
            User requester = registeredUsers.get(user.getToken());
            if (requester == null) {
                response.setStatus(401);
                response.setOperacao("excluirUsuario");
                response.setMensagem("Usuário que fez a requisição não encontrado.");
                return response;
            }

            // Verificar se o RA a ser excluído existe
            User userToDelete = registeredUsers.get(user.getRa());
            if (userToDelete == null) {
                response.setStatus(401);
                response.setOperacao("excluirUsuario");
                response.setMensagem("Usuário a ser excluído não encontrado.");
                return response;
            }

            // Verificar se o requester é um administrador ou está tentando excluir a si mesmo
            boolean isAdmin = "ADM".equalsIgnoreCase(requester.getTipoUsuario());
            boolean isSelf = user.getToken().equals(user.getRa());

            if (!isAdmin && !isSelf) {
                response.setStatus(401); // Acesso não autorizado
                response.setOperacao("excluirUsuario");
                response.setMensagem("Acesso não autorizado.");
                return response;
            }

            // Excluir o usuário
            registeredUsers.remove(user.getRa());

            response.setStatus(201);
            response.setOperacao("excluirUsuario");
            response.setMensagem("Exclusão realizada com sucesso.");
        }
        System.out.println("Resposta do servidor (JSON): " + JsonUtils.toJson(response));

        return response;
    }

    private static Response handleEditUserData(User user) throws Exception {
        Response response = new Response();

        // Verificar se o token (RA) foi enviado
        if (user.getToken() == null || user.getToken().isEmpty()) {
            response.setStatus(401);
            response.setOperacao("editarUsuario");
            response.setMensagem("Token inválido ou ausente.");
            System.out.println("Resposta do servidor (JSON): " + JsonUtils.toJson(response));

            return response;
        }

        synchronized (registeredUsers) {
            // Verificar se o RA (token) enviado corresponde ao RA do usuário
            User storedUser = registeredUsers.get(user.getToken());
            if (storedUser == null) {
                response.setStatus(401);
                response.setOperacao("editarUsuario");
                response.setMensagem("Usuário não encontrado.");
                System.out.println("Resposta do servidor (JSON): " + JsonUtils.toJson(response));

                return response;
            }

            // Obter os dados do usuário enviados no JSON
            User usuarioEditado = user.getUsuario();

            // Verificar se o objeto usuarioEditado foi recebido corretamente
            if (usuarioEditado == null) {
                response.setStatus(401);
                response.setOperacao("editarUsuario");
                response.setMensagem("Dados do usuário não foram enviados corretamente.");
                System.out.println("Resposta do servidor (JSON): " + JsonUtils.toJson(response));

                return response;
            }


            // Atualizar os dados do usuário
            if (usuarioEditado.getNome() != null && !usuarioEditado.getNome().isEmpty()) {
                storedUser.setNome(usuarioEditado.getNome());
            }
            if (usuarioEditado.getSenha() != null && !usuarioEditado.getSenha().isEmpty()) {
                storedUser.setSenha(usuarioEditado.getSenha());
            }

            // Atualizar o usuário no mapa
            registeredUsers.put(user.getToken(), storedUser);

            response.setStatus(201);
            response.setOperacao("editarUsuario");
            response.setMensagem("Dados atualizados com sucesso.");
        }
        System.out.println("Resposta do servidor (JSON): " + JsonUtils.toJson(response));

        return response;
    }

    private static Response handleGetUserData(User user) throws Exception {
        Response response = new Response();

        // Verificar se o token (RA) foi enviado
        if (user.getToken() == null || user.getToken().isEmpty()) {
            response.setStatus(401);
            response.setOperacao("obterDadosCadastro");
            response.setMensagem("Token inválido ou ausente.");
            return response;
        }

        synchronized (registeredUsers) {
            // Verificar se o RA (token) enviado corresponde ao RA do usuário
            User storedUser = registeredUsers.get(user.getRa());
            if (storedUser == null || !storedUser.getToken().equals(user.getToken())) {
                response.setStatus(401);
                response.setOperacao("obterDadosCadastro");
                response.setMensagem("Usuário não encontrado ou token inválido.");
                return response;
            }

            // Retornar os dados do usuário
            response.setStatus(200);
            response.setOperacao("obterDadosCadastro");
            response.setUsuario(storedUser);  // Retorna os dados do usuário
        }
        System.out.println("Resposta do servidor (JSON): " + JsonUtils.toJson(response));

        return response;
    }

    private static Response handleUserRegistration(User user) throws Exception {
        Response response = new Response();

        // Validação dos dados de cadastro
        String validationMessage = validateUser(user);
        if (!validationMessage.isEmpty()) {
            response.setStatus(401);
            response.setOperacao("cadastrarUsuario");
            response.setMensagem(validationMessage);
            return response;
        }

        synchronized (registeredUsers) {
            // Verifica se o RA já existe
            if (registeredUsers.containsKey(user.getRa())) {
                response.setStatus(401);
                response.setOperacao("cadastrarUsuario");
                response.setMensagem("Não foi possível cadastrar pois o usuario informado já existe.");
                return response;
            }

            // Define o tipo de usuário como "comum" por padrão, caso não seja enviado
            if (user.getTipoUsuario() == null || user.getTipoUsuario().isEmpty()) {
                user.setTipoUsuario("comum");
            }

            // Cadastro realizado com sucesso
            registeredUsers.put(user.getRa(), user);
        }

        response.setStatus(201); // Status de criado
        response.setOperacao("cadastrarUsuario");
        response.setMensagem("Cadastro realizado com sucesso.");
        System.out.println("Resposta do servidor (JSON): " + JsonUtils.toJson(response));

        return response;
    }

    private static Response handleUserLogin(User user) throws Exception {
        Response response = new Response();

        String validationMessage = validateLogin(user);
        if (validationMessage.isEmpty()) {
            synchronized (registeredUsers) {
                User storedUser = registeredUsers.get(user.getRa());
                if (storedUser == null || !storedUser.getSenha().equals(user.getSenha())) {
                    response.setStatus(401);
                    response.setOperacao("login");
                    response.setMensagem("Credenciais incorretas.");
                    return response;
                }

                // Usando o RA como token
                String token = user.getRa();  // O RA é o token

                // Armazenar o token
                storedUser.setToken(token);  // Associa o token (RA) ao usuário
                registeredUsers.put(user.getRa(), storedUser);

                response.setStatus(200);
                response.setToken(token);  // Retorna o token (RA)
            }
        } else {
            response.setStatus(401);
            response.setOperacao("login");
            response.setMensagem(validationMessage);
        }
        System.out.println("Resposta do servidor (JSON): " + JsonUtils.toJson(response));

        return response;
    }



    private static String validateUser(User user) {
        if (user.getRa().length() == 0 || String.valueOf(user.getRa()).length() != 7) {
            return "RA inválido. Deve conter exatamente 7 números.";
        }

        if (user.getSenha() == null || user.getSenha().length() < 8 || user.getSenha().length() > 20 ||
                !user.getSenha().matches("[a-zA-Z]+")) {
            return "Senha inválida. Deve conter entre 8 e 20 caracteres, apenas letras sem acentuação.";
        }

        if (user.getNome() == null || user.getNome().length() > 50 || !user.getNome().matches("[A-Z ]+")) {
            return "Nome inválido. Deve conter no máximo 50 caracteres, apenas letras maiúsculas e espaços.";
        }

        return "";
    }

    private static String validateLogin(User user) {
        if (user.getRa().length() == 0 || String.valueOf(user.getRa()).length() != 7) {
            return "RA inválido. Deve conter exatamente 7 números.";
        }

        if (user.getSenha() == null || user.getSenha().length() < 8 || user.getSenha().length() > 20 ||
                !user.getSenha().matches("[a-zA-Z]+")) {
            return "Senha inválida. Deve conter entre 8 e 20 caracteres, apenas letras sem acentuação.";
        }

        return "";
    }

    private static Response handleLogout(User user) throws Exception {
        Response response = new Response();

        // Verificar se o token está presente
        if (user.getToken() == null || user.getToken().isEmpty()) {
            response.setStatus(401);
            response.setOperacao("logout");
            response.setMensagem("Token inválido ou ausente.");
            return response;
        }

        // Logout bem-sucedido
        response.setStatus(200);
        System.out.println("Resposta do servidor (JSON): " + JsonUtils.toJson(response));

        return response;
    }
}
