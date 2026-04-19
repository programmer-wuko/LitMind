package com.litmind.service.recommend;

import java.util.List;

public interface ExternalPaperService {

    List<PaperInfo> searchArxivPapers(String query, int maxResults);

    List<PaperInfo> searchSemanticScholarPapers(String query, int maxResults);

    List<PaperInfo> getHotPapers(int maxResults);

    String extractSearchKeywords(String pdfAnalysisText);

    class PaperInfo {
        private String title;
        private String authors;
        private String url;
        private String externalPaperId;
        private String source; // arXiv, Semantic Scholar等
        private String abstractText;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getAuthors() { return authors; }
        public void setAuthors(String authors) { this.authors = authors; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getExternalPaperId() { return externalPaperId; }
        public void setExternalPaperId(String externalPaperId) { this.externalPaperId = externalPaperId; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getAbstract() { return abstractText; }
        public void setAbstract(String abstractText) { this.abstractText = abstractText; }
    }
}
