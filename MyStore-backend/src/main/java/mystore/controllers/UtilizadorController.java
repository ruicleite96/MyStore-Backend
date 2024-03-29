package mystore.controllers;

import mystore.models.Cliente;
import mystore.models.Funcionario;
import mystore.models.Utilizador;
import mystore.models.enums.RoleUtilizador;
import mystore.services.ClienteService;
import mystore.services.FuncionarioService;
import mystore.services.UtilizadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static mystore.models.enums.RoleUtilizador.FUNCIONARIO;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/utilizadores")
public class UtilizadorController {

    @Autowired
    private UtilizadorService utilizadorService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private FuncionarioService funcionarioService;


    @RequestMapping(path = "/signin", method = POST)
    public Utilizador signin(@RequestBody Map<String, String> credentials, HttpServletResponse response) {
        if (!credentials.containsKey("email") || !credentials.containsKey("password")) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }
        String email = credentials.get("email");
        String password = credentials.get("password");

        return utilizadorService.signin(email, password)
                .map(utilizador -> {
                    try {
                        response.setHeader("Access-Token", utilizadorService.tokenFor(utilizador));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return utilizador;
                })
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Credenciais inválidas"));
    }

    @RequestMapping(path = "/signup", method = POST)
    public void signup(@RequestBody Map<String, String> body) {
        if (!body.containsKey("email") || !body.containsKey("password") ||
                !body.containsKey("nome") || !body.containsKey("role")) {
            throw new IllegalArgumentException("Dados inválidos");
        }

        String email = body.get("email");
        String password = body.get("password");
        String nome = body.get("nome");
        RoleUtilizador role = RoleUtilizador.valueOf(body.get("role"));

        int numero = 0;
        if (role == FUNCIONARIO) {
            if (!body.containsKey("numero")) {
                throw new IllegalArgumentException("Dados inválidos");
            }
            numero = Integer.valueOf(body.get("numero"));
        }

        utilizadorService.signup(email, password, nome, role, numero);
    }

    @RequestMapping(path = "/editarDados", method = PUT)
    public Utilizador editarDados(@RequestAttribute long uid, @RequestBody Map<String, String> body) {
        return utilizadorService.atualizarDados(uid, body).orElseThrow(() -> new EntityNotFoundException("Utilizador não existe"));
    }

    @RequestMapping(path = "/dados", method = GET)
    public Utilizador getDados(@RequestAttribute long uid) {
        return utilizadorService.get(uid).orElseThrow(() -> new EntityNotFoundException("Utilizador não existe"));
    }

    @RequestMapping(path = "/alterarPassword", method = PUT)
    public void alterarPassword(@RequestAttribute long uid, @RequestBody Map<String, String> body) {
        if (!body.containsKey("oldPassword") || !body.containsKey("newPassword")) {
            throw new IllegalArgumentException("Dados inválidos");
        }
        boolean r = utilizadorService.alterarPassword(uid, body.get("oldPassword"), body.get("newPassword"));
        if (!r) {
            throw new AuthenticationCredentialsNotFoundException("Credenciais inválidas");
        }
    }

    @RequestMapping(path = "clientes", method = GET)
    public List<Cliente> clientes(@RequestAttribute RoleUtilizador role) {
        if (role != FUNCIONARIO) {
            throw new AuthorizationServiceException("Sem autorização");
        }
        return clienteService.list();
    }

    @RequestMapping(value = "clientes/{id}", method = GET)
    public Cliente getCliente(@PathVariable long id, @RequestAttribute RoleUtilizador role) {
        if (role != FUNCIONARIO) {
            throw new AuthorizationServiceException("Sem autorização");
        }
        return clienteService.get(id).orElseThrow(() -> new EntityNotFoundException("Cliente não existe"));
    }

    @RequestMapping(path = "funcionarios", method = GET)
    public List<Funcionario> funcionarios(@RequestAttribute RoleUtilizador role) {
        if (role != FUNCIONARIO) {
            throw new AuthorizationServiceException("Sem autorização");
        }
        return funcionarioService.list();
    }

    @RequestMapping(value = "funcionarios/{id}", method = GET)
    public Funcionario getFuncionario(@PathVariable long id, @RequestAttribute RoleUtilizador role) {
        if (role != FUNCIONARIO) {
            throw new AuthorizationServiceException("Sem autorização");
        }
        return funcionarioService.get(id).orElseThrow(() -> new EntityNotFoundException("Funcionário não existe"));
    }

    @RequestMapping(value = "funcionarios/apagar", method = DELETE)
    public void apagarFuncionario(@RequestParam long uid, @RequestAttribute RoleUtilizador role) {
        if (role != FUNCIONARIO) {
            throw new AuthorizationServiceException("Sem autorização");
        }
        funcionarioService.apagar(uid);
    }
}
