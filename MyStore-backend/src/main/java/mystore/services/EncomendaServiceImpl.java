package mystore.services;

import mystore.daos.EncomendaDAO;
import mystore.daos.LinhaEncomendaDAO;
import mystore.daos.ProdutoDAO;
import mystore.models.*;
import mystore.models.enums.EstadoEncomenda;
import mystore.models.enums.MetodoPagamento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static mystore.models.enums.EstadoEncomenda.*;


@Service
@Transactional
public class EncomendaServiceImpl implements EncomendaService {

    @Autowired
    private EncomendaDAO encomendaDAO;

    @Autowired
    private LinhaEncomendaDAO linhaEncomendaDAO;

    @Autowired
    private ProdutoDAO produtoDAO;


    @Override
    public void save(Encomenda encomenda) {
        for (LinhaEncomenda linha : encomenda.getLinhasEncomenda()) {
            linhaEncomendaDAO.save(linha);
        }
        encomendaDAO.save(encomenda);
        for (LinhaEncomenda linha : encomenda.getLinhasEncomenda()) {
            if (encomenda.getEstado() == AGUARDA_PAGAMENTO) {
                encomendaDAO.updateEstatisticasEncomenda(linha);
            }
            if (encomenda.getEstado() != CANCELADA) {
                produtoDAO.decrementStock(linha.getProduto().getCodigo(), linha.getQuantidade());
            }
        }
    }

    @Override
    public void update(Encomenda encomenda) {
        encomendaDAO.update(encomenda);
    }

    @Override
    @Transactional
    public List<Encomenda> list() {
        return encomendaDAO.getAll();
    }

    @Override
    public List<Encomenda> ultimas(int quantidadeEncomendas) {
        return encomendaDAO.ultimas(quantidadeEncomendas);
    }

    @Override
    @Transactional
    public List<Encomenda> listByCliente(long uid) {
        return null;
    }

    @Override
    public Optional<Encomenda> get(long id) {
        return encomendaDAO.find(id);
    }

    @Override
    public Encomenda checkout(Cliente cliente, Morada moradaEntrega, Carrinho carrinho, MetodoPagamento metodoPagamento) {
        Encomenda encomenda = new Encomenda();
        encomenda.setCliente(cliente);
        encomenda.setMoradaEntrega(moradaEntrega);
        encomenda.setMetodoPagamento(metodoPagamento);
        encomenda.setEstado(AGUARDA_PAGAMENTO);
        Set<LinhaEncomenda> linhasEncomenda = new HashSet<>();
        System.out.println("AQUI2");
        for (LinhaCarrinho linhaCarrinho : carrinho.getLinhasCarrinho()) {
            LinhaEncomenda linhaEncomenda = new LinhaEncomenda();
            linhaEncomenda.setEncomenda(encomenda);
            linhaEncomenda.setProduto(linhaCarrinho.getProduto());
            linhaEncomenda.setQuantidade(linhaCarrinho.getQuantidade());
            linhasEncomenda.add(linhaEncomenda);
        }
        System.out.println("AQUI3");
        encomenda.setLinhasEncomenda(linhasEncomenda);
        save(encomenda);
        System.out.println("AQUI4");
        return encomenda;
    }

    @Override
    public Optional<Encomenda> alterarEstado(long id, EstadoEncomenda estadoEncomenda) {
        Optional<Encomenda> optionalEncomenda = encomendaDAO.find(id);
        if (optionalEncomenda.isPresent()) {
            Encomenda encomenda = optionalEncomenda.get();
            switch (estadoEncomenda) {
                case EM_PROCESSAMENTO:
                    if (encomenda.getEstado() != AGUARDA_PAGAMENTO) {
                        return Optional.empty();
                    }
                    LocalDateTime now = LocalDateTime.now();
                    if (encomenda.getDataLimitePagamento().isBefore(now.toLocalDate())) {
                        return Optional.empty();
                    }
                    encomenda.setDataPagamento(now);
                    for (LinhaEncomenda linha : encomenda.getLinhasEncomenda()) {
                        encomendaDAO.updateEstatisticasVenda(linha);
                    }
                    break;
                case ENVIADA:
                    if (encomenda.getEstado() != EM_PROCESSAMENTO) {
                        return Optional.empty();
                    }
                    break;
                case ENTREGUE:
                    if (encomenda.getEstado() != ENVIADA) {
                        return Optional.empty();
                    }
                    break;
                case CANCELADA:
                    if (encomenda.getEstado() != AGUARDA_PAGAMENTO) {
                        return Optional.empty();
                    }
                    break;
            }
            encomenda.setEstado(estadoEncomenda);
            update(encomenda);
            return Optional.of(encomenda);
        } else {
            return Optional.empty();
        }
    }
}
