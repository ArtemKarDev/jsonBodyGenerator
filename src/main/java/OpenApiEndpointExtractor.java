import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;

public class OpenApiEndpointExtractor {

    public static void main(String[] args) throws Exception {
        // Укажите путь к вашему файлу OpenAPI (например, "openApi.yaml")
        String openApiFilePath = "openapiCSOC.yaml";

        // Загружаем и парсим OpenAPI спецификацию
        OpenAPI openAPI = parseOpenApi(openApiFilePath);

        // Извлекаем все пути (эндпоинты)
        if (openAPI.getPaths() != null) {
            System.out.println("Список всех эндпоинтов:");
            openAPI.getPaths().forEach((path, pathItem) -> {
                System.out.println(path); // Печатаем путь
                pathItem.readOperationsMap().forEach((method, operation) -> {
                    // Печатаем HTTP метод и описание операции
                    System.out.println("  HTTP Метод: " + method);
                    System.out.println("  Описание: " + operation.getSummary());
                });
            });
        } else {
            System.out.println("Эндпоинты не найдены.");
        }
    }

    // Метод для парсинга OpenAPI файла
    private static OpenAPI parseOpenApi(String filePath) {
        OpenAPIV3Parser parser = new OpenAPIV3Parser();
        SwaggerParseResult parseResult = parser.readLocation(filePath, null, null);
        if (parseResult.getMessages().size() > 0) {
            throw new RuntimeException("Ошибка при парсинге OpenAPI: " + String.join(", ", parseResult.getMessages()));
        }
        return parseResult.getOpenAPI();
    }
}
