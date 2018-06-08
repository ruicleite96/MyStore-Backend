package mystore.services;

import mystore.daos.CategoriaDAO;
import mystore.models.Categoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class CategoriaServiceImpl implements CategoriaService {


    @Autowired
    protected CategoriaDAO categoriaDAO;


    @Override
    public void save(Categoria objToSave) {
        categoriaDAO.save(objToSave);
    }

    @Override
    public List<Categoria> list() {
        return categoriaDAO.getAll();
    }
}
