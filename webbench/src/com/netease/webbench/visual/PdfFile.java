/**
  * Copyright (c) <2011>, <NetEase Corporation>
  * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *    3. Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netease.webbench.visual;

import java.awt.Color;
import java.io.FileOutputStream;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.netease.webbench.statis.PdfTable;

/**
 * PDF file
 * @author LI WEIZHAO
 */
public class PdfFile {
	private int pageHeight = 700;
	private int pageLength = 900;
	private Document document;
	private PdfWriter writer;
	
	public PdfFile(String saveAsFileName) throws Exception {
		Rectangle pageSize = new Rectangle(pageLength, pageHeight);
		pageSize.setBackgroundColor(new java.awt.Color(0xFF, 0xFF, 0xFF));
		document = new Document(pageSize);	
		writer = PdfWriter.getInstance(document, new FileOutputStream(saveAsFileName));
	}
	
	public PdfFile(int pageHeight, int pageLength, String saveAsFileName) throws Exception {
		this.pageHeight = pageHeight;
		this.pageLength = pageLength;
		Rectangle pageSize = new Rectangle(pageLength, pageHeight);
		pageSize.setBackgroundColor(new java.awt.Color(0xFF, 0xFF, 0xFF));
		document = new Document(pageSize);	
		writer = PdfWriter.getInstance(document, new FileOutputStream(saveAsFileName));
	}
	
	public void beginAddElement() {
		if (!document.isOpen())
			document.open();
	}
	
	public void endAddElement() {
		if (document.isOpen())
			document.close();
	}
	
	public void addCover(String title, String subTitle, int titleFontSize, int subTitleFontSize) throws Exception {
        PdfContentByte cb = writer.getDirectContent();        
        cb.beginText();        
        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);        
        cb.setFontAndSize(bf, titleFontSize);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, title, pageLength / 2, pageHeight / 2, 0);        
        cb.setFontAndSize(bf, subTitleFontSize);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, subTitle, pageLength / 2, pageHeight / 2 - titleFontSize, 0);        
        cb.endText();
	}
	
	public void addTextPage(String title, String content, int fontSize, int lineSpace) throws Exception {
		document.newPage();
		String[] lines = content.split("\n");
		
		PdfContentByte cb = writer.getDirectContent();
		cb.beginText();
		/* print title */
		BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252,
				BaseFont.NOT_EMBEDDED);
		cb.setFontAndSize(bf, 35);
		int yPosit = pageHeight - 50;
		cb.showTextAligned(PdfContentByte.ALIGN_CENTER, title, pageLength / 2, yPosit, 0);

		/* print main body */
		bf = BaseFont.createFont(BaseFont.TIMES_ITALIC, BaseFont.CP1250,
				BaseFont.NOT_EMBEDDED);
		cb.setFontAndSize(bf, fontSize);
		yPosit -= 50;
		for (int i = 0; i < lines.length; i++) {
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, lines[i],
					50, yPosit, 0);
			yPosit -= (fontSize + lineSpace);
		}
		cb.endText();		
	}
	
	public void addAllImageFile(List<String> imageFilePath) throws Exception {
		/* loop to read pdf file, and add to pdf file */
		for (String path : imageFilePath) {
			document.newPage();
			Image img = Image.getInstance(path);
			img.setAlignment(Image.ALIGN_CENTER);
			document.add(img);
		}
	}
	
	public void addImageFile(String imageFilePath) throws Exception {
		document.newPage();
		Image img = Image.getInstance(imageFilePath);
		img.setAlignment(Image.ALIGN_CENTER);
		document.add(img);		
	}
	
	public void addTablePage(PdfTable pdfTable) throws Exception {
		document.newPage();	
		Font font = FontFactory.getFont("Helvetica", 35, Font.BOLD, Color.BLACK);
		Paragraph p = new Paragraph(pdfTable.getTitle(), font);
		p.setAlignment(Element.ALIGN_CENTER);
		document.add(p);
		font = FontFactory.getFont("Helvetica", 18, Font.NORMAL, Color.BLACK);
		p = new Paragraph(pdfTable.getSubTitle(), font);
		p.setAlignment(Element.ALIGN_CENTER);
		document.add(p);
		document.add(pdfTable.getTable());
	}
}
