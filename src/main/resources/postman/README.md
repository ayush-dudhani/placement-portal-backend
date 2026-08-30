# Postman suite

Import both JSON files in this directory, select **Placement Portal - Local**, and start the backend on port 8080.

Recommended order:

1. Run **Authentication / Register student** once.
2. Run **Authentication / Login student**. Its test script saves `studentAccessToken` and makes it the active `accessToken`.
3. For admin APIs, provide an existing admin account and run **Authentication / Login admin**. It saves `adminAccessToken` and makes it active.
4. Run create requests before dependent requests. Tests capture IDs such as `companyId`, `driveId`, `roleId`, `applicationId`, `roundId`, and `offerId`.
5. Use **Authentication / Use student token** or **Use admin token** to switch workspaces without logging in again.

Postman keeps the HttpOnly refresh cookie in its cookie jar. File requests contain empty file placeholders that must be selected locally. CSV formats are documented in `docs/api-contract.md`. Change `idempotencyKey` when submitting different import content.

Run **Logout** with the active bearer token selected. It sends both the Postman refresh cookie and `Authorization` header so the refresh token and Redis-backed access session are revoked together.
