# Application Name: Doc Inteligence System


### OpenSearch Setup
1. Download `Docker`
2. Set Env Variable: 
    ```
    window:
    set OPENSEARCH_INITIAL_ADMIN_PASSWORD=<strong-password>{DocIntel@1221}
    echo %OPENSEARCH_INITIAL_ADMIN_PASSWORD%
    ```

3. Run in main page : `docker compose up -d`

### Test Commands:
1. curl -X POST http://localhost:8080/api/document -F "file=@doc.txt"
2. curl -X POST http://localhost:8080/api/document -F "file=@DocIntel.pdf"
3. curl -X POST http://localhost:8080/api/document -F "file=@test.pdf"