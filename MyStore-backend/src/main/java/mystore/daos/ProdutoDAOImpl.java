package mystore.daos;

import mystore.models.Categoria;
import mystore.models.LinhaEncomenda;
import mystore.models.Produto;
import mystore.models.Promocao;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static javax.persistence.criteria.JoinType.*;

@Repository("produtoDAO")
public class ProdutoDAOImpl extends GenericDAOImpl<Produto, Long> implements ProdutoDAO {

    @Override
    public List<Produto> novidades(int quantidadeProdutos) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Produto> criteriaQuery = criteriaBuilder.createQuery(Produto.class);
        Root<Produto> root = criteriaQuery.from(Produto.class);
        Predicate novidade = criteriaBuilder.greaterThanOrEqualTo(root.get("dataRegisto"), LocalDateTime.now().minusDays(7));
        Order porDataDesc = criteriaBuilder.desc(root.get("dataRegisto"));
        criteriaQuery
                .select(root)
                .where(novidade)
                .orderBy(porDataDesc);
        return entityManager.createQuery(criteriaQuery).setMaxResults(quantidadeProdutos).getResultList();
    }

    @Override
    public List<Produto> maisVendidos(int quantidadeProdutos) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> criteriaQuery = criteriaBuilder.createQuery(Object[].class);
        Root<LinhaEncomenda> root = criteriaQuery.from(LinhaEncomenda.class);

        Join<LinhaEncomenda, Produto> linhaEncomenda_produto = root.join("produto", INNER);

        Expression<?>[] expressions = new Expression[2];
        expressions[0] = linhaEncomenda_produto.get("codigo");
        expressions[1] = criteriaBuilder.sum(root.get("quantidade"));

        Order porQuantidadeComprada = criteriaBuilder.desc(criteriaBuilder.sum(root.get("quantidade")));

        criteriaQuery
                .multiselect(expressions)
                .groupBy(linhaEncomenda_produto.get("codigo"))
                .orderBy(porQuantidadeComprada);

        List<Object[]> result = entityManager.createQuery(criteriaQuery).setMaxResults(quantidadeProdutos).getResultList();
        return result.parallelStream().map(objects -> find((long) objects[0]).get()).collect(Collectors.toList());
    }

    @Override
    public List<Object[]> maisVendidosDetail(int quantidadeProdutos) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> criteriaQuery = criteriaBuilder.createQuery(Object[].class);
        Root<LinhaEncomenda> root = criteriaQuery.from(LinhaEncomenda.class);

        Join<LinhaEncomenda, Produto> linhaEncomenda_produto = root.join("produto", INNER);

        Expression<?>[] expressions = new Expression[2];
        expressions[0] = linhaEncomenda_produto.get("codigo");
        expressions[1] = criteriaBuilder.sum(root.get("quantidade"));

        Order porQuantidadeComprada = criteriaBuilder.desc(criteriaBuilder.sum(root.get("quantidade")));

        criteriaQuery
                .multiselect(expressions)
                .groupBy(linhaEncomenda_produto.get("codigo"))
                .orderBy(porQuantidadeComprada);

        List<Object[]> list = entityManager.createQuery(criteriaQuery).setMaxResults(quantidadeProdutos).getResultList();
        List<Object[]> result = new ArrayList<>();
        for (Object[] objects : list) {
            Object[] o = new Object[3];
            o[0] = find((long) objects[0]).get();
            o[1] = objects[1];
            o[3] = totalFaturado((long) objects[0]);
            result.add(o);
        }
        return result;
    }

    @Override
    public List<Produto> emPromocao(int quantidadeProdutos) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Produto> criteriaQuery = criteriaBuilder.createQuery(type);
        Root<Produto> root = criteriaQuery.from(type);
        Predicate emPromocao = criteriaBuilder.notEqual(root.get("precoPromocional"), 0.0);
        criteriaQuery
                .select(root)
                .where(emPromocao);
        return entityManager.createQuery(criteriaQuery).setMaxResults(quantidadeProdutos).getResultList();
    }

    @Override
    public List<Produto> listByCategoria(long categoria, int firstResult, int maxResults) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Produto> criteriaQuery = criteriaBuilder.createQuery(Produto.class);
        Root<Produto> root = criteriaQuery.from(type);
        criteriaQuery
                .select(root)
                .where(criteriaBuilder.equal(root.get("categoria"), categoria));
        return entityManager.createQuery(criteriaQuery).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @Override
    public List<Produto> related(Produto produto, int maxResults) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Produto> criteriaQuery = criteriaBuilder.createQuery(type);
        Root<Produto> root = criteriaQuery.from(type);
        criteriaQuery
                .select(root)
                .where(criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("categoria"), produto.getCategoria().getId())),
                        criteriaBuilder.notEqual(root.get("codigo"), produto.getCodigo()));
        return entityManager.createQuery(criteriaQuery).setMaxResults(maxResults).getResultList();
    }

    @Override
    public List<Produto> search(String value) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Produto> criteriaQuery = criteriaBuilder.createQuery(type);
        Root<Produto> root = criteriaQuery.from(type);
        criteriaQuery
                .select(root)
                .where(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), "%" + value.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("descricao")), "%" + value.toLowerCase() + "%"))
                );
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public List<Produto> search(long categoria, String value) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Produto> criteriaQuery = criteriaBuilder.createQuery(type);
        Root<Produto> root = criteriaQuery.from(type);
        criteriaQuery
                .select(root)
                .where(criteriaBuilder.and(
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), "%" + value.toLowerCase() + "%"),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("descricao")), "%" + value.toLowerCase() + "%"))),
                        criteriaBuilder.equal(root.get("categoria"), categoria)
                );
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public void updatePrices(Promocao promocao) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaUpdate<Produto> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(type);
        Root<Produto> root = criteriaUpdate.from(type);

        criteriaUpdate
                .set(root.<Double>get("precoPromocional"),
                        criteriaBuilder.diff(root.get("precoBase"),
                                criteriaBuilder.prod(root.get("precoBase"), promocao.getDesconto())));

        if (promocao.getCategoria() != null) {
            criteriaUpdate
                    .where(criteriaBuilder.equal(root.get("categoria"), promocao.getCategoria().getId()));
        } else {
            Set<Long> ids = promocao.getProdutos().parallelStream().map(Produto::getCodigo).collect(Collectors.toSet());
            criteriaUpdate
                    .where(root.get("codigo").in(ids));
        }
        entityManager.createQuery(criteriaUpdate).executeUpdate();
    }

    @Override
    public double totalFaturado(long codigoProduto) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> criteriaQuery = criteriaBuilder.createQuery(Double.class);
        Root<LinhaEncomenda> root = criteriaQuery.from(LinhaEncomenda.class);
        Expression<Double> preco = criteriaBuilder.diff(root.get("precoUnitario"), root.get("valorDesconto"));
        Expression<Double> precoLinha = criteriaBuilder.prod(preco, root.get("quantidade"));
        Expression<Double> total = criteriaBuilder.sum(precoLinha);
        criteriaQuery
                .select(total.alias("total"));
        return entityManager.createQuery(criteriaQuery).getSingleResult();
    }

}
