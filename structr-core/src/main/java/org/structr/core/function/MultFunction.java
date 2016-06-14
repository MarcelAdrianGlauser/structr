/**
 * Copyright (C) 2010-2016 Structr GmbH
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
package org.structr.core.function;

import org.structr.common.error.FrameworkException;
import org.structr.core.GraphObject;
import org.structr.schema.action.ActionContext;
import org.structr.schema.action.Function;

/**
 *
 */
public class MultFunction extends Function<Object, Object> {

	public static final String ERROR_MESSAGE_MULT = "Usage: ${mult(value1, value2)}. Example: ${mult(5, 2)}";

	@Override
	public String getName() {
		return "mult()";
	}

	@Override
	public Object apply(final ActionContext ctx, final GraphObject entity, final Object[] sources) throws FrameworkException {

		Double result = 1.0d;

		if (sources != null) {

			for (Object i : sources) {

				try {

					result *= Double.parseDouble(i.toString());

				} catch (Throwable t) {

					logException(entity, t, sources);

					return t.getMessage();

				}
			}

		} else {

			logParameterError(entity, sources, ctx.isJavaScriptContext());

		}

		return result;

	}


	@Override
	public String usage(boolean inJavaScriptContext) {
		return ERROR_MESSAGE_MULT;
	}

	@Override
	public String shortDescription() {
		return "Multiplies the first argument by the second argument";
	}

}
