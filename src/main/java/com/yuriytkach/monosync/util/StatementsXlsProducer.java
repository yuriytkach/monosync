package com.yuriytkach.monosync.util;

import java.time.LocalDate;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.yuriytkach.monosync.model.Statement;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Dependent
public class StatementsXlsProducer {

  @Inject
  CurrencyMapper currencyMapper;

  public XSSFWorkbook produceReport(final List<Statement> statements, final int accountCurrency) {
    final XSSFWorkbook workbook = new XSSFWorkbook();
    final XSSFSheet sheet = workbook.createSheet(LocalDate.now().toString());

    for (int i = 0; i < statements.size(); i++) {
      final var row = sheet.createRow(i);
      final var statement = statements.get(i);

      row.createCell(0).setCellValue(statement.getPrintedDate());
      row.createCell(1).setCellValue(statement.getPrintedDescription(currencyMapper, accountCurrency));
      row.createCell(2).setCellValue(statement.getPrintedAmount());
    }

    sheet.setColumnWidth(0, 3200);
    sheet.setColumnWidth(1, 15000);
    sheet.setColumnWidth(2, 3200);

    return workbook;
  }

}
