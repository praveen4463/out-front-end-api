package com.zylitics.front.esdb;

import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.model.ShotBasicDetails;
import com.zylitics.front.model.ShotMetadataIndexFields;
import com.zylitics.front.provider.ShotProvider;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.ValueCount;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Optional;

@Service
public class EsdbShotProvider implements ShotProvider {
  
  private final RestHighLevelClient restHighLevelClient;
  
  private final APICoreProperties apiCoreProperties;
  
  public EsdbShotProvider(RestHighLevelClient restHighLevelClient,
                          APICoreProperties apiCoreProperties) {
    this.restHighLevelClient = restHighLevelClient;
    this.apiCoreProperties = apiCoreProperties;
  }
  
  @Override
  public Optional<String> getLatestShot(int buildId) {
    SearchRequest searchRequest =
        new SearchRequest(apiCoreProperties.getEsdb().getShotMetadataIndex());
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    QueryBuilder query = QueryBuilders.boolQuery()
        .must(QueryBuilders.termQuery(ShotMetadataIndexFields.BUILD_ID, buildId));
    sourceBuilder
        .query(query)
        .sort(ShotMetadataIndexFields.CREATE_DATE, SortOrder.DESC)
        .size(1);
    sourceBuilder.fetchSource(ShotMetadataIndexFields.SHOT_NAME, null);
    searchRequest.source(sourceBuilder);
    SearchHit[] hits;
    try {
      hits = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT)
          .getHits().getHits();
    } catch (IOException io) {
      throw new RuntimeException(io);
    }
    if (hits.length == 0) {
      return Optional.empty();
    }
    return Optional.of((String) hits[0].getSourceAsMap().get(ShotMetadataIndexFields.SHOT_NAME));
  }
  
  @Override
  public Optional<ShotBasicDetails> getShotBasicDetails(int buildId, @Nullable Integer versionId) {
    SearchRequest searchRequest =
        new SearchRequest(apiCoreProperties.getEsdb().getShotMetadataIndex());
    SearchSourceBuilder sourceBuilderFirst = new SearchSourceBuilder();
    BoolQueryBuilder query = QueryBuilders.boolQuery()
        .must(QueryBuilders.termQuery(ShotMetadataIndexFields.BUILD_ID, buildId));
    if (versionId != null) {
      query.must(QueryBuilders.termQuery(ShotMetadataIndexFields.TEST_VERSION_ID, versionId));
    }
    String fieldShotName = ShotMetadataIndexFields.SHOT_NAME;
    sourceBuilderFirst
        .query(query)
        .aggregation(AggregationBuilders.count("total_shots")
            .field(fieldShotName))
        .sort(ShotMetadataIndexFields.CREATE_DATE, SortOrder.ASC)
        .size(1);
    sourceBuilderFirst.fetchSource(fieldShotName, null);
    searchRequest.source(sourceBuilderFirst);
    SearchResponse response;
    try {
      response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    } catch (IOException io) {
      throw new RuntimeException(io);
    }
    Aggregations aggregations = response.getAggregations();
    ValueCount countAgg = aggregations.get("total_shots");
    long totalShots = countAgg.getValue();
    if (totalShots == 0) {
      return Optional.empty();
    }
    SearchHit[] hits = response.getHits().getHits();
    String firstShot = (String) hits[0].getSourceAsMap().get(fieldShotName);
    // get last shot in a separate query
    SearchSourceBuilder sourceBuilderLast = new SearchSourceBuilder();
    sourceBuilderLast
        .query(query)
        .sort(ShotMetadataIndexFields.CREATE_DATE, SortOrder.DESC)
        .size(1);
    sourceBuilderLast.fetchSource(fieldShotName, null);
    searchRequest.source(sourceBuilderLast); // use the same searchRequest and response variables
    try {
      response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    } catch (IOException io) {
      throw new RuntimeException(io);
    }
    hits = response.getHits().getHits();
    String lastShot = (String) hits[0].getSourceAsMap().get(fieldShotName);
    return Optional.of(new ShotBasicDetails()
        .setTotalShots(totalShots)
        .setFirstShot(firstShot)
        .setLastShot(lastShot));
  }
}
