# InspectusPro API

Backend do MVP **multi-tenant** para laudos com **formulários dinâmicos**, **permissões granulares** e base para **planos/cupons**.

## Stack (backend)
- Java 21
- Spring Boot 4
- REST (Spring MVC)
- Spring Security (JWT próprio no MVP)
- PostgreSQL + Flyway
- JPA (Hibernate)
- OpenAPI/Swagger UI (springdoc)

## Multi-tenant (MVP)
- Isolamento **row-level** via coluna `tenant_id`.
- O tenant será identificado por header: `X-Tenant-Id`.

## Como rodar testes
No Windows (PowerShell), garanta `JAVA_HOME` e execute:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\Openjdk-21-21.0.2"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\mvnw.cmd test
```

## Documentação da API (Swagger)
Após subir a aplicação, a UI do Swagger fica disponível em:
- `/swagger-ui.html`

