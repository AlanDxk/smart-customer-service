package org.example.smart.ai;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class KnowledgeSyncTest {

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Test
    public void syncKnowledgeToVector() {
        System.out.println("开始同步 ES 知识库 → 向量库...");

        try {
            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                    .index("knowledge_base")
                    .query(q -> q.matchAll(m -> m))
                    .size(1000),
                    Map.class);

            int count = 0;
            for (Hit<Map> hit : response.hits().hits()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> doc = (Map<String, Object>) hit.source();
                if (doc == null) continue;

                String content = (String) doc.get("content");
                String title = (String) doc.get("title");
                if (content == null) continue;

                String text = (title != null ? title + "：" : "") + content;

                Embedding embedding = embeddingModel.embed(text).content();
                TextSegment segment = TextSegment.from(text);

                embeddingStore.add(embedding, segment);
                count++;
                System.out.println("已同步: " + text.substring(0, Math.min(50, text.length())) + "...");
            }

            System.out.println("同步完成！共处理 " + count + " 条记录");
        } catch (Exception e) {
            System.err.println("同步失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}