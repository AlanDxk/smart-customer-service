//package org.example.smart.ai.config;
//
//import dev.langchain4j.model.openai.OpenAiChatModel;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class LangChain4jConfig {
//
//    @Bean
//    public OpenAiChatModel openAiChatModel() {
//        return OpenAiChatModel.builder()
//                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")//url参考百炼平台API文档
//                .apiKey(System.getenv("API_KEY"))//获取环境变量API_KEY使用
//                .modelName("qwen-plus")//设置模型名称
//                .build();
//    }
//}