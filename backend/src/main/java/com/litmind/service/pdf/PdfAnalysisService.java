package com.litmind.service.pdf;

import com.litmind.model.entity.PdfAnalysis;

public interface PdfAnalysisService {

    PdfAnalysis getAnalysis(Long fileId);

    PdfAnalysis analyzePdf(Long fileId);

    PdfAnalysis updateAnalysis(Long fileId, PdfAnalysisUpdateRequest request);

    String extractTextFromPdf(String filePath);

    class PdfAnalysisUpdateRequest {
        private String researchBackground;
        private String coreContent;
        private String experimentResults;
        private String additionalInfo;

        public String getResearchBackground() { return researchBackground; }
        public void setResearchBackground(String researchBackground) { this.researchBackground = researchBackground; }
        public String getCoreContent() { return coreContent; }
        public void setCoreContent(String coreContent) { this.coreContent = coreContent; }
        public String getExperimentResults() { return experimentResults; }
        public void setExperimentResults(String experimentResults) { this.experimentResults = experimentResults; }
        public String getAdditionalInfo() { return additionalInfo; }
        public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }
    }

    class PdfAnalysisResult {
        private String researchBackground;
        private String coreContent;
        private String experimentResults;
        private String additionalInfo;

        public String getResearchBackground() { return researchBackground; }
        public void setResearchBackground(String researchBackground) { this.researchBackground = researchBackground; }
        public String getCoreContent() { return coreContent; }
        public void setCoreContent(String coreContent) { this.coreContent = coreContent; }
        public String getExperimentResults() { return experimentResults; }
        public void setExperimentResults(String experimentResults) { this.experimentResults = experimentResults; }
        public String getAdditionalInfo() { return additionalInfo; }
        public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }
    }
}
