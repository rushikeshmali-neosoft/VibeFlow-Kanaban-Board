package com.vibeflow.admin.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import com.vibeflow.task.entity.Task;
import com.vibeflow.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminExportService {

    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public byte[] exportTasksCsv() {
        List<Task> tasks = taskRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(out))) {
            writer.writeNext(new String[]{"ID", "Title", "Status", "Assignee"});
            for (Task t : tasks) {
                String assignee = t.getAssignee() != null ? t.getAssignee().getEmail() : "Unassigned";
                writer.writeNext(new String[]{
                        t.getId().toString(),
                        t.getTitle(),
                        t.getStatus().name(),
                        assignee
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating CSV", e);
        }
        return out.toByteArray();
    }

    @Transactional(readOnly = true)
    public byte[] exportTasksExcel() {
        List<Task> tasks = taskRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Tasks");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Title");
            headerRow.createCell(2).setCellValue("Status");
            headerRow.createCell(3).setCellValue("Assignee");

            int rowIdx = 1;
            for (Task t : tasks) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(t.getId());
                row.createCell(1).setCellValue(t.getTitle());
                row.createCell(2).setCellValue(t.getStatus().name());
                row.createCell(3).setCellValue(t.getAssignee() != null ? t.getAssignee().getEmail() : "Unassigned");
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportTasksPdf() {
        List<Task> tasks = taskRepository.findAll();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("VibeFlow Tasks Report"));
            document.add(new Paragraph("--------------------------------------------------"));
            
            for (Task t : tasks) {
                String assignee = t.getAssignee() != null ? t.getAssignee().getEmail() : "Unassigned";
                document.add(new Paragraph(
                        String.format("ID: %d | Title: %s | Status: %s | Assignee: %s",
                                t.getId(), t.getTitle(), t.getStatus().name(), assignee)
                ));
            }
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
