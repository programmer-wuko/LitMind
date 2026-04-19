package com.litmind.service.ai;

import com.litmind.service.pdf.PdfAnalysisService;

public interface AiService {

    String getModelName();

    PdfAnalysisService.PdfAnalysisResult analyzePdf(String pdfText);

    String answerQuestion(String pdfText, String question);
}
