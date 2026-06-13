package ai.xenopilot.customer;

import java.util.List;

public record CsvRecordPreview<T>(int rowNumber, T data, boolean valid, List<String> errors) {
}
