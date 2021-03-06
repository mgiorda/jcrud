package com.jcrud.jpa;

import java.util.List;

public interface GenericDao {

	long save(Object object);

	void update(Object object);

	void delete(Object object);

	void deleteById(Class<?> daoClass, long id);

	<T> T getById(Class<T> daoClass, long id);

	<T> List<T> getElements(Class<T> daoClass, int offset, int limit);

	<T> List<T> getElements(DaoCriteria<T> criteria, int offset, int limit);
}