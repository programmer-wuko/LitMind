package com.litmind.service.recommend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 外部学术论文服务
 * 从arXiv、Semantic Scholar等外部API获取学术论文
 */
@Service
@Slf4j
public class ExternalPaperService {

    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    @Value("${recommendation.arxiv.enabled:true}")
    private boolean arxivEnabled;

    @Value("${recommendation.semantic-scholar.enabled:true}")
    private boolean semanticScholarEnabled;

    public ExternalPaperService() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)  // 增加读取超时到60秒
                .writeTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)  // 启用连接失败重试
                .build();
    }

    /**
     * 从arXiv搜索论文
     */
    public List<PaperInfo> searchArxivPapers(String query, int maxResults) {
        if (!arxivEnabled) {
            log.debug("arXiv搜索已禁用");
            return new ArrayList<>();
        }

        List<PaperInfo> papers = new ArrayList<>();
        try {
            // arXiv API: http://export.arxiv.org/api/query?search_query=all:keyword&start=0&max_results=10
            // 限制查询长度，避免URL过长
            String searchQuery = query.length() > 100 ? query.substring(0, 100) : query;
            String url = String.format("http://export.arxiv.org/api/query?search_query=all:%s&start=0&max_results=%d",
                    java.net.URLEncoder.encode(searchQuery, "UTF-8"), maxResults);

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .header("User-Agent", "LitMind/1.0")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.warn("arXiv API调用失败: status={}", response.code());
                    return papers;
                }

                String responseBody = response.body().string();
                papers = parseArxivResponse(responseBody, maxResults);
                log.info("从arXiv获取到 {} 篇论文", papers.size());
            }
        } catch (java.net.SocketTimeoutException e) {
            log.error("搜索arXiv论文超时: {}", e.getMessage());
            // 超时不返回结果，让上层逻辑处理
        } catch (Exception e) {
            log.error("搜索arXiv论文失败: {}", e.getMessage(), e);
        }

        return papers;
    }

    /**
     * 从Semantic Scholar搜索论文
     */
    public List<PaperInfo> searchSemanticScholarPapers(String query, int maxResults) {
        if (!semanticScholarEnabled) {
            log.debug("Semantic Scholar搜索已禁用");
            return new ArrayList<>();
        }

        List<PaperInfo> papers = new ArrayList<>();
        try {
            // Semantic Scholar API: https://api.semanticscholar.org/graph/v1/paper/search?query=keyword&limit=10
            String url = String.format("https://api.semanticscholar.org/graph/v1/paper/search?query=%s&limit=%d&fields=title,authors,year,url,abstract",
                    java.net.URLEncoder.encode(query, "UTF-8"), maxResults);

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .header("User-Agent", "LitMind/1.0")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.warn("Semantic Scholar API调用失败: status={}", response.code());
                    return papers;
                }

                String responseBody = response.body().string();
                papers = parseSemanticScholarResponse(responseBody, maxResults);
                log.info("从Semantic Scholar获取到 {} 篇论文", papers.size());
            }
        } catch (Exception e) {
            log.error("搜索Semantic Scholar论文失败: {}", e.getMessage(), e);
        }

        return papers;
    }

    /**
     * 获取热门论文（从arXiv获取最近发布的论文）
     */
    public List<PaperInfo> getHotPapers(int maxResults) {
        List<PaperInfo> papers = new ArrayList<>();
        try {
            // 获取最近提交的论文（使用更具体的分类，避免查询所有论文导致超时）
            // 使用cat:cs.AI来获取AI相关论文，避免查询所有论文导致超时
            String url = String.format("http://export.arxiv.org/api/query?search_query=cat:cs.AI+OR+cat:cs.LG+OR+cat:cs.CV&sortBy=submittedDate&sortOrder=descending&start=0&max_results=%d",
                    maxResults);

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .header("User-Agent", "LitMind/1.0")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.warn("获取arXiv热门论文失败: status={}", response.code());
                    // 如果失败，返回示例论文
                    return getFallbackPapers(maxResults);
                }

                String responseBody = response.body().string();
                papers = parseArxivResponse(responseBody, maxResults);
                log.info("获取到 {} 篇热门论文", papers.size());
                
                // 如果解析结果为空，返回示例论文
                if (papers.isEmpty()) {
                    log.warn("arXiv返回结果为空，使用降级方案");
                    return getFallbackPapers(maxResults);
                }
            }
        } catch (java.net.SocketTimeoutException e) {
            log.error("获取热门论文超时: {}", e.getMessage());
            // 超时情况下返回示例论文
            return getFallbackPapers(maxResults);
        } catch (Exception e) {
            log.error("获取热门论文失败: {}", e.getMessage(), e);
            // 其他错误也返回示例论文
            return getFallbackPapers(maxResults);
        }

        return papers;
    }
    
    /**
     * 降级方案：返回空列表（当外部API失败时，不返回无效的示例论文）
     * 让上层逻辑降级到推荐系统内的文件
     */
    private List<PaperInfo> getFallbackPapers(int maxResults) {
        log.info("外部API失败，返回空列表，让上层逻辑降级到系统内文件推荐");
        // 不再返回无效的示例论文，避免用户点击无效链接
        return new ArrayList<>();
    }

    /**
     * 解析arXiv API响应
     */
    private List<PaperInfo> parseArxivResponse(String xmlResponse, int maxResults) throws Exception {
        List<PaperInfo> papers = new ArrayList<>();

        // 简单的XML解析（可以使用更专业的XML解析库）
        String[] entries = xmlResponse.split("<entry>");
        for (int i = 1; i < entries.length && papers.size() < maxResults; i++) {
            String entry = entries[i];
            try {
                PaperInfo paper = new PaperInfo();

                // 提取标题
                String titleMatch = "<title>";
                int titleStart = entry.indexOf(titleMatch);
                if (titleStart != -1) {
                    int titleEnd = entry.indexOf("</title>", titleStart);
                    if (titleEnd != -1) {
                        String title = entry.substring(titleStart + titleMatch.length(), titleEnd)
                                .replace("\n", " ").trim();
                        paper.setTitle(title);
                    }
                }

                // 提取作者
                List<String> authors = new ArrayList<>();
                String[] authorEntries = entry.split("<author>");
                for (int j = 1; j < authorEntries.length; j++) {
                    String authorEntry = authorEntries[j];
                    int nameStart = authorEntry.indexOf("<name>");
                    if (nameStart != -1) {
                        int nameEnd = authorEntry.indexOf("</name>", nameStart);
                        if (nameEnd != -1) {
                            String authorName = authorEntry.substring(nameStart + 6, nameEnd).trim();
                            authors.add(authorName);
                        }
                    }
                }
                paper.setAuthors(String.join(", ", authors));

                // 提取arXiv ID和URL
                int idStart = entry.indexOf("<id>");
                if (idStart != -1) {
                    int idEnd = entry.indexOf("</id>", idStart);
                    if (idEnd != -1) {
                        String idUrl = entry.substring(idStart + 4, idEnd).trim();
                        // arXiv ID格式: http://arxiv.org/abs/1234.5678v1
                        if (idUrl.contains("/abs/")) {
                            String arxivId = idUrl.substring(idUrl.lastIndexOf("/abs/") + 5);
                            paper.setExternalPaperId(arxivId);
                            paper.setUrl("https://arxiv.org/abs/" + arxivId);
                        }
                    }
                }

                // 提取摘要
                int summaryStart = entry.indexOf("<summary>");
                if (summaryStart != -1) {
                    int summaryEnd = entry.indexOf("</summary>", summaryStart);
                    if (summaryEnd != -1) {
                        String summary = entry.substring(summaryStart + 9, summaryEnd)
                                .replace("\n", " ").trim();
                        paper.setAbstract(summary);
                    }
                }

                if (paper.getTitle() != null && !paper.getTitle().isEmpty()) {
                    paper.setSource("arXiv");
                    papers.add(paper);
                }
            } catch (Exception e) {
                log.warn("解析arXiv条目失败: {}", e.getMessage());
            }
        }

        return papers;
    }

    /**
     * 解析Semantic Scholar API响应
     */
    private List<PaperInfo> parseSemanticScholarResponse(String jsonResponse, int maxResults) throws Exception {
        List<PaperInfo> papers = new ArrayList<>();

        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode dataNode = rootNode.get("data");

        if (dataNode == null || !dataNode.isArray()) {
            return papers;
        }

        for (JsonNode paperNode : dataNode) {
            if (papers.size() >= maxResults) {
                break;
            }

            try {
                PaperInfo paper = new PaperInfo();
                paper.setTitle(paperNode.has("title") ? paperNode.get("title").asText() : "");
                
                // 提取作者
                if (paperNode.has("authors") && paperNode.get("authors").isArray()) {
                    List<String> authorNames = new ArrayList<>();
                    for (JsonNode authorNode : paperNode.get("authors")) {
                        if (authorNode.has("name")) {
                            authorNames.add(authorNode.get("name").asText());
                        }
                    }
                    paper.setAuthors(String.join(", ", authorNames));
                }

                // 提取URL
                if (paperNode.has("url")) {
                    paper.setUrl(paperNode.get("url").asText());
                }

                // 提取论文ID
                if (paperNode.has("paperId")) {
                    paper.setExternalPaperId(paperNode.get("paperId").asText());
                }

                // 提取摘要
                if (paperNode.has("abstract")) {
                    paper.setAbstract(paperNode.get("abstract").asText());
                }

                paper.setSource("Semantic Scholar");
                papers.add(paper);
            } catch (Exception e) {
                log.warn("解析Semantic Scholar条目失败: {}", e.getMessage());
            }
        }

        return papers;
    }

    /**
     * 从PDF分析结果中提取搜索关键词
     */
    public String extractSearchKeywords(String pdfAnalysisText) {
        if (pdfAnalysisText == null || pdfAnalysisText.isEmpty()) {
            return "";
        }

        // 提取关键词（简化实现）
        // 实际可以使用更复杂的NLP方法提取关键词
        String[] words = pdfAnalysisText.toLowerCase()
                .replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                .split("\\s+");

        // 停用词列表
        java.util.Set<String> stopWords = new java.util.HashSet<>(java.util.Arrays.asList(
                "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
                "的", "是", "在", "了", "和", "与", "或", "但", "而", "为", "以", "及"
        ));

        // 提取重要词汇（长度>3，非停用词）
        List<String> keywords = new ArrayList<>();
        for (String word : words) {
            word = word.trim();
            if (word.length() > 3 && !stopWords.contains(word)) {
                keywords.add(word);
                if (keywords.size() >= 5) { // 最多取5个关键词
                    break;
                }
            }
        }

        return String.join(" ", keywords);
    }

    /**
     * 论文信息类
     */
    public static class PaperInfo {
        private String title;
        private String authors;
        private String url;
        private String externalPaperId;
        private String source; // arXiv, Semantic Scholar等
        private String abstractText;

        // Getters and Setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthors() {
            return authors;
        }

        public void setAuthors(String authors) {
            this.authors = authors;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getExternalPaperId() {
            return externalPaperId;
        }

        public void setExternalPaperId(String externalPaperId) {
            this.externalPaperId = externalPaperId;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getAbstract() {
            return abstractText;
        }

        public void setAbstract(String abstractText) {
            this.abstractText = abstractText;
        }
    }
}

