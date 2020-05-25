package com.es.service;

import com.es.dao.PhysicalEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PhysicalDao extends ElasticsearchRepository<PhysicalEntity, String> {
}
