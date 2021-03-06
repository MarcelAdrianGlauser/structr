/**
 * Copyright (C) 2010-2017 Structr GmbH
 *
 * This file is part of Structr <http://structr.org>.
 *
 * Structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.rest.resource;

import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.structr.api.graph.Direction;
import org.structr.api.util.Iterables;
import org.structr.common.PagingHelper;
import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.GraphObject;
import org.structr.core.Result;
import org.structr.core.app.App;
import org.structr.core.app.StructrApp;
import org.structr.core.entity.AbstractNode;
import org.structr.core.graph.NodeFactory;
import org.structr.core.graph.RelationshipInterface;
import org.structr.core.property.PropertyKey;
import org.structr.rest.exception.IllegalPathException;

/**
 *
 *
 */
public class RelationshipResource extends WrappingResource {

	public static final String REQUEST_PARAMETER_FILTER_INTERNAL_RELATIONSHIP_TYPES = "domainOnly";
	private static final Logger logger = LoggerFactory.getLogger(RelationshipResource.class.getName());
	private Direction direction = null;

	@Override
	public boolean checkAndConfigure(final String part, final SecurityContext securityContext, final HttpServletRequest request) {

		this.securityContext = securityContext;

		if ("in".equals(part.toLowerCase())) {

			direction = Direction.INCOMING;
			return true;

		} else if ("out".equals(part.toLowerCase())) {

			direction = Direction.OUTGOING;
			return true;

		}

		return false;
	}

	@Override
	public Result doGet(final PropertyKey sortKey, final boolean sortDescending, final int pageSize, final int page) throws FrameworkException {

		// fetch all results, paging is applied later
		final List<? extends GraphObject> results = wrappedResource.doGet(null, false, NodeFactory.DEFAULT_PAGE_SIZE, NodeFactory.DEFAULT_PAGE).getResults();
		final App app                             = StructrApp.getInstance();

		if (results != null && !results.isEmpty()) {

			try {

				final List<GraphObject> resultList = new LinkedList<>();
				for (GraphObject obj : results) {

					if (obj instanceof AbstractNode) {

						final List<? extends RelationshipInterface> relationships = Direction.INCOMING.equals(direction) ?

							Iterables.toList(((AbstractNode) obj).getIncomingRelationships()) :
							Iterables.toList(((AbstractNode) obj).getOutgoingRelationships());

						if (relationships != null) {

							boolean filterInternalRelationshipTypes = false;

							if (securityContext != null && securityContext.getRequest() != null) {

								final String filterInternal = securityContext.getRequest().getParameter(REQUEST_PARAMETER_FILTER_INTERNAL_RELATIONSHIP_TYPES);
								if (filterInternal != null) {

									filterInternalRelationshipTypes = "true".equals(filterInternal);
								}
							}

							// allow the user to remove internal relationship types from
							// the result set using the request parameter "filterInternal=true"
							if (filterInternalRelationshipTypes) {

								for (final RelationshipInterface rel : relationships) {

									if (!rel.isInternal()) {
										resultList.add(rel);
									}
								}

							} else {

								resultList.addAll(relationships);
							}
						}
					}
				}

				final int rawResultCount = resultList.size();

				return new Result(PagingHelper.subList(resultList, pageSize, page), rawResultCount, true, false);

			} catch (Throwable t) {

				logger.warn("Exception while fetching relationships", t);
			}

		} else {

			logger.info("No results from parent..");

		}

		throw new IllegalPathException(getResourceSignature() + " can only be applied to a non-empty resource");
	}

	@Override
	public Resource tryCombineWith(final Resource next) throws FrameworkException {

		if (next instanceof UuidResource) {
			return next;
		}

		return super.tryCombineWith(next);
	}

	@Override
	public boolean isCollectionResource() {
		return true;
	}

        @Override
        public String getResourceSignature() {
                return wrappedResource.getResourceSignature();
        }
}
