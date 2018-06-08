package mystore.services;

import mystore.daos.ProdutoDAO;
import mystore.models.Produto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProdutoServiceImpl implements ProdutoService {

    @Autowired
    protected ProdutoDAO produtoDAO;


    @Override
    @Transactional
    public List<Produto> list() {
        return produtoDAO.getAll();
    }

    @Override
    public List<Produto> listPromocao() {
        return null;
    }

    @Override
    public void save(Produto objToSave) {
        produtoDAO.save(objToSave);
    }
}