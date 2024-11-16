import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.RequestBody;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.parser.core.models.SwaggerParseResult;


import java.util.*;

public class JsonGenerator {

    // Генератор случайных значений
    private static final Random random = new Random();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        // Путь к файлу openApi.yaml
        String openApiFilePath = "openApi.yaml";

        // Парсим OpenAPI спецификацию
        OpenAPI openAPI = parseOpenApi(openApiFilePath);

        // Эндпоинт, для которого мы хотим сгенерировать JSON тело запроса
        String endpoint = "/pet"; // Пример: /pets
        String method = "post"; // Пример: POST запрос

        // Генерация JSON Body для запроса
        Map<String, Object> jsonBody = generateJsonBodyForEndpoint(openAPI, endpoint, method);
        System.out.println(jsonBody.toString());

        // Выводим сгенерированный JSON
        String jsonBodyStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonBody);
        System.out.println(jsonBodyStr);
    }

    // Метод для парсинга OpenAPI спецификации
    private static OpenAPI parseOpenApi(String filePath) {
        OpenAPIV3Parser parser = new OpenAPIV3Parser();
        SwaggerParseResult parseResult = parser.readLocation(filePath, null, null);
        if (parseResult.getMessages().size() > 0) {
            throw new RuntimeException("Ошибка при парсинге OpenAPI: " + String.join(", ", parseResult.getMessages()));
        }
        return parseResult.getOpenAPI();
    }

    // Генерация JSON тела для указанного эндпоинта
    private static Map<String, Object> generateJsonBodyForEndpoint(OpenAPI openAPI, String endpoint, String method) {
        PathItem pathItem = openAPI.getPaths().get(endpoint);
        if (pathItem == null) {
            throw new RuntimeException("Эндпоинт не найден: " + endpoint);
        }

        // Получаем нужный метод (например, POST)
        Operation operation = getOperationForMethod(pathItem, method);
        if (operation == null || operation.getRequestBody() == null) {
            throw new RuntimeException("Для указанного метода (" + method + ") нет тела запроса.");
        }

        // Получаем схему тела запроса из RequestBody
        RequestBody requestBody = operation.getRequestBody();
        Schema<?> requestSchema = requestBody.getContent().get("application/json").getSchema();

        // Генерируем тело запроса
        return generateJsonBody(requestSchema);
    }

    // Получение Operation для конкретного метода (GET, POST и т.д.)
    private static Operation getOperationForMethod(PathItem pathItem, String method) {
        switch (method.toUpperCase()) {
            case "GET":
                return pathItem.getGet();
            case "POST":
                return pathItem.getPost();
            case "PUT":
                return pathItem.getPut();
            case "DELETE":
                return pathItem.getDelete();
            case "PATCH":
                return pathItem.getPatch();
            default:
                return null;
        }
    }

    // Генерация JSON Body на основе схемы
    private static Map<String, Object> generateJsonBody(Schema<?> schema) {
        Map<String, Object> jsonBody = new HashMap<>();

        if (schema instanceof ObjectSchema) {
            // Обрабатываем поля для ObjectSchema (сложные объекты)
            ObjectSchema objectSchema = (ObjectSchema) schema;
            for (Map.Entry<String, Schema> entry : objectSchema.getProperties().entrySet()) {
                String fieldName = entry.getKey();
                Schema fieldSchema = entry.getValue();

                jsonBody.put(fieldName, generateRandomValueForField(fieldSchema));
            }
        }

        return jsonBody;
    }

    // Генерация случайного значения для каждого типа
    private static Object generateRandomValueForField(Schema<?> fieldSchema) {
        if (fieldSchema instanceof StringSchema) {
            // Генерация случайной строки
            int maxLength = ((StringSchema) fieldSchema).getMaxLength() != null ? ((StringSchema) fieldSchema).getMaxLength() : 20;
            return generateRandomString(maxLength);
        } else if (fieldSchema instanceof IntegerSchema) {
            // Генерация случайного целого числа
            return random.nextInt(1000); // пример диапазона
        } else if (fieldSchema instanceof BooleanSchema) {
            // Генерация случайного булевого значения
            return random.nextBoolean();
        } else if (fieldSchema instanceof DateTimeSchema) {
            // Генерация случайной даты и времени
            return generateRandomDateTime();
        } else if (fieldSchema instanceof ArraySchema) {
            // Обработка массива
            ArraySchema arraySchema = (ArraySchema) fieldSchema;
            Schema<?> itemsSchema = arraySchema.getItems();
            if (itemsSchema != null) {
                List<Object> randomArrayValues = new ArrayList<>();
                int arrayLength = random.nextInt(5) + 1; // Длина массива от 1 до 5
                for (int i = 0; i < arrayLength; i++) {
                    randomArrayValues.add(generateRandomValueForField(itemsSchema));
                }
                return randomArrayValues;
            }
        }

        return null;
    }

    // Генерация случайной строки указанной длины
    private static String generateRandomString(int maxLength) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < maxLength; i++) {
            int randomIndex = random.nextInt(characters.length());
            stringBuilder.append(characters.charAt(randomIndex));
        }
        return stringBuilder.toString();
    }

    // Генерация случайной даты и времени
    private static String generateRandomDateTime() {
        // Возвращаем случайную дату в формате ISO-8601
        int year = random.nextInt(20) + 2000; // 2000-2019
        int month = random.nextInt(12) + 1;  // 1-12
        int day = random.nextInt(28) + 1;    // 1-28
        int hour = random.nextInt(24);       // 0-23
        int minute = random.nextInt(60);     // 0-59
        int second = random.nextInt(60);     // 0-59
        return String.format("%04d-%02d-%02dT%02d:%02d:%02dZ", year, month, day, hour, minute, second);
    }
}
