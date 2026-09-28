# API Contract: POST /users

## Request
{
  "email": "user@example.com",
  "name": "User Name",
  "password": "securepassword123"
}

## Response (201 Created)
{
  "id": 1,
  "email": "user@example.com",
  "name": "User Name"
}

## Error (400 Bad Request)
{
  "error": "Email already exists"
}
