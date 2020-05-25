package com.es.service;

import com.es.dao.ReportEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ReportDao extends ElasticsearchRepository<ReportEntity, String> {
}
