package com.jcrud.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.jcrud.model.CRUDDispatcher;
import com.jcrud.model.HttpMethod;
import com.jcrud.model.HttpRequest;
import com.jcrud.model.HttpResponse;
import com.jcrud.model.HttpTypeAdapter;
import com.jcrud.model.RestHandler;
import com.jcrud.model.exceptions.CRUDResourceNotExistent;

public class CRUDDispatcherImpl implements CRUDDispatcher {

	private final Map<String, Class<?>> pathAssignments;

	@Autowired
	private RestHandlerFactory restHandlerFactory;

	@Autowired
	private HttpTypeAdapter httpTypeAdapter;

	public CRUDDispatcherImpl(Map<String, Class<?>> pathAssignments) {
		this.pathAssignments = pathAssignments;
	}

	@Override
	public HttpResponse handleRequest(HttpRequest request) {

		HttpResponse response = null;

		String path = request.getPath();
		String id = null;

		Class<?> resourceClass = pathAssignments.get(path);

		if (resourceClass == null) {
			String pathWithId = path.substring(0, path.lastIndexOf("/"));
			id = path.substring(path.lastIndexOf("/") + 1, path.length());

			resourceClass = pathAssignments.get(pathWithId);
		}

		if (resourceClass != null) {
			RestHandler<?> restHandler = restHandlerFactory.newRestHandler(resourceClass);
			response = (id == null) ? getRestRequest(restHandler, request) : getRestRequestWithId(restHandler, request, id);
		}

		if (response == null) {
			return ExceptionResponse.new404(String.format("There isn't any resource assigned to path '%s'", path));
		}
		return response;
	}

	private <T> HttpResponse getRestRequest(RestHandler<T> restHandler, HttpRequest request) {

		HttpResponse response = null;

		HttpMethod method = request.getMethod();
		if (method == HttpMethod.POST) {

			Class<T> resourceClass = restHandler.getResourceClass();
			T contentObject = httpTypeAdapter.getFromRequest(request, resourceClass);
			T responseObject = restHandler.handlePOST(contentObject);

			response = httpTypeAdapter.toHttpResponse(responseObject);
		} else if (method == HttpMethod.GET) {

			// TODO: obtener de los queryParams de la request
			int elementsCount = 50;
			int pageNumber = 0;

			List<T> elements = restHandler.handleGET(elementsCount, pageNumber);
			response = httpTypeAdapter.toHttpResponse(elements);
		}

		if (response == null) {
			response = ExceptionResponse.new400(String.format("Method '%s' not allowed without id", method));
		}
		return response;
	}

	private <T> HttpResponse getRestRequestWithId(RestHandler<T> restHandler, HttpRequest request, String id) {

		HttpResponse response = null;

		HttpMethod method = request.getMethod();

		try {
			if (method == HttpMethod.DELETE) {

				restHandler.handleDELETE(id);

			} else if (method == HttpMethod.PUT) {

				Class<T> resourceClass = restHandler.getResourceClass();
				T contentObject = httpTypeAdapter.getFromRequest(request, resourceClass);
				T responseObject = restHandler.handlePUT(id, contentObject);

				response = httpTypeAdapter.toHttpResponse(responseObject);

			} else if (method == HttpMethod.GET) {

				T element = restHandler.handleGET(id);
				response = httpTypeAdapter.toHttpResponse(element);
			}

		} catch (CRUDResourceNotExistent e) {
			response = ExceptionResponse.fromException(e);
		}
		if (response == null) {
			return ExceptionResponse.new400(String.format("Method '%s' not allowed with id", method));
		}

		return response;
	}
}