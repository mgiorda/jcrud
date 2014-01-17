package com.jcrud.jpa;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class GenericDaoImpl extends HibernateDaoSupport implements GenericDao {

	public String save(Object object) {

		Serializable id = getHibernateTemplate().save(object);
		return String.valueOf(id);
	}

	public void update(Object object) {
		getHibernateTemplate().update(object);
	}

	public void delete(Object object) {
		getHibernateTemplate().delete(object);
	}

	public void deleteById(Class<?> daoClass, String id) {

		Object object = getById(daoClass, id);
		delete(object);
	}

	public <T> T getById(Class<T> daoClass, String id) {

		@SuppressWarnings("unchecked")
		T object = (T) getHibernateTemplate().get(daoClass, id);

		return object;
	}

	public <T> List<T> getElements(Class<T> daoClass, int offset, int limit) {

		DetachedCriteria criteria = DetachedCriteria.forClass(daoClass);

		@SuppressWarnings("unchecked")
		List<T> elements = getHibernateTemplate().findByCriteria(criteria, offset, limit);

		return elements;
	}
}