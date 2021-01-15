package com.zylitics.front.esdb;

import com.google.common.base.Strings;
import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.model.ZwlProgramOutput;
import com.zylitics.front.model.ZwlProgramOutputIndexFields;
import com.zylitics.front.provider.ZwlProgramOutputProvider;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EsdbZwlProgramOutputProvider implements ZwlProgramOutputProvider {
  
  // we want all available output fetched. This is important so that if client delays sending a
  // request for some version and if that version is now completed, client has just 1 request to
  // fetch all output because it will received a complete status and will move to next version. If
  // we fetch partial output, client may end up receiving uncompleted output for a version.
  // I feel 1000 should be enough to get all output
  private final static int MAX_OUTPUT_SIZE = 1000;
  
  private final RestHighLevelClient restHighLevelClient;
  
  private final APICoreProperties apiCoreProperties;
  
  public EsdbZwlProgramOutputProvider(RestHighLevelClient restHighLevelClient,
                                      APICoreProperties apiCoreProperties) {
    this.restHighLevelClient = restHighLevelClient;
    this.apiCoreProperties = apiCoreProperties;
  }
  
  // - token keeps the date of last record found, nothing else.
  @Override
  public Optional<ZwlProgramOutput> getOutput(int buildId,
                                              int versionId,
                                              @Nullable String nextOutputToken) {
    SearchRequest searchRequest =
        new SearchRequest(apiCoreProperties.getEsdb().getZwlProgramOutputIndex());
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    BoolQueryBuilder query = QueryBuilders.boolQuery()
        .must(QueryBuilders.termQuery(ZwlProgramOutputIndexFields.BUILD_ID, buildId))
        .must(QueryBuilders.termQuery(ZwlProgramOutputIndexFields.TEST_VERSION_ID, versionId));
    if (!Strings.isNullOrEmpty(nextOutputToken)) {
      query.must(QueryBuilders.rangeQuery(ZwlProgramOutputIndexFields.CREATE_DATE)
          .gt(OffsetDateTime.parse(nextOutputToken)));
    }
    sourceBuilder
        .query(query)
        .sort(ZwlProgramOutputIndexFields.CREATE_DATE, SortOrder.ASC)
        .size(MAX_OUTPUT_SIZE);
    sourceBuilder.fetchSource(new String[] {
        ZwlProgramOutputIndexFields.OUTPUT,
        ZwlProgramOutputIndexFields.CREATE_DATE,
        ZwlProgramOutputIndexFields.ENDED
    }, null);
    searchRequest.source(sourceBuilder);
    SearchHit[] hits;
    try {
      hits = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT)
          .getHits().getHits();
    } catch (IOException io) {
      throw new RuntimeException(io);
    }
    int totalHits = hits.length;
    List<String> outputs = new ArrayList<>();
    for (SearchHit hit : hits) {
      outputs.add((String) hit.getSourceAsMap().get(ZwlProgramOutputIndexFields.OUTPUT));
    }
    if (outputs.size() > 0) {
      ZwlProgramOutput zwlProgramOutput = new ZwlProgramOutput().setOutputs(outputs);
      Map<String, Object> source = hits[totalHits - 1].getSourceAsMap();
      boolean ended = (boolean) source.get(ZwlProgramOutputIndexFields.ENDED);
      if (!ended) {
        // cast to string as we just need to keep it as a string token, no parsing needed at this
        // step
        zwlProgramOutput
            .setNextOutputToken((String) source.get(ZwlProgramOutputIndexFields.CREATE_DATE));
      }
      return Optional.of(zwlProgramOutput);
    } else if (!Strings.isNullOrEmpty(nextOutputToken)) {
      // when no output is found (whether with or without token), just return the token received.
      return Optional.of(new ZwlProgramOutput().setNextOutputToken(nextOutputToken));
    }
    return Optional.empty();
  }
}
