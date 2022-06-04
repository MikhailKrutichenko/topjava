package ru.javawebinar.topjava.dao;

import java.util.List;

public interface CRUD<E> {
    List<E> getAll();

    E getById(Integer id);

    E update(E entity);

    E delete(Integer id);

    E create(E entity);
}
