package ru.javawebinar.topjava.dao;

import java.util.List;

public interface Crud<E> {
    List<E> getAll();

    E getById(int id);

    E update(E entity);

    boolean delete(int id);

    E create(E entity);
}
