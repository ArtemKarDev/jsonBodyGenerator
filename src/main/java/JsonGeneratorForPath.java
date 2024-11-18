import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class JsonGeneratorForPath {

    // Генератор случайных значений
    private static final Random random = new Random();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        // Путь к файлу openApi.yaml
        String openApiFilePath = "openapiCSOC.yaml";

        // Путь и метод эндпоинта для генерации
        String endpointPath = "/sources/upsert$";
        String httpMethod = "post";

        // Парсим OpenAPI спецификацию
        OpenAPI openAPI = parseOpenApi(openApiFilePath);

        // Генерация JSON Body для эндпоинта
        Map<String, Object> jsonBody = generateJsonBodyForEndpoint(openAPI, endpointPath, httpMethod);

        // Выводим сгенерированный JSON
        String jsonOutput = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonBody);
        System.out.println(jsonOutput);
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

    // Метод для получения схемы из запроса эндпоинта
    private static Map<String, Object> generateJsonBodyForEndpoint(OpenAPI openAPI, String endpointPath, String httpMethod) {
        // Получаем операции для эндпоинта по пути
        Map<String, PathItem> paths = openAPI.getPaths();
        PathItem pathItem = paths.get(endpointPath);
        if (pathItem == null) {
            throw new RuntimeException("Не найден путь в OpenAPI: " + endpointPath);
        }

        // Получаем операцию по методу (POST, GET и т.д.)
        Operation operation = null;
        switch (httpMethod.toLowerCase()) {
            case "post":
                operation = pathItem.getPost();
                break;
            case "get":
                operation = pathItem.getGet();
                break;
            case "put":
                operation = pathItem.getPut();
                break;
            case "delete":
                operation = pathItem.getDelete();
                break;
            default:
                throw new RuntimeException("Неизвестный HTTP метод: " + httpMethod);
        }

        if (operation == null || operation.getRequestBody() == null || operation.getRequestBody().getContent().get("application/json") == null) {
            throw new RuntimeException("Не найдено тело запроса для эндпоинта " + endpointPath);
        }

        // Извлекаем схему запроса из `requestBody`
        MediaType mediaType = operation.getRequestBody().getContent().get("application/json");
        Schema<?> requestSchema = mediaType.getSchema();

        // Генерируем JSON тело для схемы
        return generateJsonBody(requestSchema, openAPI);
    }

    // Метод для генерации JSON Body на основе схемы
    private static Map<String, Object> generateJsonBody(Schema<?> schema, OpenAPI openAPI) {
        Map<String, Object> jsonBody = new HashMap<>();

        if (schema instanceof ObjectSchema) {
            // Обрабатываем поля для ObjectSchema (сложные объекты)
            ObjectSchema objectSchema = (ObjectSchema) schema;
            for (Map.Entry<String, Schema> entry : objectSchema.getProperties().entrySet()) {
                String fieldName = entry.getKey();
                Schema fieldSchema = entry.getValue();

                jsonBody.put(fieldName, generateRandomValueForField(fieldSchema, openAPI));
            }
        }

        return jsonBody;
    }

    // Метод для генерации случайного значения для каждого типа
    private static Object generateRandomValueForField(Schema<?> fieldSchema, OpenAPI openAPI) {
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

                // Проверка на наличие ссылки $ref в элементах массива
                if (itemsSchema.get$ref() != null) {
                    String ref = itemsSchema.get$ref();
                    String refName = ref.substring(ref.lastIndexOf("/") + 1); // Получаем имя ссылки
                    itemsSchema = openAPI.getComponents().getSchemas().get(refName); // Извлекаем схему по $ref
                }

                for (int i = 0; i < arrayLength; i++) {
                    // Рекурсивно генерируем значения для каждого элемента массива
                    randomArrayValues.add(generateRandomValueForField(itemsSchema, openAPI));
                }
                return randomArrayValues;
            }
        } else if (fieldSchema instanceof ObjectSchema) {
            // Рекурсивная обработка вложенного объекта
            return generateJsonBody(fieldSchema, openAPI);
        } else if (fieldSchema instanceof NumberSchema) {
            // Генерация случайного числа (для типа number)
            return generateRandomNumber();
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
        int year = random.nextInt(20) + 2000; // 2000-2019
        int month = random.nextInt(12) + 1;  // 1-12
        int day = random.nextInt(28) + 1;    // 1-28
        int hour = random.nextInt(24);       // 0-23
        int minute = random.nextInt(60);     // 0-59
        int second = random.nextInt(60);     // 0-59
        return String.format("%04d-%02d-%02dT%02d:%02d:%02dZ", year, month, day, hour, minute, second);
    }

    // Генерация случайного числа (для типа number)
    private static double generateRandomNumber() {
        return random.nextDouble() * 1000; // Генерация случайного числа от 0 до 1000
    }
}
