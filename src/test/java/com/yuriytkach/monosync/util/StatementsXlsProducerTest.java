package com.yuriytkach.monosync.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yuriytkach.monosync.model.Statement;

@ExtendWith(MockitoExtension.class)
class StatementsXlsProducerTest {

  @Mock
  private CurrencyMapper currencyMapper;

  @InjectMocks
  private StatementsXlsProducer tested;

  @Test
  void shouldProduceXlsFile() throws IOException {
    final var statement = new Statement(
      "ZuHWzqkKGVo=",
      1554466347,
      "Покупка щастя",
      7997,
      false,
      -95000,
      -95000,
      980,
      0,
      19000,
      10050000
    );

    try (XSSFWorkbook workbook = tested.produceReport(List.of(statement), 980)) {
      try (XSSFWorkbook expected = new XSSFWorkbook(getClass().getClassLoader().getResourceAsStream("test.xlsx"))) {
        assertThat(workbook.getNumberOfSheets()).isEqualTo(1);

        final XSSFSheet actualSheet = workbook.getSheetAt(0);
        final XSSFSheet expectedSheet = expected.getSheetAt(0);

        assertThat(actualSheet.getLastRowNum()).isEqualTo(expectedSheet.getLastRowNum());

        IntStream.range(0, actualSheet.getLastRowNum()).forEach(rowIndex -> {
          final XSSFRow actualRow = actualSheet.getRow(rowIndex);
          final XSSFRow expectedRow = expectedSheet.getRow(rowIndex);

          assertThat(actualRow.getLastCellNum()).isEqualTo(expectedRow.getLastCellNum());

          IntStream.range(0, actualRow.getLastCellNum()).forEach(col -> {
            final XSSFCell actualCell = actualRow.getCell(col);
            final XSSFCell expectedCell = expectedRow.getCell(col);

            if (expectedCell == null) {
              assertThat(actualCell).describedAs("Expected cell is empty. Actual should be empty").isNull();
            } else {
              assertThat(actualCell.getCellType()).isEqualTo(expectedCell.getCellType());
              switch (actualCell.getCellType()) {
                case STRING -> assertThat(actualCell.getStringCellValue()).isEqualTo(expectedCell.getStringCellValue());
                case NUMERIC ->
                  assertThat(actualCell.getNumericCellValue()).isEqualTo(expectedCell.getNumericCellValue());
                case BOOLEAN ->
                  assertThat(actualCell.getBooleanCellValue()).isEqualTo(expectedCell.getBooleanCellValue());
                default -> throw new IllegalArgumentException();
              }
              assertThat(actualCell.getCellStyle()).isEqualTo(expectedCell.getCellStyle());
            }
            assertThat(actualSheet.isColumnHidden(col)).isEqualTo(expectedSheet.isColumnHidden(col));
          });
        });
      }
    }
  }

}
