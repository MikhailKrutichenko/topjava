package ru.javawebinar.topjava.dao;

import java.util.List;

public interface CRUD<E> {
    List<E> getAll();

    E getById(int id);

    E update(int id, E entity);

    boolean delete(int id);

    E create(E entity);
}
