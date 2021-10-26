# spring_rest

Сreate a simple SpringBoot app.
Implement following API methods:
1. Uploading xml files with name structure – customer_type_date.xml
   1.1. Validate name
   1.2. Parse content and convert to json
   1.3. Save on a filesystem
   1.4. Throw exception if there is file with the same name
2. Replace file –same behavior as above except exception file should be replaced
3. Delete file by name
4. Get content of file by name
5. Get files by date
6. Get files by customer
7. Get files by type