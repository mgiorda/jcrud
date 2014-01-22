package com.jcrud.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

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
	@Qualifier("rootTypeAdapter")
	private HttpTypeAdapter httpTypeAdapter;

	public CRUDDispatcherImpl(Map<String, Class<?>> pathAssignments) {
		this.pathAssignments = pathAssignments;
	}

	@Override
	public void registerResource(String path, Class<?> resourceClass) {
		pathAssignments.put(path, resourceClass);
	}

	@Override
	public HttpResponse handleRequest(HttpRequest request) {

		HttpResponse response = null;

		String path = request.getPath();
		long id = -1;

		Class<?> resourceClass = pathAssignments.get(path);

		if (resourceClass == null) {
			String pathWithId = path.substring(0, path.lastIndexOf("/"));
			String strId = path.substring(path.lastIndexOf("/") + 1, path.length());
			id = Long.valueOf(strId);

			resourceClass = pathAssignments.get(pathWithId);
		}

		if (resourceClass != null) {
			RestHandler restHandler = restHandlerFactory.getRestHandler(resourceClass);
			response = (id == -1) ? getRestRequest(restHandler, request, resourceClass) : getRestRequestWithId(restHandler, request, resourceClass, id);
		}

		if (response == null) {
			return ExceptionResponse.new404(String.format("There isn't any resource assigned to path '%s'", path));
		}
		return response;
	}

	private <T> HttpResponse getRestRequest(RestHandler restHandler, HttpRequest request, Class<T> resourceClass) {

		HttpResponse response = null;

		HttpMethod method = request.getMethod();
		if (method == HttpMethod.POST) {

			T contentObject = httpTypeAdapter.getFromRequest(request, resourceClass);
			T responseObject = restHandler.handlePOST(contentObject);

			response = httpTypeAdapter.toHttpResponse(request, responseObject);
		} else if (method == HttpMethod.GET) {

			int elementsCount = getIntegerQueryParam(request, "elementsCount");
			int pageNumber = getIntegerQueryParam(request, "pageNumber");

			List<T> elements = null;

			String filterParamName = "filter";

			String query = request.getQueryParam(filterParamName);

			if (query != null) {
				try {
					query = URLDecoder.decode(query, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					throw new IllegalStateException(e);
				}
			} else {
				query = request.getHeader(filterParamName);
			}
			if (query != null) {
				elements = restHandler.handleGET(resourceClass, elementsCount, pageNumber, query);
			} else {
				elements = restHandler.handleGET(resourceClass, elementsCount, pageNumber);
			}
			response = httpTypeAdapter.toHttpResponse(request, elements);
		}

		if (response == null) {
			response = ExceptionResponse.new400(String.format("Method '%s' not allowed without id", method));
		}
		return response;
	}

	private <T> HttpResponse getRestRequestWithId(RestHandler restHandler, HttpRequest request, Class<T> resourceClass, long id) {

		HttpResponse response = null;

		HttpMethod method = request.getMethod();

		try {
			if (method == HttpMethod.DELETE) {

				restHandler.handleDELETE(resourceClass, id);
				String successMessage = "Successfully deleted resource";

				response = httpTypeAdapter.toHttpResponse(request, successMessage);

			} else if (method == HttpMethod.PUT) {

				T contentObject = httpTypeAdapter.getFromRequest(request, resourceClass);
				T responseObject = restHandler.handlePUT(id, contentObject);

				response = httpTypeAdapter.toHttpResponse(request, responseObject);

			} else if (method == HttpMethod.GET) {

				T element = restHandler.handleGET(resourceClass, id);
				response = httpTypeAdapter.toHttpResponse(request, element);
			}

		} catch (CRUDResourceNotExistent e) {
			response = ExceptionResponse.fromException(e);
		}
		if (response == null) {
			return ExceptionResponse.new400(String.format("Method '%s' not allowed with id", method));
		}

		return response;
	}

	private int getIntegerQueryParam(HttpRequest request, String queryName) {

		int intValue = -1;

		String queryParam = request.getQueryParam(queryName);
		if (queryParam != null) {
			return Integer.valueOf(queryParam);
		}

		return intValue;
	}
}
