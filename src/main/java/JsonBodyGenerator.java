import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.parser.OpenAPIV3Parser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class JsonBodyGenerator {

    // Генератор случайных значений
    private static final Random random = new Random();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        // Путь к файлу openApi.yaml
        String openApiFilePath = "openapiCSOC.yaml";

        // Парсим OpenAPI спецификацию
        OpenAPI openAPI = parseOpenApi(openApiFilePath);

        // Пример генерации JSON для объекта
        Schema<?> schema = openAPI.getComponents().getSchemas().get("ru.vtb.integration.mdm.cs.organization.model.primaryrecordwriter.v3.PartialUpsertRequest");
        Map<String, Object> objectJsonBody = generateJsonBody(schema, openAPI);

        // Выводим сгенерированный JSON
        String jsonBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectJsonBody);
        System.out.println(jsonBody);
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

    // Метод для генерации JSON Body на основе схемы
    private static Map<String, Object> generateJsonBody(Schema<?> schema, OpenAPI openAPI) {
        Map<String, Object> jsonBody = new HashMap<>();

        if (schema instanceof ObjectSchema) {
            // Обрабатываем поля для ObjectSchema (сложные объекты)
            ObjectSchema objectSchema = (ObjectSchema) schema;
            for (Map.Entry<String, Schema> entry : objectSchema.getProperties().entrySet()) {
                String fieldName = entry.getKey();
                Schema fieldSchema = entry.getValue();

                // Проверка на наличие ссылки на схему (через $ref)
                if (fieldSchema.get$ref() != null) {
                    String ref = fieldSchema.get$ref();
                    String refName = ref.substring(ref.lastIndexOf("/") + 1);
                    fieldSchema = openAPI.getComponents().getSchemas().get(refName); // Получаем схему по $ref
                }

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
                int arrayLength = random.nextInt(3) + 1; // Длина массива от 1 до 5

                // Проверка на наличие ссылки $ref в элементах массива
                if (itemsSchema.get$ref() != null) {
                    String ref = itemsSchema.get$ref();
                    String refName = ref.substring(ref.lastIndexOf("/") + 1); // Получаем имя ссылки на схему
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
        // Возвращаем случайную дату в формате ISO-8601
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
        return random.nextDouble() * 100; // Пример случайного числа от 0 до 100
    }
}
