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
package com.netease.webbench.statis;

import java.awt.Color;

import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/**
 * 
 * @author LI WEIZHAO
 *
 */
public abstract class PdfTable {
	protected 	PdfPTable table;
	protected String title;
	protected String subTitle;
	
	public PdfTable(String title, String subTitle) {
		this.title = title;
		this.subTitle = subTitle;
	}
	
    public static PdfPCell makeCell(String text, boolean isBold, boolean grayBackground, int colspan) {
    	Font font;
    	if (isBold) {
    		font = FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK);
    	} else {
    		font = FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK);
    	}
        Paragraph p = new Paragraph(text, font);
        PdfPCell cell = new PdfPCell(p);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setFixedHeight(50);
        cell.setColspan(colspan);
        if (grayBackground) {
        	cell.setBackgroundColor(new Color(0xC0, 0xC0, 0xC0));
        }
        return cell;
    }
    
	public static PdfPTable createNewTable(int colNums, float[] widths) throws Exception {
		PdfPTable table = new PdfPTable(colNums);
		Rectangle r = new Rectangle(PageSize.A4.getRight(72), PageSize.A4.getTop(72));
		table.setWidthPercentage(widths, r);
		table.setTotalWidth(860);
		table.setLockedWidth(true);
		table.setSpacingBefore(25f);	
		return table;
	}
	
	public PdfPTable getTable() {
		return table;
	}

	public String getTitle() {
		return title;
	}

	public String getSubTitle() {
		return subTitle;
	}
}
