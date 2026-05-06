package org.example.smart.ai.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchConfiguration;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchConfigurationKnn;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class RagConfig {

    @Value("${langchain4j.elasticsearch.server-url}")
    private String serverUrl;

    @Value("${langchain4j.elasticsearch.index-name}")
    private String indexName;

    @Value("${langchain4j.elasticsearch.username}")
    private String username;

    @Value("${langchain4j.elasticsearch.password}")
    private String password;

    /**
     * ES REST 客户端（配置 SSL 信任所有 + Basic Auth）
     */
    @Bean(destroyMethod = "close")
    public RestClient restClient() throws Exception {
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, (chain, authType) -> true)
                .build();

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password)
        );

        return RestClient.builder(HttpHost.create(serverUrl))
                .setHttpClientConfigCallback(hcb -> hcb
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .setSSLContext(sslContext)
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE))
                .build();
    }

    /**
     * ES 高级客户端（供需要原生查询的场景使用）
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        return new ElasticsearchClient(
                new RestClientTransport(restClient, new JacksonJsonpMapper())
        );
    }

//    /**
//     * 【修改点】内存向量存储
//     * 启动时从 ES 全量加载数据到内存，后续查询在内存中进行，避开 ES KNN Bug
//     */
//    @Bean
//    public EmbeddingStore<TextSegment> embeddingStore(ElasticsearchClient client) {
//        InMemoryEmbeddingStore<TextSegment> inMemoryStore = new InMemoryEmbeddingStore<>();
//
//        try {
//            System.out.println("正在从 Elasticsearch 加载向量数据到内存...");
//
//            // 查询 ES 中所有数据 (假设 index 名为 rag_documents，如果不对请修改)
//            // 注意：这里使用 matchAll 获取所有文档，如果数据量巨大(>10万)，启动会变慢
//            SearchResponse<Map> response = client.search(s -> s
//                            .index("rag_documents")
//                            .query(q -> q.matchAll(m -> m))
//                            .size(10000), // 设置一个合理的上限，或者用 scroll 查询
//                    Map.class
//            );
//
//            List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();
//
//            for (Hit<Map> hit : response.hits().hits()) {
//                Map<String, Object> source = hit.source();
//                if (source == null) continue;
//
//                // 1. 获取文本
//                String text = (String) source.get("text");
//                if (text == null) continue;
//
//                // 2. 获取向量 (根据你的 JSON，它是个 List<Number>)
//                List<Number> vectorList = (List<Number>) source.get("vector");
//                float[] vector = new float[vectorList.size()];
//                for (int i = 0; i < vectorList.size(); i++) {
//                    vector[i] = vectorList.get(i).floatValue();
//                }
//                Embedding embedding = Embedding.from(vector);
//
//                // 3. 组装对象放入内存
//                TextSegment segment = TextSegment.from(text);
//                inMemoryStore.add(embedding, segment);
//            }
//
//            System.out.println("向量数据加载完成，共加载: " + response.hits().total().value() + " 条");
//
//        } catch (IOException e) {
//            System.err.println("加载 ES 向量数据失败: " + e.getMessage());
//            e.printStackTrace();
//        }
//
//        return inMemoryStore;
//    }

    /**
     * Elasticsearch 向量存储
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(RestClient restClient) {
        return ElasticsearchEmbeddingStore.builder()
                .restClient(restClient)
                .indexName(indexName)
                .build();
    }


    /**
     * RAG 检索增强器（将 ES 检索结果注入 LLM 上下文）
     */
    @Bean
    public RetrievalAugmentor retrievalAugmentor(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel) {

        var contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.6)
                .build();

        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .build();
    }
}