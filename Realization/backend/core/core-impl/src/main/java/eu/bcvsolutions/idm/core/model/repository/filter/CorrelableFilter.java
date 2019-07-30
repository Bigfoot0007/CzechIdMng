package eu.bcvsolutions.idm.core.model.repository.filter;

import java.lang.reflect.Field;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import eu.bcvsolutions.idm.core.api.dto.filter.CorrelationFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.exception.CorrelationPropertyNotExistsException;
import eu.bcvsolutions.idm.core.api.exception.CorrelationPropertyUnsupportedTypeException;
import eu.bcvsolutions.idm.core.api.repository.filter.BaseFilterBuilder;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * Correlation filter
 * 
 * @author svandav
 */
@Component
public class CorrelableFilter<E extends AbstractEntity> extends BaseFilterBuilder<E, DataFilter> {

	@Override
	public String getName() {
		return CorrelationFilter.PARAMETER_CORRELATION_PROPERTY;
	}

	@Override
	public Predicate getPredicate(Root<E> root, AbstractQuery<?> query, CriteriaBuilder builder, DataFilter filter) {

		if (!(filter instanceof CorrelationFilter)
				|| Strings.isNullOrEmpty(((CorrelationFilter) filter).getProperty())) {
			return null;
		}

		CorrelationFilter correlationFilter = (CorrelationFilter) filter;
		try {
			// Check if the property exists in the entity
			Field field = EntityUtils.getFirstFieldInClassHierarchy(root.getJavaType(), correlationFilter.getProperty());
			// Only search by String is supported now
			if (String.class != field.getType()) {
				throw new CorrelationPropertyUnsupportedTypeException(root.getJavaType().getCanonicalName(),
						correlationFilter.getProperty());
			}
		} catch (NoSuchFieldException e) {
			throw new CorrelationPropertyNotExistsException(root.getJavaType().getCanonicalName(),
					correlationFilter.getProperty(), e);
		}

		return builder.equal(root.get(correlationFilter.getProperty()), correlationFilter.getValue());
	}

	@Override
	public Page<E> find(DataFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException(
				"Find by external code only is not supported, use concrete service instead.");
	}
}
