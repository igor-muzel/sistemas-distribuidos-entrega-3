
package org.example.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.common.*;

import java.io.*;
import java.net.*;
import java.util.Scanner;

@JsonInclude(JsonInclude.Include.NON_NULL) // Ignorar campos nulos
public class EchoClient2 {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Digite o endereço do servidor:");
            String serverHost = scanner.nextLine();

            System.out.println("Digite a porta do servidor:");
            int porta = scanner.nextInt();
            scanner.nextLine();  // Consume the newline character
            System.out.println("IP: " + serverHost + "| Porta: " + porta);

            try (
                    Socket socket = new Socket(serverHost, porta);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                System.out.println("Tentando se conectar no servidor\nIP: " + serverHost + "| Porta: " + porta);

                while (true) {
                    System.out.println("Conectado!");
                    System.out.println("Escolha uma operação (1 - Cadastro, 2 - Login):");
                    int operacao = scanner.nextInt();
                    scanner.nextLine();  // Consume the newline character

                    User user = new User();
                    if (operacao == 1) {
                        user.setOperacao("cadastrarUsuario");

                        // Escolher tipo de usuário
                        System.out.println("Tipo de usuário (ADM/comum):");
                        String tipoUsuario = scanner.nextLine();
                        user.setTipoUsuario(tipoUsuario);

                        System.out.println("RA de 7 dígitos:");
                        user.setRa(scanner.nextLine());

                        System.out.println("Nome até 50 caracteres:");
                        user.setNome(scanner.nextLine());

                        System.out.println("Senha de 8 a 20 caracteres:");
                        user.setSenha(scanner.nextLine());


                    } else if (operacao == 2) {
                        user.setOperacao("login");

                        System.out.println("RA:");
                        user.setRa(scanner.nextLine());

                        System.out.println("Senha:");
                        user.setSenha(scanner.nextLine());

                        // Enviar o login para o servidor
                        out.println(JsonUtils.toJson(user));
                        String response = in.readLine();
                        Response responseObj = JsonUtils.fromJson(response, Response.class);

                        System.out.println("Resposta do servidor: " + response);

                        // Se login foi bem-sucedido
                        if (responseObj.getStatus() == 200 && operacao == 2) {
                            System.out.println("Você está logado!");
                            String token = responseObj.getToken(); // Pega o token do servidor

                            while (true) {
                                System.out.println("Escolha uma operação:");
                                System.out.println("1 - Continuar navegando");
                                System.out.println("2 - Solicitar dados do seu cadastro");
                                System.out.println("3 - Sair");
                                System.out.println("4 - Editar dados do cadastro");
                                System.out.println("5 - Excluir usuário");
                                System.out.println("6 - Criar categoria (somente ADM)");
                                System.out.println("7 - Listar categorias");
                                System.out.println("8 - Listar usuários (somente ADM)");
                                System.out.println("9 - Buscar usuário");
                                System.out.println("10 - Excluir categoria (somanete ADM)");
                                System.out.println("11 - Localizar categoria");
                                int escolha = scanner.nextInt();
                                scanner.nextLine(); // Consume the newline character

                                if (escolha == 1) {
                                    continue;
                                } else if (escolha == 2) {
                                    // Enviar pedido para obter os dados do usuário
                                    User requestUser = new User();
                                    requestUser.setOperacao("obterDadosCadastro");
                                    requestUser.setRa(token);  // RA usado para identificação do usuário
                                    requestUser.setToken(token);  // Token (RA) associado ao login

                                    out.println(JsonUtils.toJson(requestUser));
                                    String dadosResposta = in.readLine();
                                    Response dadosResponseObj = JsonUtils.fromJson(dadosResposta, Response.class);

                                    if (dadosResponseObj.getStatus() == 200) {
                                        System.out.println("Dados do Cadastro: " + JsonUtils.toJson(dadosResponseObj.getUsuario()));
                                    } else {
                                        System.out.println("Erro ao obter dados: " + dadosResponseObj.getMensagem());
                                    }
                                } else if (escolha == 3) {
                                    // Logout
                                    User logoutUser = new User();
                                    logoutUser.setOperacao("logout");
                                    logoutUser.setToken(token);

                                    out.println(JsonUtils.toJson(logoutUser));
                                    String logoutResponse = in.readLine();
                                    System.out.println("Resposta do servidor: " + logoutResponse);

                                    Response logoutResponseObj = JsonUtils.fromJson(logoutResponse, Response.class);
                                    if (logoutResponseObj.getStatus() == 200) {
                                        System.out.println("Logout realizado com sucesso.");
                                        break;
                                    } else {
                                        System.out.println("Erro ao realizar logout: " + logoutResponseObj.getMensagem());
                                    }
                                } else if (escolha == 4) {
                                    // Editar dados do cadastro
                                    User editUser = new User();
                                    editUser.setOperacao("editarUsuario");
                                    editUser.setToken(token);  // Token (RA) associado ao login

                                    System.out.println("Token atual: " + editUser.getToken());

                                    // Criar um objeto User para representar os dados do usuário
                                    User usuarioEditado = new User();
                                    usuarioEditado.setRa(token);  // O RA é o token

                                    System.out.println("Informe o RA:");
                                    String raInformado = scanner.nextLine();

                                    // Verificar se o RA informado é diferente do token atual
                                    if (!raInformado.equals(editUser.getToken())) {
                                        // Criar um objeto Response com status 401 e a mensagem de erro
                                        Response errorResponse = new Response();
                                        errorResponse.setStatus(401);
                                        errorResponse.setOperacao("editarUsuario");
                                        errorResponse.setMensagem("Os campos recebidos não são válidos.");

                                        // Enviar o JSON de erro para o servidor
                                        String jsonError = JsonUtils.toJson(errorResponse);
                                        out.println(jsonError);
                                        System.out.println("JSON enviado ao servidor: " + jsonError);

                                        // Exibir mensagem de erro para o usuário
                                        System.out.println("Erro: O RA informado não corresponde ao token atual.");
                                        continue; // Volta ao início do loop sem enviar a solicitação de edição
                                    }

                                    System.out.println("Novo nome (deixe em branco para manter o atual):");
                                    String novoNome = scanner.nextLine();
                                    if (!novoNome.isEmpty()) {
                                        usuarioEditado.setNome(novoNome);
                                    } else {
                                        usuarioEditado.setNome(user.getNome()); // Mantém o nome atual
                                    }

                                    System.out.println("Nova senha (deixe em branco para manter a atual):");
                                    String novaSenha = scanner.nextLine();
                                    if (!novaSenha.isEmpty()) {
                                        usuarioEditado.setSenha(novaSenha);
                                    } else {
                                        usuarioEditado.setSenha(user.getSenha()); // Mantém a senha atual
                                    }

                                    // Definir o objeto usuarioEditado no editUser
                                    editUser.setUsuario(usuarioEditado);

                                    // Enviar o JSON para o servidor
                                    String json = JsonUtils.toJson(editUser);
                                    System.out.println("JSON enviado: " + json); // Depuração
                                    out.println(json);

                                    // Receber a resposta do servidor
                                    String editResponse = in.readLine();
                                    Response editResponseObj = JsonUtils.fromJson(editResponse, Response.class);

                                    if (editResponseObj.getStatus() == 201) {
                                        System.out.println("Resposta do servidor: " + editResponse);
                                        System.out.println("Dados atualizados com sucesso!");
                                    } else {
                                        System.out.println("Erro ao atualizar dados: " + editResponseObj.getMensagem());
                                    }
                                } else if (escolha == 5) {
                                    // Excluir usuário
                                    User deleteUser = new User();
                                    deleteUser.setOperacao("excluirUsuario");
                                    deleteUser.setToken(token);  // Token (RA) associado ao login

                                    System.out.println("Digite o RA do usuário que deseja excluir:");
                                    String raExclusao = scanner.nextLine();
                                    deleteUser.setRa(raExclusao);

                                    // Enviar o JSON para o servidor
                                    String json = JsonUtils.toJson(deleteUser);
                                    System.out.println("JSON enviado: " + json); // Depuração
                                    out.println(json);

                                    // Receber a resposta do servidor
                                    String deleteResponse = in.readLine();
                                    Response deleteResponseObj = JsonUtils.fromJson(deleteResponse, Response.class);

                                    if (deleteResponseObj.getStatus() == 201) {
                                        System.out.println("Resposta do servidor: " + deleteResponse);
                                        System.out.println("Exclusão realizada com sucesso!");
                                    } else {
                                        System.out.println("Erro ao excluir usuário: " + deleteResponseObj.getMensagem());
                                    }
                                } else if (escolha == 6) {
                                    // Criar categoria (somente ADM)
                                    User categoriaRequest = new User();
                                    categoriaRequest.setOperacao("salvarCategoria");
                                    categoriaRequest.setToken(token);  // Token (RA) associado ao login

                                    System.out.println("Digite o nome da categoria:");
                                    String nomeCategoria = scanner.nextLine();

                                    // Criar o objeto Categoria
                                    Categoria categoria = new Categoria();
                                    categoria.setNome(nomeCategoria); // Apenas defina o nome, o ID será gerado pelo servidor

                                    // Definir a categoria no objeto categoriaRequest
                                    categoriaRequest.setCategoria(categoria);

                                    // Enviar o JSON para o servidor
                                    String json = JsonUtils.toJson(categoriaRequest);
                                    System.out.println("JSON enviado: " + json); // Depuração
                                    out.println(json);

                                    // Receber a resposta do servidor
                                    String categoriaResponse = in.readLine();
                                    Response categoriaResponseObj = JsonUtils.fromJson(categoriaResponse, Response.class);

                                    if (categoriaResponseObj.getStatus() == 201) {
                                        System.out.println("Resposta do servidor: " + categoriaResponse);
                                        System.out.println("Categoria salva com sucesso!");
                                    } else {
                                        System.out.println("Erro ao salvar categoria: " + categoriaResponseObj.getMensagem());
                                    }
                                }else if (escolha == 7) {
                                    // Listar categorias
                                    User listarCategoriasRequest = new User();
                                    listarCategoriasRequest.setOperacao("listarCategorias");
                                    listarCategoriasRequest.setToken(token);  // Token (RA) associado ao login

                                    // Enviar o JSON para o servidor
                                    String json = JsonUtils.toJson(listarCategoriasRequest);
                                    System.out.println("JSON enviado: " + json); // Depuração
                                    out.println(json);

                                    // Receber a resposta do servidor
                                    String listarCategoriasResponse = in.readLine();
                                    Response listarCategoriasResponseObj = JsonUtils.fromJson(listarCategoriasResponse, Response.class);

                                    if (listarCategoriasResponseObj.getStatus() == 201) {
                                        System.out.println("Resposta do servidor: " + listarCategoriasResponse);
                                        System.out.println("Categorias:");
                                        for (Categoria categoria : listarCategoriasResponseObj.getCategorias()) {
                                            System.out.println("ID: " + categoria.getId() + ", Nome: " + categoria.getNome());
                                        }
                                    } else {
                                        System.out.println("Erro ao listar categorias: " + listarCategoriasResponseObj.getMensagem());
                                    }
                                }else if (escolha == 8) {  // Nova opção para listar usuários
                                    User listarUsuariosRequest = new User();
                                    listarUsuariosRequest.setOperacao("listarUsuarios");
                                    listarUsuariosRequest.setToken(token);  // Token (RA) associado ao login

                                    // Enviar o JSON para o servidor
                                    String json = JsonUtils.toJson(listarUsuariosRequest);
                                    System.out.println("JSON enviado: " + json); // Depuração
                                    out.println(json);

                                    // Receber a resposta do servidor
                                    String listarUsuariosResponse = in.readLine();
                                    Response listarUsuariosResponseObj = JsonUtils.fromJson(listarUsuariosResponse, Response.class);

                                    if (listarUsuariosResponseObj.getStatus() == 201) {
                                        System.out.println("Resposta do servidor: " + listarUsuariosResponse);
                                        System.out.println("Usuários cadastrados:");
                                        for (User usuario : listarUsuariosResponseObj.getUsuarios()) {
                                            System.out.println("RA: " + usuario.getRa() + ", Nome: " + usuario.getNome() + ", Tipo: " + usuario.getTipoUsuario());
                                        }
                                    } else {
                                        System.out.println("Erro ao listar usuários: " + listarUsuariosResponseObj.getMensagem());
                                    }
                                }else if (escolha == 9) {  // Nova opção para localizar usuário
                                    System.out.println("Digite o RA do usuário que deseja buscar:");
                                    String raBuscado = scanner.nextLine();

                                    User localizarUsuarioRequest = new User();
                                    localizarUsuarioRequest.setOperacao("localizarUsuario");
                                    localizarUsuarioRequest.setToken(token);  // Token (RA) associado ao login
                                    localizarUsuarioRequest.setRa(raBuscado); // RA do usuário a ser buscado

                                    // Enviar o JSON para o servidor
                                    String json = JsonUtils.toJson(localizarUsuarioRequest);
                                    System.out.println("JSON enviado: " + json); // Depuração
                                    out.println(json);

                                    // Receber a resposta do servidor
                                    String localizarUsuarioResponse = in.readLine();
                                    Response localizarUsuarioResponseObj = JsonUtils.fromJson(localizarUsuarioResponse, Response.class);

                                    if (localizarUsuarioResponseObj.getStatus() == 201) {
                                        System.out.println("Resposta do servidor: " + localizarUsuarioResponse);
                                        System.out.println("Usuário encontrado:");
                                        User usuarioEncontrado = localizarUsuarioResponseObj.getUsuario();
                                        System.out.println("RA: " + usuarioEncontrado.getRa() +
                                                ", Nome: " + usuarioEncontrado.getNome() +
                                                ", Senha: " + usuarioEncontrado.getSenha()); // Exibe a senha (se necessário)
                                    } else {
                                        System.out.println("Erro ao localizar usuário: " + localizarUsuarioResponseObj.getMensagem());
                                    }
                                }else if (escolha == 10) {  // Nova opção para excluir categoria
                                    System.out.println("Digite o ID da categoria que deseja excluir:");
                                    int idCategoria = scanner.nextInt();
                                    scanner.nextLine(); // Consumir a nova linha

                                    User excluirCategoriaRequest = new User();
                                    excluirCategoriaRequest.setOperacao("excluirCategoria");
                                    excluirCategoriaRequest.setToken(token);  // Token (RA) associado ao login
                                    excluirCategoriaRequest.setId(idCategoria); // ID da categoria a ser excluída

                                    // Enviar o JSON para o servidor
                                    String json = JsonUtils.toJson(excluirCategoriaRequest);
                                    System.out.println("JSON enviado: " + json); // Depuração
                                    out.println(json);

                                    // Receber a resposta do servidor
                                    String excluirCategoriaResponse = in.readLine();
                                    Response excluirCategoriaResponseObj = JsonUtils.fromJson(excluirCategoriaResponse, Response.class);

                                    if (excluirCategoriaResponseObj.getStatus() == 201) {
                                        System.out.println("Resposta do servidor: " + excluirCategoriaResponse);
                                        System.out.println("Categoria excluída com sucesso.");
                                    } else {
                                        System.out.println("Erro ao excluir categoria: " + excluirCategoriaResponseObj.getMensagem());
                                    }
                                }// Dentro do while (true) após o login, adicione a opção 11 para localizar categoria
                                else if (escolha == 11) {  // Nova opção para localizar categoria
                                    System.out.println("Digite o ID da categoria que deseja buscar:");
                                    int idCategoria = scanner.nextInt();
                                    scanner.nextLine(); // Consumir a nova linha

                                    // Criar o JSON de requisição
                                    User localizarCategoriaRequest = new User();
                                    localizarCategoriaRequest.setOperacao("localizarCategoria");
                                    localizarCategoriaRequest.setToken(token);  // Token (RA) associado ao login
                                    localizarCategoriaRequest.setId(idCategoria); // ID da categoria a ser buscada

                                    // Enviar o JSON para o servidor
                                    String json = JsonUtils.toJson(localizarCategoriaRequest);
                                    System.out.println("JSON enviado: " + json); // Depuração
                                    out.println(json);

                                    // Receber a resposta do servidor
                                    String localizarCategoriaResponse = in.readLine();
                                    Response localizarCategoriaResponseObj = JsonUtils.fromJson(localizarCategoriaResponse, Response.class);

                                    if (localizarCategoriaResponseObj.getStatus() == 201) {
                                        System.out.println("Resposta do servidor: " + localizarCategoriaResponse);
                                        System.out.println("Categoria encontrada:");
                                        Categoria categoriaEncontrada = localizarCategoriaResponseObj.getCategoria();
                                        System.out.println("ID: " + categoriaEncontrada.getId() + ", Nome: " + categoriaEncontrada.getNome());
                                    } else {
                                        System.out.println("Resposta do servidor: "+localizarCategoriaResponse);
                                        System.out.println("Erro ao localizar categoria: " + localizarCategoriaResponseObj.getMensagem());
                                    }
                                } else {
                                    System.out.println("Operação inválida!");
                                }
                            }
                        }
                    }

                   //  Exibir o JSON gerado para o cadastro (sem o tipo de usuário)
                    System.out.println("JSON enviado: " + JsonUtils.toJson(user));

                    out.println(JsonUtils.toJson(user));
                    String response = in.readLine();
                    Response responseObj = JsonUtils.fromJson(response, Response.class);

                    System.out.println("Resposta do servidor: " + response);

                    // Checar se login foi realizado com sucesso
                    if (responseObj.getStatus() == 200 && operacao == 2) {
                        System.out.println("Você está logado!");
                        String token = responseObj.getToken(); // Pega o token do servidor

                        while (true) {
                            System.out.println("Se desejar sair, digite 3 ");
                            int logout = scanner.nextInt();
                            scanner.nextLine(); // Consume the newline character

                            if (logout == 3) { // logout
                                User logoutUser = new User();
                                logoutUser.setOperacao("logout");
                                logoutUser.setToken(token);

                                out.println(JsonUtils.toJson(logoutUser));
                                String logoutResponse = in.readLine();
                                System.out.println("JSON enviado: " + JsonUtils.toJson(logoutUser));
                                System.out.println("Resposta do servidor: " + logoutResponse);

                                Response logoutResponseObj = JsonUtils.fromJson(logoutResponse, Response.class);
                                if (logoutResponseObj.getStatus() == 200) {
                                    System.out.println("Logout realizado com sucesso.");
                                    break;
                                } else {
                                    System.out.println("Erro ao realizar logout: " + logoutResponseObj.getMensagem());
                                }
                            } else {
                                System.out.println("Operação inválida!");
                            }
                        }
                    }

                    System.out.println("Deseja continuar? (s/n):");
                    if ("n".equalsIgnoreCase(scanner.nextLine())) {
                        break;
                    }
                }
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Erro: Tempo limite excedido ao tentar conectar ao servidor!");
        } catch (ConnectException e) {
            System.err.println("Erro: Não foi possível conectar ao servidor. Verifique o endereço IP e a porta.");
        } catch (IOException e) {
            System.err.println("Erro: Problema de entrada/saída. " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
}