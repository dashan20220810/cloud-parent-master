package com.qianyi.casinoadmin.util;

import org.apache.poi.ss.usermodel.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtils {


    public static List<List<String>> readExcelContentList(Workbook wb){
        if(wb != null){
            List<List<String>> content = new ArrayList<>();
            SimpleDateFormat smf = new SimpleDateFormat("yyyy-MM-dd");
            Sheet sheet = wb.getSheetAt(0);
            Row row = sheet.getRow(0);
            int rowNum = sheet.getLastRowNum();
            int colNum = row.getPhysicalNumberOfCells();
            //正文内容应该从第二行读取，第一行为表头
            for (int ri = 1; ri <= rowNum; ri++) {
                row = sheet.getRow(ri);
                int ci = 0;
                List<String> col = new ArrayList<>();
                while (ci < colNum){
                    Cell cell = row.getCell(ci++);
                    if(cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK){
                        break;
                    }
                    CellType cellTypeEnum = cell.getCellTypeEnum();
                    if(cellTypeEnum.getCode() == 1){
                        String data = cell.getRichStringCellValue().getString();
                        col.add(data);
                    }else{
                        String data = String.valueOf((int)cell.getNumericCellValue());
                        col.add(data);
                    }
                }
                content.add(col);
            }
            return content;
        }
        return null;
    }


}
